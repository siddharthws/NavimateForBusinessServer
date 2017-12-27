package navimateforbusiness.api

import grails.converters.JSON
import navimateforbusiness.ApiException
import navimateforbusiness.Constants
import navimateforbusiness.Data
import navimateforbusiness.DomainToJson
import navimateforbusiness.JsonToDomain
import navimateforbusiness.Lead
import navimateforbusiness.Role
import navimateforbusiness.SmsHelper
import navimateforbusiness.Task
import navimateforbusiness.TaskStatus
import navimateforbusiness.Template
import navimateforbusiness.User
import navimateforbusiness.UserStatus
import org.grails.web.json.JSONArray

class UserApiController {

    def authService
    def taskService
    def fcmService
    def reportService
    def leadService

    def changePassword() {
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Compare current password
        if (!request.JSON.oldPassword.equals(user.password)) {
            throw new ApiException("Password Validation Failed...", Constants.HttpCodes.BAD_REQUEST)
        }

        // Update password
        user.password = request.JSON.newPassword
        user.save(flush: true, failOnError: true)

        def resp = [success: true]
        render resp as JSON
    }

    /*
     * Get team function is used to get the List of Representatives.
     * For Manager a list of Representatives assigned to that particular Manager will be fetched.
     * For Admin a list of all the Representatives available will be fetched.
     */
    def getTeam() {
        List<User> team
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        if(user.role==navimateforbusiness.Role.ADMIN) {
             //Get Team List of Admin
             team = User.findAllByAccountAndRoleAndManagerIsNotNull(user.account, Role.REP)
        }
        else {
             //Get Team List of Manager
             team = User.findAllByManager(user)
        }

        //Serialize the members into a JSON object and send the response to frontend
        def resp = new JSONArray()
        team.each { member ->
            resp.add(navimateforbusiness.Marshaller.serializeUser(member))
        }
        render resp as JSON
    }

    /*
     * This function is used by the Admin or Manager to add Representatives.
     * The function checks if the representative is already registered else it creates Representative object
     * and sends a notification to the provided  mobile number
     */
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
            SmsHelper.SendSms(rep.phoneNumber, user.name + " has added you to navimate. Join on https://play.google.com/store/apps/details?id=com.biz.navimate")
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
        List<Lead> leads
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        if(user.role==navimateforbusiness.Role.ADMIN) {
            //Get Lead List of Admin
            leads = Lead.findAllByAccountAndManagerIsNotNull(user.account)
        }
        else {
            // Get Lead List of Manager
            leads = Lead.findAllByManager(user)
        }
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
                // check for duplicate lead in database
                lead = Lead.findByManagerAndTitleAndDescriptionAndPhoneAndAddressAndEmail(user,jsonLead.title,jsonLead.description,jsonLead.phoneNumber,jsonLead.address,jsonLead.email)
                if(!lead) {
                    // Create new lead
                    lead = new Lead(
                            account: user.account,
                            manager: user
                    )
                }
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
                    // Push user to FCM
                    if (!fcms.contains(task.rep.fcmId)) {
                        fcms.push(task.rep.fcmId)
                    }

                    // Close and save task
                    task.status = TaskStatus.CLOSED
                    task.save(failOnError: true, flush: true)
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

        // Get Task List
        List<Task> tasks = Task.findAllByManagerAndIsRemoved(user, false)

        // Sort in descending order of ID
        tasks.sort(true) {-it.id}

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

