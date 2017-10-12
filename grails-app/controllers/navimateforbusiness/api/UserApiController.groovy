package navimateforbusiness.api

import grails.converters.JSON
import navimateforbusiness.ApiException
import navimateforbusiness.Constants
import navimateforbusiness.Form
import navimateforbusiness.Lead
import navimateforbusiness.Role
import navimateforbusiness.SmsHelper
import navimateforbusiness.Task
import navimateforbusiness.TaskStatus
import navimateforbusiness.User
import navimateforbusiness.UserStatus
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject

import javax.xml.bind.Marshaller

class UserApiController {

    def authService
    def taskService
    def fcmService
    def reportService
    def leadService

    def getTeam() {
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get Team List
        List<User> team = User.findAllByManager(user)

        def resp = new JSONArray()
        team.each { member ->
            resp.add(navimateforbusiness.Marshaller.serializeUser(member))
        }
        render resp as JSON
    }

    def addRep() {
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Check if rep is already registered in this manager's team
        User rep = User.findByPhoneNumberAndRole(request.JSON.phoneNumber, Role.REP)
        if (rep) {
            rep.name = request.JSON.name
            rep.email = request.JSON.email
            rep.manager = user
            rep.account = user.account
        }
        else {
            // Create New Rep Object
            rep = new User(
                    account: user.account,
                    manager: user,
                    name: request.JSON.name,
                    email: request.JSON.email,
                    phoneNumber: request.JSON.phoneNumber,
                    role: Role.REP,
                    status: UserStatus.INACTIVE)

            // Send SMS to new user
            SmsHelper.SendSms(rep.phoneNumber, user.name + " has added you to navimate. Join on https://play.google.com/store/apps/details?id=com.navimate.business")
        }
        rep.save(flush: true, failOnError: true)

        def resp = [success: true]
        render resp as JSON
    }

    def removeReps() {
        // Get Reps from JSON
        JSONArray repsJson = JSON.parse(request.JSON.reps)
        repsJson.each {repJson ->
            User rep = User.findById(repJson.id)
            if (!rep) {
                throw new ApiException("Rep not found", Constants.HttpCodes.BAD_REQUEST)
            }

            // Remove Rep's Manager
            rep.manager = null
            rep.save(flush: true, failOnError: true)
        }

        def resp = [success: true]
        render resp as JSON
    }

    def getLead() {
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get Lead List
        List<Lead> leads = Lead.findAllByManager(user)

        def resp = new JSONArray()
        leads.each { lead ->
            resp.add(navimateforbusiness.Marshaller.serializeLead(lead))
        }
        render resp as JSON
    }

    def editLeads() {
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        def jsonLeads = JSON.parse(request.JSON.leads)
        def fcms = []
        jsonLeads.each { jsonLead ->
            // Validate mandatory lead fields
            if (!jsonLead.title || !jsonLead.phoneNumber || !jsonLead.latitude || !jsonLead.longitude || !jsonLead.address) {
                throw new ApiException("Manadaotry lead information missing", Constants.HttpCodes.BAD_REQUEST)
            }

            Lead lead = null
            if (jsonLead.id) {
                // Edit Existing lead
                lead = Lead.findById(jsonLead.id)
            } else {
                // Create new lead
                lead = new Lead(
                        account: user.account,
                        manager: user
                )
            }

            // Update Information passed from json
            lead.title       = jsonLead.title
            lead.phone      = jsonLead.phoneNumber
            lead.address    = jsonLead.address
            lead.latitude   = jsonLead.latitude
            lead.longitude  = jsonLead.longitude
            lead.description  = jsonLead.description ? jsonLead.description : ""
            lead.email      = jsonLead.email ? jsonLead.email : ""

            // Save lead
            lead.save(flush: true, failOnError: true)

            // Check if the lead has any tasks
            def tasks = Task.findAllByLead(lead)
            tasks.each {task ->
                // Send FCM notification only if task is OPEN
                if (task.status == TaskStatus.OPEN) {
                    if (!fcms.contains(task.rep.fcmId)) {
                        fcms.push(task.rep.fcmId)
                    }
                }
            }
        }

        // Send notifications to all reps
        fcms.each {fcm ->
            fcmService.notifyApp(fcm)
        }

        def resp = [success: true]
        render resp as JSON
    }

