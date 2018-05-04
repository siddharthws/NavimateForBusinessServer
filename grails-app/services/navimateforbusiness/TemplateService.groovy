package navimateforbusiness

import grails.gorm.transactions.Transactional

@Transactional
class TemplateService {
    // ----------------------- Dependencies ---------------------------//
    def fieldService

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
                throw new navimateforbusiness.ApiException("Illegal access to template", navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
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

    // ----------------------- Private APIs ---------------------------//
}
