package navimateforbusiness

import grails.gorm.transactions.Transactional
import navimateforbusiness.enums.TaskStatus
import navimateforbusiness.util.ApiException
import navimateforbusiness.util.Constants

@Transactional
class TemplateService {
    // ----------------------- Dependencies ---------------------------//
    def fieldService
    def leadService
    def taskService
    def formService
    def productService

    // ----------------------- Getters ---------------------------//
    // Method to get all templates for a user
    def getForUser(User user) {
        // Get all unremoved templates for user's account and given type
        def templates = Template.findAllByAccountAndIsRemoved(user.account, false)

        // Sort templates in order of IDs
        templates = templates.sort {it -> it.id}

        // Return sorted templates
        return templates
    }
    def getForUserRemoved(User user) {
        // Get all unremoved templates for user's account and given type
        def templates = Template.findAllByAccount(user.account, false)

        // Sort templates in order of IDs
        templates = templates.sort {it -> it.id}

        // Return sorted templates
        return templates
    }

    // Method to get all templates for a user with a specific type
    def getForUserByType(User user, int type) {
        // Get all templates available for this user
        def templates = getForUser(user)

        // Use templates of given type only
        templates = templates.findAll {it -> it.type == type}

        // Return sorted templates
        return templates
    }

    // Method to get lead for user by id
    def getForUserById(User user, long id) {
        // Get lead by this id
        Template template = Template.findByAccountAndIsRemovedAndId(user.account, false, id)

        template
    }

    // Method to get lead for user by name
    def getForUserByName(User user, String name, int type) {
        // Get template by this name and type
        Template template = Template.findByAccountAndIsRemovedAndTypeAndName(user.account, false, type, name)

        template
    }

    // Method to get through last updated
    def getForUserAfterLastUpdated(User user, Date date) {
        def templates = getForUserRemoved(user)
        templates = templates.findAll {Template it -> it.lastUpdated >= date}
        templates
    }

    // ----------------------- Public APIs ---------------------------//
    // Methods to convert template objects to / from JSON
    def toJson(Template template) {
        // Convert template properties to JSON
        def json = [
            id: template.id,
            name: template.name,
            type: template.type,
            fields: []
        ]

        // Convert template fields to JSON
        def fields = fieldService.getForTemplate(template)
        fields.each {field ->
            json.fields.push(fieldService.toJson(field))
        }

        json
    }

    Template fromJson(def json, User user) {
        Template template = null

        // Get existing template or create new
        if (json.id) {
            template = getForUserById(user, json.id)
            if (!template) {
                throw new ApiException("Illegal access to template", Constants.HttpCodes.BAD_REQUEST)
            }
        } else {
            template = new Template(
                    account: user.account,
                    owner: user
            )
        }

        // Set parameters from JSON
        template.type = json.type
        template.name = json.name

        // Set fields using JSON entries
        json.fields.each {fieldJson ->
            // Convert JSON to field domain object
            def field = fieldService.fromJson(fieldJson, template)

            // Add new fields to template
            if (!field.id) {
                template.addToFields(field)
            }
        }

        // Add date info
        if (!template.dateCreated) {
            template.dateCreated = new Date()
        }
        template.lastUpdated = new Date()

        template
    }

    // Method to remove a template object
    def remove(User user, Template template) {
        // Remove all objects associatedw= with this template
        switch (template.type) {
            case Constants.Template.TYPE_FORM:
                // Remove all forms associated with this template
                def forms = formService.getAllForUserByFilter(user, [template: [ids: [template.id]]])
                forms.each {FormM form ->
                    formService.remove(user, form)
                }
                // Remove all tasks associated with this form template
                def tasks = taskService.getAllForUserByFilter(user, [formTemplate: [ids: [template.id]]])
                tasks.each {TaskM task ->
                    taskService.remove(user, task)
                }
                break
            case Constants.Template.TYPE_TASK:
                // Remove all tasks associated with this template
                def tasks = taskService.getAllForUserByFilter(user, [template: [ids: [template.id]]])
                tasks.each {TaskM task ->
                    taskService.remove(user, task)
                }
                break
            case Constants.Template.TYPE_LEAD:
                // Remove all leads associated with this template
                def leads = leadService.getAllForUserByFilter(user, [template: [ids: [template.id]]])
                leads.each {LeadM lead ->
                    leadService.remove(user, lead)
                }
                break
            case Constants.Template.TYPE_PRODUCT:
                // Remove all products associated with this template
                def products = productService.getAllForUserByFilter(user, [template: [ids: [template.id]]])
                products.each {ProductM product ->
                    productService.remove(user, product)
                }
                break
        }

        // Remove template
        template.isRemoved = true
        template.lastUpdated = new Date()
        template.save(failOnError: true, flush: true)
    }

    // Method to get FCMs associated with the lead
    def getAffectedReps (User user, Template template) {
        def reps = []

        // Add FCMs as per template type
        switch (template.type) {
            case Constants.Template.TYPE_FORM:
                // Get FCMs for all affected tasks
                def tasks = taskService.getAllForUserByFilter(user, [formTemplate: [ids: [template.id]]])
                def openTasks = tasks.findAll {TaskM it -> it.status == TaskStatus.OPEN}
                openTasks.each {TaskM task -> if (task.repId) {reps.push(User.findById(task.repId))} }
                break
            case Constants.Template.TYPE_LEAD:
                // Get all affected leads
                def leads = leadService.getAllForUserByFilter(user, [template: [ids: [template.id]]])
                leads.each {LeadM lead -> reps.addAll(leadService.getAffectedReps(user, lead)) }
                break
            case Constants.Template.TYPE_TASK:
                // Get FCMs for all affected tasks
                def tasks = taskService.getAllForUserByFilter(user, [template: [ids: [template.id]]])
                def openTasks = tasks.findAll {TaskM it -> it.status == TaskStatus.OPEN}
                openTasks.each {TaskM task -> if (task.repId) {reps.push(User.findById(task.repId))} }
                break
        }

        reps
    }

    // ----------------------- Private APIs ---------------------------//
}
