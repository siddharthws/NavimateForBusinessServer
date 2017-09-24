package navimateforbusiness.api

import grails.converters.JSON
import navimateforbusiness.ApiException
import navimateforbusiness.Constants
import navimateforbusiness.Form
import navimateforbusiness.Role
import navimateforbusiness.Task
import navimateforbusiness.TaskStatus
import navimateforbusiness.User
import org.grails.web.json.JSONArray

class RepApiController {

    def getMyProfile() {
        def phoneNumber = request.getHeader("phoneNumber")

        // Check if the rep is registered. (Rep is registered from the dashboard)
        User rep = User.findByPhoneNumberAndRole(phoneNumber, Role.REP)
        if (!rep) {
            throw new ApiException("Rep not registered", Constants.HttpCodes.BAD_REQUEST)
        }

        // Return user information
        def resp = navimateforbusiness.Marshaller.serializeUser(rep)
        render resp as JSON
    }

    def getTasks() {
        def id = request.getHeader("id")
        User rep = User.findById(id)
        if (!rep) {
            throw new ApiException("Unauthorized", Constants.HttpCodes.UNAUTHORIZED)
        }

        // Get Task List
        List<Task> tasks = Task.findAllByRep(rep)

        def resp = new JSONArray()
        tasks.each { task ->
            if (task.status == TaskStatus.OPEN) {
                resp.add(navimateforbusiness.Marshaller.serializeTaskForRep(task))
            }
        }
        render resp as JSON
    }

    def submitForm() {
        def id = request.getHeader("id")
        User rep = User.findById(id)
        if (!rep) {
            throw new ApiException("Unauthorized", Constants.HttpCodes.UNAUTHORIZED)
        }

        // Validate Task
        def task = Task.findById(request.JSON.taskId)
        if (!task) {
            throw new ApiException("Task not found", Constants.HttpCodes.BAD_REQUEST)
        }

        // Add form to DB
        def data = request.JSON.data.toString()
        Form form = new Form(   name:       task.template.name,
                                task:       task,
                                account:    rep.account,
                                data:       data,
                                owner:      rep)
        form.save(flush: true, failOnErorr: true)

        // Update task status if required
        if (request.JSON.closeTask) {
            task.status = TaskStatus.CLOSED
            task.save(flush: true, failOnErorr: true)
        }

        def resp = [success: true]
        render resp as JSON
    }

    def updateFcm() {
        def id = request.getHeader("id")
        User rep = User.findById(id)
        if (!rep) {
            throw new ApiException("Unauthorized", Constants.HttpCodes.UNAUTHORIZED)
        }

        // Update User FCM
        rep.fcmId = request.JSON.fcmId
        rep.save(flush: true, failOnErorr: true)

        def resp = [success: true]
        render resp as JSON
    }
}
