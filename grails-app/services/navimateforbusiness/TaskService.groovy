package navimateforbusiness

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import org.grails.web.json.JSONArray

@Transactional
class TaskService {

    def fcmService

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
                    cId:            task.account.id + String.format("%08d", task.id),
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
