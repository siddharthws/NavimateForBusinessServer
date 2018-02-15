package navimateforbusiness.api

import grails.converters.JSON
import navimateforbusiness.AccountSettings
import navimateforbusiness.ApiException
import navimateforbusiness.Constants
import navimateforbusiness.Data
import navimateforbusiness.DomainToJson
import navimateforbusiness.Field
import navimateforbusiness.Form
import navimateforbusiness.JsonToDomain
import navimateforbusiness.Lead
import navimateforbusiness.Role
import navimateforbusiness.SmsHelper
import navimateforbusiness.Task
import navimateforbusiness.TaskStatus
import navimateforbusiness.Template
import navimateforbusiness.User
import navimateforbusiness.UserStatus
import navimateforbusiness.Value

class RepApiController {

    def reportService

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
        List<Task> tasks = Task.findAllByRepAndAccount(rep, rep.account)

        // Get Sync data form request
        def syncData = request.JSON.syncData

        // Check which tasks need to be sent back to app
        def tasksJson = []
        tasks.each {task ->
            // Assume all OPEN tasks as unsynced and closed tasks as synced
            boolean bSynced = (task.status == TaskStatus.CLOSED)

            // Change sync flag as per the sync data versions
            for (int i = 0; i < syncData.size(); i++) {
                def syncObject = syncData.get(i)
                if (syncObject.id == task.id) {
                    bSynced = (syncObject.ver == task.version)
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
            Lead lead = Lead.findByIdAndAccount(syncObject.id, rep.account)

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
        User rep = authenticate()

        // Get Sync data form request
        def syncData = request.JSON.syncData

        // Get Template with this id
        def templates = Template.findAllByAccountAndIsRemoved(rep.account, false)

        // Check which templates need to be sent back to app
        def templatesJson = []
        templates.each {template ->
            boolean bSynced = false

            //Check for Version
            for (int i = 0; i < syncData.size(); i++) {
                def syncObject = syncData.get(i)
                if (syncObject.id == template.id) {
                    bSynced = (syncData.ver == template.version)
                }
            }

            // Add to response if version mismatch
            if (!bSynced) {
                templatesJson.push(DomainToJson.Template(template))
            }
        }

        // Push templates which are to be removed
        def removedTemplateIds = []
        syncData.each { syncObj ->
            // Get template by ID
            def template = Template.findByAccountAndId(rep.account, syncObj.id)

            // Check if template has been removed
            if (!template || template.isRemoved) {
                removedTemplateIds.push(template.id)
            }
        }

        // Send response
        def resp = [
                "templates" : templatesJson,
                "removedIds": removedTemplateIds
        ]
        render resp as JSON
    }

    def syncFields() {
        def id = request.getHeader("id")
        User rep = User.findById(id)
        if (!rep) {
            throw new ApiException("Unauthorized", Constants.HttpCodes.UNAUTHORIZED)
        }

        // Get Sync data form request
        def syncData = request.JSON.syncData

        // Check which templates need to be sent back to app
        def fieldsJson = []
        syncData.each {syncObject ->
            // Get lead with this id
            Field field = Field.findById(syncObject.id)

            // Add to response if version mismatch
            if (field && (field.version > syncObject.ver)) {
                fieldsJson.push(DomainToJson.Field(field))
            }
        }

        // Send response
        def resp = [
                "fields" : fieldsJson
        ]
        render resp as JSON
    }

    def syncData() {
        def id = request.getHeader("id")
        User rep = User.findById(id)
        if (!rep) {
            throw new ApiException("Unauthorized", Constants.HttpCodes.UNAUTHORIZED)
        }

        // Get Sync data form request
        def syncData = request.JSON.syncData

        // Check which templates need to be sent back to app
        def dataJson = []
        syncData.each {syncObject ->
            // Get lead with this id
            Data data = Data.findById(syncObject.id)

            // Add to response if version mismatch
            if (data && (data.version > syncObject.ver)) {
                dataJson.push(DomainToJson.Data(data))
            }
        }

        // Send response
        def resp = [
                "data" : dataJson
        ]
        render resp as JSON
    }

    def syncValues() {
        def id = request.getHeader("id")
        User rep = User.findById(id)
        if (!rep) {
            throw new ApiException("Unauthorized", Constants.HttpCodes.UNAUTHORIZED)
        }

        // Get Sync data form request
        def syncData = request.JSON.syncData

        // Check which templates need to be sent back to app
        def valuesJson = []
        syncData.each {syncObject ->
            // Get lead with this id
            Value value = Value.findById(syncObject.id)

            // Add to response if version mismatch
            if (value && (value.version > syncObject.ver)) {
                valuesJson.push(DomainToJson.Value(value))
            }
        }

        // Send response
        def resp = [
                "values" : valuesJson
        ]
        render resp as JSON
    }

    def syncForms() {
        def id = request.getHeader("id")
        User rep = User.findById(id)
        if (!rep) {
            throw new ApiException("Unauthorized", Constants.HttpCodes.UNAUTHORIZED)
        }

        // Get Sync data form request
        def formsJson = request.JSON.forms

        // Check which templates need to be sent back to app
        def formsJsonResp = []
        formsJson.each {formJson ->
            Form form = JsonToDomain.Form(formJson, rep)
            form.save(failOnError: true, flush: true)

            // Close task if required
            if (formJson.closeTask) {
                form.task.status = TaskStatus.CLOSED
                form.task.save(failOnError: true, flush: true)
            }

            // prepare response JSOn with IDs and version
            def valuesResp = []
            form.submittedData.values.each {value ->
                valuesResp.push([
                    id: value.id,
                    ver: value.version
                ])
            }
            def formResp = [
                    id: form.id,
                    ver: form.version,
                    data: [
                        id: form.submittedData.id,
                        ver: form.submittedData.version,
                        values: valuesResp
                    ]
            ]
            formsJsonResp.push(formResp)
        }

        // Send response
        def resp = [
                "forms" : formsJsonResp
        ]
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

    // API to get init data on app start
    def appStart() {
        // Get rep
        def rep = authenticate()

        // Get account settings
        def accSettings = AccountSettings.findByAccount(rep.account)

        // Return JSON response
        def resp = DomainToJson.AccountSettings(accSettings)
        render resp as JSON
    }

    def syncLocationReport() {
        // Get rep
        User rep = authenticate()

        // Get report elements from request
        def reportJson = request.JSON.report

        // Extract and save location report objects
        reportService.saveLocationReport(rep, reportJson)

        // Send success response
        def resp = [success: true]
        render resp as JSON
    }

    def addTask() {
        // Get rep
        User rep = authenticate()

        // Add rep ID to task json
        request.JSON.repId = rep.id

        // Parse to Task
        Task task = JsonToDomain.Task(request.JSON, rep.manager)
        task.save(failOnError: true, flush: true)

        // prepare response JSOn with IDs and version
        def valuesResp = []
        task.templateData.values.each {value ->
            valuesResp.push([
                    id: value.id,
                    ver: value.version
            ])
        }
        def taskResp = [
                id: task.id,
                ver: task.version,
                data: [
                        id: task.templateData.id,
                        ver: task.templateData.version,
                        values: valuesResp
                ]
        ]
        render taskResp as JSON
    }

    def addLead() {
        // Get rep
        User rep = authenticate()

        // Parse to Lead
        Lead lead = JsonToDomain.Lead(request.JSON.leadJson, rep)
        lead.save(failOnError: true, flush: true)

        // prepare response JSOn with IDs and version
        def valuesResp = []
        lead.templateData.values.each {value ->
            valuesResp.push([
                    id: value.id,
                    ver: value.version
            ])
        }
        def leadResp = [
                id: lead.id,
                ver: lead.version,
                data: [
                        id: lead.templateData.id,
                        ver: lead.templateData.version,
                        values: valuesResp
                ]
        ]
        render leadResp as JSON
    }

    private def authenticate() {
        // Get id from request
        long id = 0L
        try {
            id = Long.parseLong(request.getHeader("id"))
        } catch (Exception e) {
            id = 0
        }

        // Find representative with this ID
        User rep = User.findByRoleAndId(Role.REP, id)
        if (!rep) {
            throw new ApiException("Unauthorized", Constants.HttpCodes.UNAUTHORIZED)
        }

        rep
    }
}
