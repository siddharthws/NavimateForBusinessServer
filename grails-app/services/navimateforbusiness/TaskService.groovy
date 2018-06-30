package navimateforbusiness

import grails.gorm.transactions.Transactional
import navimateforbusiness.enums.Role
import navimateforbusiness.enums.TaskStatus
import navimateforbusiness.util.ApiException
import navimateforbusiness.util.Constants

@Transactional
class TaskService {
    // ----------------------- Dependencies ---------------------------//
    def templateService
    def userService
    def leadService
    def valueService
    def fcmService
    def formService

    // ----------------------- Getter APIs ---------------------------//
    // Method to get all tasks for a user
    def getForUser(User user) {
        def tasks = []

        // Get leads as per access level
        switch (user.role) {
            case Role.ADMIN:
                // Get all unremoved tasks created of account
                tasks = Task.findAllByAccountAndIsRemoved(user.account, false)
                break

            case Role.CC:
                // Get all unremoved tasks created by this user
                tasks = Task.findAllByAccountAndIsRemovedAndCreator(user.account, false, user)
                break

            case Role.MANAGER:
                // Get all unremoved tasks created by this user
                tasks = Task.findAllByAccountAndIsRemovedAndManager(user.account, false, user)
                break

            case Role.REP:
                // Get all unremoved tasks assigned to this rep
                tasks = Task.findAllByAccountAndIsRemovedAndRep(user.account, false, user)
                break
        }

        // Sort tasks in descending order of create date
        tasks = tasks.sort {it -> it.dateCreated}
        tasks = tasks.sort {it -> it.status.name()}
        tasks.reverse(true)

        // Return tasks
        tasks
    }

    def getForUserRemoved(User user) {
        def tasks = []

        // Get leads as per access level
        switch (user.role) {
            case Role.ADMIN:
                // Get all unremoved tasks created of account
                tasks = Task.findAllByAccount(user.account)
                break

            case Role.CC:
                // Get all unremoved tasks created by this user
                tasks = Task.findAllByAccountAndCreator(user.account, user)
                break

            case Role.MANAGER:
                // Get all unremoved tasks created by this user
                tasks = Task.findAllByAccountAndManager(user.account, user)
                break

            case Role.REP:
                // Get all unremoved tasks assigned to this rep
                tasks = Task.findAllByAccountAndRep(user.account, user)
                break
        }

        // Sort tasks in descending order of create date
        tasks = tasks.sort {it -> it.dateCreated}
        tasks = tasks.sort {it -> it.status.name()}
        tasks.reverse(true)

        // Return tasks
        tasks
    }

    // Method to get all tasks for a user
    def getForUserById(User user, Long id) {
        // Get all tasks for user
        def tasks = getForUser(user)

        // Find task by ID
        def task = tasks.find {it -> it.id == id}

        task
    }

    // Method to get all tasks for a user by rep
    def getForUserByRep(User user, User rep) {
        // Get all tasks for user
        def tasks = getForUser(user)

        // Find tasks by rep
        def task = tasks.findAll {Task it -> it.rep && it.rep.id == rep.id}

        task
    }

    // Method to get all tasks for a user by lead
    def getForUserByLead(User user, LeadM lead) {
        // Get all tasks for user
        def tasks = getForUser(user)

        // Find tasks by template
        def task = tasks.findAll {Task it -> it.leadid == lead.id}

        task
    }

    // Method to get all tasks for a user by template
    def getForUserByTemplate(User user, Template template) {
        // Get all tasks for user
        def tasks = getForUser(user)

        // Find tasks by template
        def task = tasks.findAll {Task it -> it.templateData.template.id == template.id}

        task
    }

    // Method to get all tasks for a user by Form template
    def getForUserByFormTemplate(User user, Template template) {
        // Get all tasks for user
        def tasks = getForUser(user)

        // Find tasks by template
        def task = tasks.findAll {Task it -> it.formTemplate.id == template.id}

        task
    }

    // ----------------------- Public APIs ---------------------------//
    // Methods to convert task objects to / from JSON
    def toJson(Task task, User user) {
        // Get lead for this task
        LeadM lead = leadService.getForUserByFilter(user, [ids: [task.leadid]])

        // Convert template properties to JSON
        def json = [
                id: task.id,
                publicId: task.publicId,
                lead: [id: lead.id, name: lead.name, lat: lead.latitude, lng: lead.longitude],
                manager: [id: task.manager.id, name: task.manager.name],
                rep: task.rep ? [id: task.rep.id, name: task.rep.name] : null,
                creator: [id: task.creator.id, name: task.creator.name],
                status: task.status.value,
                resolutionTime: task.resolutionTimeHrs,
                period: task.period,
                dateCreated: Constants.Formatters.LONG.format(Constants.Date.IST(task.dateCreated)),
                formTemplateId: task.formTemplate.id,
                templateId: task.templateData.template.id,
                values: []
        ]

        // Convert template values to JSON
        def values = task.templateData.values.sort {it -> it.id}
        values.each {value ->
            json.values.push([fieldId: value.field.id, value: value.value])
        }

        json
    }

    Task fromJson(def json, User user) {
        Task task = null

        // Get existing task or create new
        if (json.id) {
            task = getForUserById(user, json.id)
            if (!task) {
                throw new ApiException("Illegal access to task", Constants.HttpCodes.BAD_REQUEST)
            }
        } else {
            task = new Task(
                    account: user.account
            )
        }

        // Get manager to be assigned to task
        User rep = json.repId ? userService.getRepForUserById(user, json.repId) : null
        User manager
        if (rep) {
            manager = rep.manager
        } else if (!json.managerId || json.managerId == user.id) {
            manager = user
        } else {
            manager = userService.getManagerForUserById(user, json.managerId)
        }

        if (!manager) {
            throw new ApiException("Invalid manager assigned", Constants.HttpCodes.BAD_REQUEST)
        }

        // Set parameters from JSON
        task.manager = manager
        task.creator = user
        task.rep = rep
        task.leadid = json.leadId
        task.publicId = json.publicId ?: "-"
        task.status = TaskStatus.fromValue(json.status)
        task.period = json.period
        task.formTemplate = templateService.getForUserById(user, json.formTemplateId)

        // Prepare template data
        def template = templateService.getForUserById(user, json.templateId)
        if (!task.templateData || task.templateData.template != template) {
            task.templateData = new Data(account: user.account, owner: user, template: template)
        }

        // Prepare values
        json.values.each {valueJson ->
            Value value = valueService.fromJson(valueJson, task.templateData)

            if (!value.id) {
                task.templateData.addToValues(value)
            }
        }

        // Add date info
        if (!task.dateCreated) {
            task.dateCreated = new Date()
        }
        task.lastUpdated = new Date()

        task
    }

    // Method to get resolution time of task
    double getResolutionTime(Task task) {
        // Get elapsed time in millis
        def elapsedTimeMs = System.currentTimeMillis() - task.dateCreated.time

        // Get elapsed time in hrs
        double elapsedTimeHrs = (double) elapsedTimeMs / (double) (1000 * 60 * 60)

        // Round to 2 places and return
        Constants.round(elapsedTimeHrs, 2)
    }

    // Method to remove a task object
    def remove(User user, Task task) {
        // Remove all forms associated with this task
        def forms = formService.getForUserByTask(user, task)
        forms.each {Form form ->
            formService.remove(user, form)
        }

        // Close & Remove task
        task.status = TaskStatus.CLOSED
        task.isRemoved = true
        task.lastUpdated = new Date()
        task.save(failOnError: true, flush: true)
    }

    // ----------------------- Private APIs ---------------------------//
}