    def removeLeads() {
        // Get Leads from JSON
        def fcms = []
        JSONArray leadsJson = request.JSON.leads
        leadsJson.each {leadJson ->
            Lead lead = Lead.findById(leadJson.id)
            if (!lead) {
                throw new ApiException("Lead not found", Constants.HttpCodes.BAD_REQUEST)
            }

            // Remove Lead's Manager
            lead.manager = null
            lead.save(flush: true, failOnError: true)

            // Check if the lead has any tasks
            def tasks = Task.findAllByLead(lead)
            tasks.each {task ->
                // Send FCM notification only if task is OPEN
                if (task.status == TaskStatus.OPEN) {
                    if (!fcms.contains(task.rep.fcmId)) {
                        fcms.push(task.rep.fcmId)
                    }
                }
            }
        }

        // Send notifications to all reps
        fcms.each {fcm ->
            fcmService.notifyApp(fcm)
        }

        def resp = [success: true]
        render resp as JSON
    }

    def getTask() {
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get Form List
        List<Task> tasks = Task.findAllByManager(user)

        def resp = new JSONArray()
        tasks.each { task ->
            resp.add(navimateforbusiness.Marshaller.serializeTask(task))
        }
        render resp as JSON
    }

    def addTasks() {
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Update tasks using service
        JSONArray tasksJson = JSON.parse(request.JSON.tasks)
        taskService.addTasks(user, tasksJson)

        // return resposne
        def resp = [success: true]
        render resp as JSON
    }

    def closeTasks() {
        // Get Reps from JSON
        def fcms = []
        JSONArray tasksJson = request.JSON.tasks
        tasksJson.each {taskJson ->
            Task task = Task.findById(taskJson.id)
            if (!task) {
                throw new ApiException("Task not found", Constants.HttpCodes.BAD_REQUEST)
            }

            if (task.status != TaskStatus.CLOSED) {
                // Collect FCM ID to notify the app
                if (!fcms.contains(task.rep.fcmId)) {
                    fcms.push(task.rep.fcmId)
                }

                // Update Task Status
                task.status = TaskStatus.CLOSED
                task.save(flush: true, failOnError: true)
            }
        }

        // Send notifications to all reps
        fcms.each {fcm ->
            fcmService.notifyApp(fcm)
        }

        def resp = [success: true]
        render resp as JSON
    }

    def getForm() {
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get Form List
        List<Form> forms = Form.findAllByOwner(user)

        def resp = new JSONArray()
        forms.each { form ->
            resp.add(navimateforbusiness.Marshaller.serializeForm(form))
        }
        render resp as JSON
    }

    def editForm() {
        // Update Db's Form data
        def formJson = request.JSON.form
        Form form = Form.findById(formJson.id)
        form.data = formJson.data.toString()
        form.save(flush: true, failOnError: true)

        // return resposne
        def resp = [success: true]
        render resp as JSON
    }

    def getTeamReport() {
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get Report
        def resp = reportService.getTeamReport(user)

        // Send response
        render resp as JSON
    }

    def getLeadReport() {
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get Report
        def resp = reportService.getLeadReport(user)

        // Send response
        render resp as JSON
    }

    def uploadLeads() {
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Parse Input
        JSONArray excelJson = JSON.parse(request.JSON.excelData)
        ArrayList<Lead> leads = leadService.parseExcel(user, excelJson)

        // Send response
        def resp = new JSONArray();
        leads.each { lead ->
            resp.add(navimateforbusiness.Marshaller.serializeLead(lead))
        }
        render resp as JSON
    }
}
