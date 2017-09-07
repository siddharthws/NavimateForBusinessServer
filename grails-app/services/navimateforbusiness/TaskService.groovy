package navimateforbusiness

import grails.gorm.transactions.Transactional
import org.grails.web.json.JSONArray

@Transactional
class TaskService {

    def updateTasks(User manager, JSONArray tasksJson) {
        // Create Task Objects from JSONArray
        def tasks = []
        tasksJson.each {taskJson ->
            User rep = User.findById(taskJson.rep.id)
            Lead lead = Lead.findById(taskJson.lead.id)
            Form template = Form.findById(taskJson.template.id)

            // Validate JSOn data
            if (!rep || !lead || !template){
                throw new navimateforbusiness.ApiException("Invalid Data", navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
            }

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

        // Save Task Objects
        tasks.each {task ->
            task.save(flush: true, failOnError: true)
        }
    }
}
