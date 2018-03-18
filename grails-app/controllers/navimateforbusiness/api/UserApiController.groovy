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
import navimateforbusiness.Visibility
import org.grails.web.json.JSONArray

class UserApiController {

    def authService
    def taskService
    def fcmService
    def reportService
    def leadService
    def templateService

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
     * Get team function is used to get the list of representatives.
     * For manager a list of representatives assigned to that particular manager will be fetched.
     * For admin a list of all the representatives available will be fetched.
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
     * This function is used by the admin or manager to add representatives.
     * The function checks if the representative is already registered else it creates representative object
     * and sends a notification to the provided mobile number
     */
    def addRep() {
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Remove '+' from phone number
        String phoneNumber = request.JSON.phoneNumber
        if (phoneNumber.contains('+')) {
            phoneNumber = phoneNumber.replace("+", "")
        }

        // Get country code and phone from full number
        String countryCode = phoneNumber.substring(0, 2)
        String phone = phoneNumber.substring(2, phoneNumber.length())

        // Check if rep is already registered in this manager's team
        User rep = User.findByPhoneAndCountryCodeAndRole(phone, countryCode, Role.REP)
        if (rep) {
            rep.name = request.JSON.name
            rep.manager = user
            rep.account = user.account
        }
        else {
            // Create New Rep Object
            rep = new User(
                    account: user.account,
                    manager: user,
                    name: request.JSON.name,
                    phone: phone,
                    countryCode: countryCode,
                    role: Role.REP)
        }
        rep.save(flush: true, failOnError: true)

        // Send either FCM or SMS to app
        if (!fcmService.notifyApp(rep.fcmId)) {
            // Send SMS to new user
            SmsHelper.SendSms(phoneNumber, user.name + " has added you to navimate. Join on https://play.google.com/store/apps/details?id=com.biz.navimate")
        }

        def resp = [success: true]
        render resp as JSON
    }

    /*
     * Get lead function is used to get leads from the database.
     * Leads are fetched according to the role of the user.
     * If user's role is admin then all leads of the company are fetched.
     * If user's role is manager then leads created by the manager are fetched.
     * After fetching the data the response is sent to the frontend in form of JSON format.
     */
    def getLead() {
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get all leads for this user
        List<Lead> leads = leadService.getForUser(user)

        //Serialize the leads into a JSON list
        def resp = leadService.getLeadData(leads)

        render resp as JSON
    }

