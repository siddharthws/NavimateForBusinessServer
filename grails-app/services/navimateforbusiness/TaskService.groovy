package navimateforbusiness

import grails.gorm.transactions.Transactional
import org.grails.web.json.JSONArray

@Transactional
class TaskService {

    def addTasks(User manager, JSONArray tasksJson) {
        def tasks = []

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
                    status:     navimateforbusiness.TaskStatus.OPEN
            )
            tasks.push(task)
        }

        // Save Task Objects in database
        tasks.each {task ->
            task.save(flush: true, failOnError: true)
        }
    }
}
