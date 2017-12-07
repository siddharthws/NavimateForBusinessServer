package navimateforbusiness.api

import grails.converters.JSON
import navimateforbusiness.ApiException
import navimateforbusiness.Constants
import navimateforbusiness.DomainToJson
import navimateforbusiness.Form
import navimateforbusiness.Lead
import navimateforbusiness.Role
import navimateforbusiness.SmsHelper
import navimateforbusiness.Task
import navimateforbusiness.TaskStatus
import navimateforbusiness.User
import navimateforbusiness.UserStatus

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

    def syncTasks() {
        def id = request.getHeader("id")
        User rep = User.findById(id)
        if (!rep) {
            throw new ApiException("Unauthorized", Constants.HttpCodes.UNAUTHORIZED)
        }

        // Get Task List
        List<Task> tasks = Task.findAllByRepAndManagerAndStatus(rep, rep.manager, TaskStatus.OPEN)

        // Get Sync data form request
        def syncData = request.JSON.syncData

        // Check which tasks need to be sent back to app
        def tasksJson = []
        tasks.each {task ->
            // Mark task as synced if version and id are same
            boolean bSynced = false
            for (int i = 0; i < syncData.size(); i++) {
                def syncObject = syncData.get(i)
                if ((syncObject.id == task.id) && (syncObject.ver == task.version)) {
                    bSynced = true
                    break
                }
            }

            // If unsynced, add to JSON response
            if (!bSynced) {
                tasksJson.push(DomainToJson.Task(task))
            }
        }

        // Send response
        def resp = [
                "tasks" : tasksJson
        ]
        render resp as JSON
    }

    def syncLeads() {
        def id = request.getHeader("id")
        User rep = User.findById(id)
        if (!rep) {
            throw new ApiException("Unauthorized", Constants.HttpCodes.UNAUTHORIZED)
        }

        // Get Sync data form request
        def syncData = request.JSON.syncData

        // Check which tasks need to be sent back to app
        def leadsJson = []
        syncData.each {syncObject ->
            // Get lead with this id
            Lead lead = Lead.findByIdAndManager(syncObject.id, rep.manager)

            // Add to response if version mismatch
            if (lead && (lead.version > syncObject.ver)) {
                leadsJson.push(DomainToJson.Lead(lead))
            }
        }

        // Send response
        def resp = [
                "leads" : leadsJson
        ]
        render resp as JSON
    }

    def syncTemplates() {
        def id = request.getHeader("id")
        User rep = User.findById(id)
        if (!rep) {
            throw new ApiException("Unauthorized", Constants.HttpCodes.UNAUTHORIZED)
        }

        // Get Sync data form request
        def syncData = request.JSON.syncData

        // Check which templates need to be sent back to app
        def templatesJson = []
        syncData.each {syncObject ->
            // Get lead with this id
            Form form = Form.findByIdAndOwner(syncObject.id, rep.manager)

            // Add to response if version mismatch
            if (form && (form.version > syncObject.ver)) {
                templatesJson.push(DomainToJson.Form(form))
            }
        }

        // Send response
        def resp = [
                "templates" : templatesJson
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
        def latitude = request.JSON.latitude ? request.JSON.latitude : 0
        def longitude = request.JSON.longitude ? request.JSON.longitude : 0
        Form form = new Form(   name:       task.template.name,
                                task:       task,
                                account:    rep.account,
                                data:       data,
                                owner:      rep,
                                latitude:   latitude,
                                longitude:  longitude)
        form.save(flush: true, failOnErorr: true)

        // Update task status if required
        if (request.JSON.closeTask) {
            task.status = TaskStatus.CLOSED
            task.save(flush: true, failOnErorr: true)
        }

        def resp = [id: form.id, ver: form.version]
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
