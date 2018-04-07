package navimateforbusiness

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import org.grails.web.json.JSONArray

@Transactional
class TaskService {
    // ----------------------- Dependencies ---------------------------//
    def templateService
    def userService
    def leadService
    def valueService
    def fcmService

    // ----------------------- Getter APIs ---------------------------//
    // Method to get all tasks for a user
    def getForUser(User user) {
        def tasks = []

        // Get leads as per access level
        switch (user.role) {
            case navimateforbusiness.Role.ADMIN:
                // Get all unremoved tasks created of account
                tasks = Task.findAllByAccountAndIsRemoved(user.account, false)
                break

            case navimateforbusiness.Role.MANAGER:
                // Get all unremoved tasks created by this user
                tasks = Task.findAllByAccountAndIsRemovedAndManager(user.account, false, user)
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

    // ----------------------- Public APIs ---------------------------//
    // Methods to convert task objects to / from JSON
    def toJson(Task task) {
        // Convert template properties to JSON
        def json = [
                id: task.id,
                lead: [id: task.lead.id, name: task.lead.name],
                manager: [id: task.manager.id, name: task.manager.name],
                rep: task.rep ? [id: task.rep.id, name: task.rep.name] : null,
                status: task.status.value,
                period: task.period,
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
                throw new navimateforbusiness.ApiException("Illegal access to task", navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
            }
        } else {
            task = new Task(
                    account: user.account
            )
        }

        // Get manager to be assigned to task
        User manager
        if (!json.managerId || json.managerId == user.id) {
            manager = user
        } else {
            manager = userService.getManagerForUserById(user, json.managerId)
        }

        if (!manager) {
            throw new navimateforbusiness.ApiException("Invalid manager assigned", navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
        }

        // Set parameters from JSON
        task.manager = manager
        task.creator = user
        task.rep = json.repId ? userService.getRepForUserById(user, json.repId) : null
        task.lead = leadService.getForUserById(user, json.leadId)
        task.status = navimateforbusiness.TaskStatus.fromValue(json.status)
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

        task
    }

    // ----------------------- Private APIs ---------------------------//
}
