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

        // Get all unremoved tasks created by this user
        tasks = Task.findAllByAccountAndIsRemovedAndManager(user.account, false, user)

        // Sort tasks in descending order of create date
        tasks = tasks.sort {it -> it.dateCreated}
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
        def values = task.templateData.values
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
        task.rep = userService.getRepForUserById(user, json.repId)
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

    // ----------------------- Unclean APIs ---------------------------//
    def addTasks(User manager, JSONArray tasksJson) {
        def tasks = []
        def fcms = []

        // Create Task Objects from JSONArray
        tasksJson.each {taskJson ->
            // Parse JSOn to task Object
            Task task = navimateforbusiness.JsonToDomain.Task(taskJson, manager)

            // Add to array
            tasks.push(task)

            // Collect FCM IDs to which notification needs to be sent
            if (!fcms.contains(task.rep.fcmId)) {
                fcms.add(task.rep.fcmId)
            }
        }

        // Save Task Objects in database
        tasks.each {task ->
            task.save(flush: true, failOnError: true)
        }

        // Send notifications to all reps
        fcms.each {fcm ->
            fcmService.notifyApp(fcm)
        }
    }

    def getTaskData (List<Task> tasks) {
        def tasksJson = []

        tasks.each {task ->
            // Create lead JSON object
            def taskJson = [
                    id:             task.id,
                    cId:            "T" + String.format("%08d", task.id),
                    repId:          task.rep ? task.rep.id : -1,
                    leadId:         task.lead.id,
                    period:         task.period,
                    status:         task.status.name(),
                    formTemplateId: task.formTemplate.id,
                    templateId:     task.templateData.template.id,
                    templateData: [
                            id: task.templateData.id,
                            values: []
                    ]
            ]

            // Add values to templated data
            def values = task.templateData.values.sort {it -> it.id}
            values.each {value ->

                def val = value.value
                if (value.field.type == navimateforbusiness.Constants.Template.FIELD_TYPE_RADIOLIST ||
                    value.field.type == navimateforbusiness.Constants.Template.FIELD_TYPE_CHECKLIST) {
                    val = JSON.parse(value.value)
                } else if (value.field.type == navimateforbusiness.Constants.Template.FIELD_TYPE_CHECKBOX) {
                    val = Boolean.valueOf(value.value)
                }

                taskJson.templateData.values.push([
                        id: value.id,
                        fieldId: value.fieldId,
                        value: val
                ])
            }

            // Add to JSON Array
            tasksJson.push(taskJson)
        }

        tasksJson
    }
}