    /*
     * Edit leads function is used to edit the leads selected
     * This function receives a JSON object of selected leads which are to be edited.
     * This function validates the JSON object and check if it contains all the mandatory
     * lead information like title, phone number, latitude, longitude and address.
     * If the lead exists edits then edits lead, if it does not exist then the function looks for
     * duplicate lead having same lead information. If it finds a duplicate lead then what lead will be
     * edited, if duplicate lead is not found a new lead is created.
     * The function checks if there are any tasks associated with the lead which is edited. If the
     * status of the task is OPEN then it sends a notification to the representative assigned to the task.
     */
    def editLeads() {
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        def leadsJson = request.JSON.leads
        def fcms = []

        // Iterate through all Lead JSONs
        leadsJson.each { leadJson ->
            // Validate mandatory lead fields
            if (!leadJson.title || !leadJson.latitude || !leadJson.longitude || !leadJson.address) {
                throw new ApiException("Mandatory lead information missing", Constants.HttpCodes.BAD_REQUEST)
            }

            // Get Lead object from JSON
            Lead lead = JsonToDomain.Lead(leadJson, user)

            // Save lead
            lead.save(flush: true, failOnError: true)

            // Check if the lead has any tasks
            def tasks = Task.findAllByLeadAndStatus(lead, TaskStatus.OPEN)
            tasks.each {task ->
                // Collect FCM Ids
                if (!fcms.contains(task.rep.fcmId)) {
                    fcms.push(task.rep.fcmId)
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

    /*
     * Remove lead function receives a JSON object from the frontend containing leads to be removed.
     * If lead is not found an error will be displayed else it will remove the Manager associated with the lead.
     * It will check if the lead has any task assigned, if the task's status is OPEN then it will
     * send notification to respective representative and change the status to CLOSED.
     * Sends a notification to all representatives after closing the tasks.
     */
    def removeLeads() {
        // Get Leads from JSON
        JSONArray leadsJson = request.JSON.leads
        leadsJson.each {leadJson ->
            Lead lead = Lead.findById(leadJson.id)
            if (!lead) {
                throw new ApiException("Lead not found", Constants.HttpCodes.BAD_REQUEST)
            }

            // Mark remove flag in lead
            lead.isRemoved = true
            lead.save(flush: true, failOnError: true)
        }

        def resp = [success: true]
        render resp as JSON
    }

    /*
     * Get Task function is used get tasks of a user from database.
     * The function gets the tasks assigned that user only.
     * It will arrange the tasks in descending order, serialize them into a JSON object and send
     * them to frontend.
     */
    def getTask() {
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get Task List
        List<Task> tasks = taskService.getForUser(user)

        //Serialize the tasks into a JSON object and send the response to frontend
        def resp = taskService.getTaskData(tasks)
        render resp as JSON
    }

    /*
     * Add task function simply adds a new task.
     * It makes use of addTasks method in TaskService.groovy file to add a new task.
     * It gets an JSON object of task information and passes it to the addTasks method to add the
     * particular task.
     */
    def addTasks() {
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Update tasks using service
        JSONArray tasksJson = request.JSON.tasks
        taskService.addTasks(user, tasksJson)

        // return resposne
        def resp = [success: true]
        render resp as JSON
    }

    /*
     * Stop task renewal function gets a JSON object of tasks.
     * It validates the task, if the task is not found error will be displayed else it sets
     * the renewal period of the task to zero.
     */
    def stopTaskRenewal() {
        // Get user
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get all tasks of this user
        def tasks = taskService.getForUser(user)

        // Iterate through all tasks that need to be stopped for renewal
        request.JSON.ids.each {id ->
            // Get task with this id
            Task task = tasks.find {it -> it.id == id}
            if (!task) {
                throw new ApiException("Task not found...", Constants.HttpCodes.BAD_REQUEST)
            }

            // Update Task Period to 0
            task.period = 0
            task.save(flush: true, failOnError: true)
        }

        // Send back response
        def resp = [success: true]
        render resp as JSON
    }

    /*
     * Close task function gets a JSON object of tasks to be closed from the frontend.
     * It validates the tasks, if not found error is displayed.
     * If the task is found then checks the status of the task. If task's status is OPEN then the
     * assigned representatives are notified and the tasks status is updated to CLOSED.
     * A notification again is sent to all the representatives regarding the closed tasks.
     */
    def closeTasks() {
        // Get user
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get all tasks of this user
        def tasks = taskService.getForUser(user)
        log.error("IDs = " + request.JSON.ids)

        // Iterate through IDs to be remove
        def fcms = []
        request.JSON.ids.each {id ->
            // Get task with this id
            Task task = tasks.find {it -> it.id == id}
            if (!task) {
                throw new ApiException("Task not found...", Constants.HttpCodes.BAD_REQUEST)
            }

            // Save rep's fcm to be used later
            if (task.status == TaskStatus.OPEN) {
                if (!fcms.contains(task.rep.fcmId)) {
                    fcms.push(task.rep.fcmId)
                }
            }

            // Update and save task
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

    def removeTasks() {
        // Get user
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get all tasks of this user
        def tasks = taskService.getForUser(user)

        // Iterate through IDs to be remove
        def fcms = []
        request.JSON.ids.each {id ->
            // Get task with this id
            Task task = tasks.find {it -> it.id == id}
            if (!task) {
                throw new ApiException("Task not found...", Constants.HttpCodes.BAD_REQUEST)
            }

            // Save rep's fcm to be used later
            if (task.status == TaskStatus.OPEN) {
                if (!fcms.contains(task.rep.fcmId)) {
                    fcms.push(task.rep.fcmId)
                }
            }

            // Update and save task
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
        int type = Integer.parseInt(params.templateType)
        List<Template> templates

        // Get List of Form templates of admin for this user
        templates = templateService.getForUser(user, type)

        // Serialize into response
        def templatesJson = []
        templates.each {template ->
            // Sort template fields in deterministic order
            def fields = template.fields.sort(false) {it.id}

            // Create Field JSON array for template
            def fieldsJson = []
            fields.each {field ->
                // Convert firld to JSON
                def fieldJson = DomainToJson.Field(field)

                //Parse value as per field type
                if (fieldJson.value) {
                    switch (field.type) {
                        case navimateforbusiness.Constants.Template.FIELD_TYPE_RADIOLIST:
                        case navimateforbusiness.Constants.Template.FIELD_TYPE_CHECKLIST:
                            fieldJson.value = JSON.parse(fieldJson.value)
                            break
                        case navimateforbusiness.Constants.Template.FIELD_TYPE_CHECKBOX:
                            fieldJson.value = Boolean.valueOf(fieldJson.value)
                            break
                    }
                }

                // Add field JSON Array
                fieldsJson.push(fieldJson)
            }

            // Create template JSon
            def templateJson = [id: template.id, name: template.name, fields: fieldsJson]

            templatesJson.push(templateJson)
        }

        def resp = [templates: templatesJson]
        render resp as JSON
    }



    def getReport() {
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get Report
        def resp = reportService.getReport(user)

        // Send response
        render resp as JSON
    }

    def getLocationReport() {
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get and validate params
        long repId = params.long('repId')
        String date = params.selectedDate

        // Get rep from params
        def rep = User.findByAccountAndId(user.account, repId)

        // Get Report for this rep
        def resp = reportService.getLocationReport(rep, date)

        // Send response
        render resp as JSON
    }
}