    def stopTaskRenewal() {
        // Get Tasks from JSON
        JSONArray tasksJson = request.JSON.tasks
        tasksJson.each {taskJson ->
            // Validate Task
            Task task = Task.findById(taskJson.id)
            if (!task) {
                throw new ApiException("Task not found", Constants.HttpCodes.BAD_REQUEST)
            }

            // Update Task Period to 0
            task.period = 0
            task.save(flush: true, failOnError: true)
        }

        // Send back response
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

    def removeTasks() {
        // Get Reps from JSON
        def fcms = []
        JSONArray tasksJson = request.JSON.tasks
        tasksJson.each {taskJson ->
            Task task = Task.findById(taskJson.id)
            if (!task) {
                throw new ApiException("Task not found", Constants.HttpCodes.BAD_REQUEST)
            }

            // Save rep's fcm to be used later
            if (task.status != TaskStatus.CLOSED) {
                if (!fcms.contains(task.rep.fcmId)) {
                    fcms.push(task.rep.fcmId)
                }
            }

            task.isRemoved = true
            task.status = TaskStatus.CLOSED
            task.save(flush: true, failOnError: true)
        }

        // Send notifications to all reps
        fcms.each {fcm ->
            fcmService.notifyApp(fcm)
        }

        def resp = [success: true]
        render resp as JSON
    }

    def getTemplates() {
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))
        int type = Integer.parseInt(request.getHeader("templateType"))
        List<Template> templates

        // Get List of Form templates of admin for this user
        templates = Template.findAllByOwnerAndTypeAndIsRemoved(user, type, false)
        if(user.role==navimateforbusiness.Role.MANAGER) {
           // Get List of Form templates for this manager
           templates.addAll(Template.findAllByOwnerAndTypeAndIsRemoved(user.account.admin, type, false))
        }

        // Serialize into response
        def templatesJson = []
        templates.each {template ->
            // Create Field array and data array for template
            def fieldsJson = []
            def defaultData = [id: template.defaultData.id, values: []]
            template.fields.each {field ->
                fieldsJson.push(DomainToJson.Field(field))

                // Add corresponding data
                def values = template.defaultData.values.sort(false) {it.id}
                values.each {value ->
                    if (value.fieldId == field.id) {
                        def valueJson = DomainToJson.Value(value)
                        if (value.field.type == Constants.Template.FIELD_TYPE_RADIOLIST ||
                                value.field.type == Constants.Template.FIELD_TYPE_CHECKLIST) {
                            valueJson.value = JSON.parse(valueJson.value)
                        }
                        defaultData.values.push(valueJson)
                    }
                }
            }

            // Create template JSon
            def templateJson = [id: template.id, name: template.name, fields: fieldsJson, defaultData: defaultData]

            templatesJson.push(templateJson)
        }

        def resp = [templates: templatesJson]
        render resp as JSON
    }

    def saveTemplate() {
        def manager = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Parse to Template Object
        Template template = JsonToDomain.Template(request.JSON.template, manager)

        if (!template.id) {
            // For new templates, save needs to be called twice
            Data defaultData = template.defaultData

            // Save template (default data will become null due to nullable constraint)
            template.save(flush: true, failOnError: true)

            // Save template with default data
            template.defaultData = defaultData
            template.save(flush: true, failOnError: true)
        } else {
            // Save Template Object
            template.save(flush: true, failOnError: true)
        }

        // Get open tasks that are affected due to this template
        def openTasks = []
        switch (template.type) {
            case Constants.Template.TYPE_FORM :
                openTasks = Task.findAllByFormTemplateAndStatus(template, TaskStatus.OPEN)
                break
        }

        // Collect FCM Ids of affected reps
        def fcms = []
        openTasks.each {task ->
            if (!fcms.contains(task.rep.fcmId)) {
                fcms.push(task.rep.fcmId)
            }
        }

        // Send notifications to all reps
        fcms.each {fcm ->
            fcmService.notifyApp(fcm)
        }

        // return resposne
        def resp = [success: true]
        render resp as JSON
    }

    def removeTemplates() {
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get Templates from JSON
        JSONArray templateIdsJson = request.JSON.templateIds
        templateIdsJson.each {templateId ->
            // Get Template
            Template template = Template.findById(templateId)
            if (!template) {
                throw new ApiException("Template not found...", Constants.HttpCodes.BAD_REQUEST)
            }

            // Check if this user owns this template
            if (template.owner != user) {
                throw new ApiException("Not enough privilege to remove " + template.name + "...", Constants.HttpCodes.BAD_REQUEST)
            }

            // Remove Template
            template.isRemoved = true
            template.save(flush: true, failOnError: true)
        }

        def resp = [success: true]
        render resp as JSON
    }

    def getReport() {
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get Report
        def resp = reportService.getReport(user)

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
