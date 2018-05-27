package navimateforbusiness.api

import grails.converters.JSON
import navimateforbusiness.AccountSettings
import navimateforbusiness.util.ApiException
import navimateforbusiness.util.Constants
import navimateforbusiness.Form
import navimateforbusiness.LeadM
import navimateforbusiness.enums.Role
import navimateforbusiness.util.SmsHelper
import navimateforbusiness.Task
import navimateforbusiness.enums.TaskStatus
import navimateforbusiness.Template
import navimateforbusiness.User

class RepApiController {

    def reportService
    def formService
    def taskService
    def leadService
    def templateService

    def register() {
        // Remove '+' from phone number
        String phoneNumber = request.JSON.phoneNumber
        if (phoneNumber.contains('+')) {
            phoneNumber = phoneNumber.replace("+", "")
        }

        // Get country code and phone from full number
        String countryCode = phoneNumber.substring(0, 2)
        String phone = phoneNumber.substring(2, phoneNumber.length())

        // Check if the rep is registered. (Rep is registered from the dashboard)
        User rep = User.findByPhoneAndCountryCodeAndRole(phone, countryCode, Role.REP)
        if (!rep) {
            // Create new rep object
            rep = new User( name: "NA",
                            phone: phone,
                            countryCode: countryCode,
                            role: Role.REP)
            rep.save(failOnError: true, flush: true)
        }

        // Return user information
        def resp = [id: rep.id, name: rep.name]
        render resp as JSON
    }

    def sync () {
        User rep = authenticate()

        def resp = [templates: [templates: [], remove: []],
                    leads: [leads: [], remove: []],
                    tasks: [tasks: [], remove: []]]

        if (rep.account) {
            Date lastSyncTime = new Date(request.JSON.lastSyncTime)
            String lastSyncTimeString = lastSyncTime.format(Constants.Date.FORMAT_LONG)

            // Fill Template Response
            def templates = templateService.getForUserAfterLastUpdated(rep, lastSyncTime)
            // Find all templates updated after sync date
            templates.each {Template template ->
                if (template.isRemoved) {
                    resp.templates.remove.push(template.id)
                } else {
                    resp.templates.templates.push(templateService.toJson(template))
                }
            }

            // Iterate through all tasks of this rep
            def leads = []
            taskService.getForUserRemoved(rep).each {Task task ->
                // Check if lead was updated after sync time
                LeadM lead = leadService.getForUserByIdRemoved(rep, task.leadid)
                if (task.dateCreated > lastSyncTime) {
                    if (!leads.contains(lead)) {leads.push(lead)}
                } else if (lead.updateTime > lastSyncTimeString) {
                    // Remove Lead / Add JSON as required
                    if (lead.isRemoved) {
                        resp.leads.remove.push(lead.id)
                    } else {
                        if (!leads.contains(lead)) {leads.push(lead)}
                    }
                }

                // Check if task was updated after sync time
                if (task.lastUpdated > lastSyncTime) {
                    // Remove Lead / Add JSON as required
                    if (task.isRemoved) {
                        resp.tasks.remove.push(task.id)
                    } else {
                        resp.tasks.tasks.push(taskService.toJson(task, rep))
                    }
                }
            }

            leads.each {LeadM it -> resp.leads.leads.push(leadService.toJson(it, rep))}
        }

        render resp as JSON
    }

    def syncForms() {
        def rep = authenticate()

        // Report error for reps under no account
        if (!rep.account) {
            throw new ApiException("Not registered with any account for form submission", Constants.HttpCodes.BAD_REQUEST)
        }

        def idResp = []
        request.JSON.forms.each {def formJson ->
            Form form = formService.fromJson(formJson, rep)
            form.save(failOnError: true, flush: true)

            // Close task if required
            if (formJson.closeTask && form.task.status == TaskStatus.OPEN) {
                form.task.status = TaskStatus.CLOSED
                form.task.lastUpdated = new Date()
                form.task.save(failOnError: true, flush: true)
            }

            // Add Form IS to response JSON
            idResp.push(form.id)
        }

        // Send response
        def resp = [
                "forms" : idResp
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

    def updateName() {
        def id = request.getHeader("id")
        User rep = User.findById(id)
        if (!rep) {
            throw new ApiException("Unauthorized", Constants.HttpCodes.UNAUTHORIZED)
        }

        // Update User FCM
        rep.name = request.JSON.name
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
        def resp = [startHr: 0, endHr: 0]
        if (rep.account) {
            def accSettings = AccountSettings.findByAccount(rep.account)
            resp.startHr = accSettings.startHr
            resp.endHr = accSettings.endHr
        }

        render resp as JSON
    }

    def syncLocationReport() {
        // Get rep
        User rep = authenticate()

        // Report error for reps under no account
        if (!rep.account) {
            throw new ApiException("Not registered with any account for form submission", Constants.HttpCodes.BAD_REQUEST)
        }

        // Get report elements from request
        def reportJson = request.JSON.report

        // Extract and save location report objects
        reportService.saveLocationReport(rep, reportJson)

        // Send success response
        def resp = [success: true]
        render resp as JSON
    }

    def addTask() {
        /*// Get rep
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
        render taskResp as JSON*/
        throw new ApiException("API Unavailable")
    }

    def addLead() {
        /*// Get rep
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
        render leadResp as JSON*/
        throw new ApiException("API Unavailable")
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
