package navimateforbusiness.api

import grails.converters.JSON
import navimateforbusiness.ApiException
import navimateforbusiness.Constants
import navimateforbusiness.Form
import navimateforbusiness.Role
import navimateforbusiness.SmsHelper
import navimateforbusiness.Task
import navimateforbusiness.TaskStatus
import navimateforbusiness.User
import navimateforbusiness.UserStatus
import org.grails.web.json.JSONArray

class RepApiController {

    def getMyProfile() {
        // Check if the rep is registered. (Rep is registered from the dashboard)
        User rep = User.findByPhoneNumberAndRole(request.JSON.phoneNumber, Role.REP)
        if (!rep) {
            throw new ApiException("Rep not registered", Constants.HttpCodes.BAD_REQUEST)
        }

        // Return user information
        def resp = navimateforbusiness.Marshaller.serializeUser(rep)
        render resp as JSON
    }

    def register() {
        // Check if the rep is registered. (Rep is registered from the dashboard)
        User rep = User.findByPhoneNumberAndRole(request.JSON.phoneNumber, Role.REP)
        if (!rep) {
            // Create new rep object
            rep = new User( name: request.JSON.name,
                            phoneNumber: request.JSON.phoneNumber,
                            role: Role.REP,
                            status: UserStatus.ACTIVE)
        } else {
            rep.name = request.JSON.name
        }
        rep.save(failOnError: true, flush: true)

        // Check if user has been registered
        rep = User.findByPhoneNumberAndRole(request.JSON.phoneNumber, Role.REP)
        if (!rep) {
            throw new ApiException("Rep not registered", Constants.HttpCodes.BAD_REQUEST)
        }

        // Return user information
        def resp = [id: rep.id]
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

        def tasksJson = new JSONArray()
        tasks.each { task ->
            if (task.status == TaskStatus.OPEN) {
                tasksJson.add(navimateforbusiness.Marshaller.serializeTaskForRep(task))
            }
        }

        def resp = [
                "tasks" : tasksJson
        ]
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
                                owner:      rep,
                                latitude:   request.JSON.latitude,
                                longitude:  request.JSON.longitude)
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

    def sendOtpSms() {
        SmsHelper smsHelper = new SmsHelper()
        if (!smsHelper.SendSms(request.JSON.phoneNumber, request.JSON.message)) {
            throw new ApiException("Unable to send OTP SMS")
        }

        def resp = [success: true]
        render resp as JSON
    }
}
