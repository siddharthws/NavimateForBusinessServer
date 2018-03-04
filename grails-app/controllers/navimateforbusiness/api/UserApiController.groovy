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
        }
        rep.save(flush: true, failOnError: true)

        // Send either FCM or SMS to app
        if (!fcmService.notifyApp(rep.fcmId)) {
            // Send SMS to new user
            SmsHelper.SendSms(rep.phoneNumber, user.name + " has added you to navimate. Join on https://play.google.com/store/apps/details?id=com.biz.navimate")
        }

        def resp = [success: true]
        render resp as JSON
    }

    /*
     * Remove reps function is used to remove representatives.
     * Admin or manager select representatives to be removed.
     * Remove reps function receives a JSON object of representatives selected by admin or their manager.
     */
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
            rep.account = null
            rep.save(flush: true, failOnError: true)
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

    /*
     * Close task function gets a JSON object of tasks to be closed from the frontend.
     * It validates the tasks, if not found error is displayed.
     * If the task is found then checks the status of the task. If task's status is OPEN then the
     * assigned representatives are notified and the tasks status is updated to CLOSED.
     * A notification again is sent to all the representatives regarding the closed tasks.
     */
    def closeTasks() {
        // Get Tasks from JSON
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
        int type = Integer.parseInt(params.templateType)
        List<Template> templates

        // Get List of Form templates of admin for this user
        templates = templateService.getForUser(user, type)

        // Serialize into response
        def templatesJson = []
        templates.each {template ->
            // Create Field array and data array for template
            def fieldsJson = []
            def defaultData = [id: template.defaultData.id, values: []]
            def fields = template.fields.sort(false) {it.id}
            fields.each {field ->
                fieldsJson.push(DomainToJson.Field(field))

                // Add corresponding data
                def values = template.defaultData.values
                values.each {value ->
                    if (value.fieldId == field.id) {
                        def valueJson = DomainToJson.Value(value)
                        if (value.field.type == Constants.Template.FIELD_TYPE_RADIOLIST ||
                            value.field.type == Constants.Template.FIELD_TYPE_CHECKLIST) {
                            valueJson.value = JSON.parse(valueJson.value)
                        } else if (value.field.type == navimateforbusiness.Constants.Template.FIELD_TYPE_CHECKBOX) {
                            valueJson.value = Boolean.valueOf(valueJson.value)
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
