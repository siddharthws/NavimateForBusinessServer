package navimateforbusiness

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
            // Get Rep, lead and template for the new task
            User rep = User.findById(taskJson.rep.id)
            Lead lead = Lead.findById(taskJson.lead.id)
            Form template = Form.findById(taskJson.template.id)

            // Validate JSON data
            if (!rep || !lead || !template){
                throw new navimateforbusiness.ApiException("Invalid Data", navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
            }

            // Create Task Object
            Task task = new Task(
                    account:    manager.account,
                    manager:    manager,
                    rep:        rep,
                    lead:       lead,
                    template:   template,
                    period:     taskJson.period,
                    status:     navimateforbusiness.TaskStatus.OPEN
            )
            tasks.push(task)

            // Collect FCM IDs to which notification needs to be sent
            if (!fcms.contains(rep.fcmId)) {
                fcms.add(rep.fcmId)
            }
        }

        // Send notifications to all reps
        fcms.each {fcm ->
            fcmService.notifyApp(fcm)
        }

        // Save Task Objects in database
        tasks.each {task ->
            task.save(flush: true, failOnError: true)
        }
    }
}
