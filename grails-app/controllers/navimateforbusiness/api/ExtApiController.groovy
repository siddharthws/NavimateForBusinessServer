package navimateforbusiness.api

/*import grails.converters.JSON
import navimateforbusiness.Account
import navimateforbusiness.ApiException
import navimateforbusiness.ApiKey
import navimateforbusiness.Constants
import navimateforbusiness.DomainToJson
import navimateforbusiness.Data
import navimateforbusiness.Form
import navimateforbusiness.Lead
import navimateforbusiness.Role
import navimateforbusiness.Task
import navimateforbusiness.TaskStatus
import navimateforbusiness.Template
import navimateforbusiness.User
import navimateforbusiness.Value
import org.grails.web.json.JSONArray

import java.text.DateFormat
import java.text.SimpleDateFormat*/

class ExtApiController {

    //def googleApiService
    //def fcmService
/*
    def syncManagers() {
        // Get account for this request
        def account = ApiKey.findByKey(request.getHeader("X-Api-Key")).account

        // Validate users array
        def users = request.JSON.users
        if( !users || !DomainToJson.isJsonArrayValid(users.toString())) {
            throw new ApiException("Invalid users data. Please check input.", Constants.HttpCodes.BAD_REQUEST)
        }

        // Validate data
        if (validateManagers(account, users)) {
            // Add all users to database
            addManagers(account, users)
        }

        // Send success response
        def resp = [success: true]
        render resp as JSON
    }

    def syncReps() {
        // Get account for this request
        def account = ApiKey.findByKey(request.getHeader("X-Api-Key")).account

        // Validate users array
        def users = request.JSON.users
        if( !users || !DomainToJson.isJsonArrayValid(users.toString())) {
            throw new ApiException("Invalid users data. Please check input.", Constants.HttpCodes.BAD_REQUEST)
        }

        // Validate data
        if (validateReps(account, users)) {
            // Add all users to database
            addReps(account, users)
        }

        // Send success response
        def resp = [success: true]
        render resp as JSON
    }

    def syncLeads() {
        // Get account for this request
        def account = ApiKey.findByKey(request.getHeader("X-Api-Key")).account

        // Validate leads array
        def leads = request.JSON.leads
        if( !leads || !DomainToJson.isJsonArrayValid(leads.toString())) {
            throw new ApiException("Invalid leads data. Please check input.", Constants.HttpCodes.BAD_REQUEST)
        }

        // Validate data
        if (validateLeads(account, leads)) {
            // Add all users to database
            addLeads(account, leads)
        }

        // Send success response
        def resp = [success: true]
        render resp as JSON
    }

    def syncTasks() {
        // Get account for this request
        def account = ApiKey.findByKey(request.getHeader("X-Api-Key")).account

        // Validate leads array
        def tasks = request.JSON.tasks
        if( !tasks || !DomainToJson.isJsonArrayValid(tasks.toString())) {
            throw new ApiException("Invalid tasks data. Please check input.", Constants.HttpCodes.BAD_REQUEST)
        }

        // Validate data
        if (validateTasks(account, tasks)) {
            // Add all users to database
            addTasks(account, tasks)
        }

        // Send success response
        def resp = [success: true]
        render resp as JSON
    }

    def getFormReport() {/*
        // Get account for this request
        def account = ApiKey.findByKey(request.getHeader("X-Api-Key")).account

        // Validate to & from parameters
        if (!params.from) {
            throw new ApiException("Invalid 'from' date parameter", Constants.HttpCodes.BAD_REQUEST)
        }
        if (!params.to) {
            throw new ApiException("Invalid 'to' date parameter", Constants.HttpCodes.BAD_REQUEST)
        }

        // Parse from and to to Long
        Long fromMs, toMs
        try {
            fromMs = Long.parseLong(params.from)
        } catch (Exception e) {
            throw new ApiException("Invalid 'from' date parameter", Constants.HttpCodes.BAD_REQUEST)
        }
        try {
            toMs = Long.parseLong(params.to)
        } catch (Exception e) {
            throw new ApiException("Invalid 'to' date parameter", Constants.HttpCodes.BAD_REQUEST)
        }

        // Get from and to dates
        Date from = new Date(fromMs)
        Date to = new Date(toMs)

        // Filter data submissions
        List<Data> submissions = Data.findAll().findAll {it ->
            (it.account.id == account.id) &&       // Submission for this account
            (it.owner.role == Role.REP) &&         // Submissions done by representative users
            (it.dateCreated >= from) &&            // Submission after from date
            (it.dateCreated <= to)                 // Submissions before to date
        }

        // Create JSON Array for report
        def report = []
        submissions.each {submission ->
            // Get form object for this submission
            Form form = Form.findBySubmittedData(submission)

            // Get all ext IDs to be sent to external server
            String managerId = submission.owner.manager.extId
            String userId = submission.owner.extId
            String leadId = form.task.lead.extId
            String taskId = form.task.extId

            // Ignore if any of ext IDs are null
            if (!managerId || !userId || !leadId || !taskId) {
                return
            }

            // Create report row for this submission
            def row = [
                    taskId: taskId,
                    managerId: managerId,
                    userId: userId,
                    leadId: leadId,
                    status: form.taskStatus.value,
                    date: submission.dateCreated.time,
                    latitude: form.latitude,
                    longitude: form.longitude,
                    template: submission.template.name,
                    templateData: [:]
            ]

            // Add submission values
            submission.values.each {value ->
                String valueString = ""
                // Parse value based on field type
                switch (value.field.type) {
                    case Constants.Template.FIELD_TYPE_TEXT:
                    case Constants.Template.FIELD_TYPE_NUMBER:
                    case Constants.Template.FIELD_TYPE_CHECKBOX:
                    case Constants.Template.FIELD_TYPE_DATE:
                        valueString = value.value
                        break
                    case Constants.Template.FIELD_TYPE_CHECKLIST:
                        def valueJson = JSON.parse(value.value)
                        valueJson.eachWithIndex {optionJson, i ->
                            if (i == 0) {
                                valueString += optionJson.selection
                            } else {
                                valueString += " " + optionJson.selection
                            }
                        }
                        break
                    case Constants.Template.FIELD_TYPE_RADIOLIST:
                        def valueJson = JSON.parse(value.value)
                        valueString = String.valueOf(valueJson.selection)
                        break
                    case Constants.Template.FIELD_TYPE_SIGN:
                    case Constants.Template.FIELD_TYPE_PHOTO:
                        if (value.value) {
                            valueString = "https://biz.navimateapp.com/#/photos?name=" + value.value
                        }
                        break
                }

                // Add key-value pair to templateData for this value
                row.templateData += [(value.field.title): valueString]
            }

            // Add element to report
            report.push(row)
        }

        // Send success response
        def resp = [report: report]
        render resp as JSON
    }

    // Method to validate manager data
    private def validateManagers(Account account, JSONArray usersJson) {
        // Iterate through data
        usersJson.eachWithIndex {userJson, i ->
            // Validate JSON object type
            if (!DomainToJson.isJsonObjectValid(userJson.toString())) {
                throw new ApiException("Invalid JSON at index " + i, Constants.HttpCodes.BAD_REQUEST)
            }

            // Check for valid ID
            if (!userJson.id || !(userJson.id instanceof String) || !userJson.id.length()) {
                throw new ApiException("Invalid ID " + userJson.id + " at index " + i, Constants.HttpCodes.BAD_REQUEST)
            }

            // Check for mandatory parameters & their data types
            if (!userJson.name || !(userJson.name instanceof String)) {
                throw new ApiException("Invalid parameter 'name' for user id " + userJson.id, Constants.HttpCodes.BAD_REQUEST)
            }
            if (!userJson.email || !(userJson.email instanceof String)) {
                throw new ApiException("Invalid parameter 'email' for user id " + userJson.id, Constants.HttpCodes.BAD_REQUEST)
            }
            if (!userJson.password || !(userJson.password instanceof String)) {
                throw new ApiException("Invalid parameter 'password' for user id " + userJson.id, Constants.HttpCodes.BAD_REQUEST)
            }

            // Check for existing registrations with this email
            def existingUser = User.findByEmailAndRoleGreaterThanEquals(userJson.email, Role.MANAGER)
            if (existingUser) {
                // Check if user belongs to another account
                if (existingUser.account != account) {
                    throw new ApiException("Unauthorized access to User with email " + existingUser.email, Constants.HttpCodes.BAD_REQUEST)
                }

                // Check for ext ID conflicts
                if (existingUser.extId && existingUser.extId != userJson.id) {
                    throw new ApiException("User with email " + existingUser.email + " already exists with id " + existingUser.extId, Constants.HttpCodes.BAD_REQUEST)
                }
            }
        }
    }

    // Method to validate manager data
    private def validateReps(Account account, JSONArray usersJson) {
        // Iterate through data
        usersJson.eachWithIndex {userJson, i ->
            // Validate JSON object type
            if (!DomainToJson.isJsonObjectValid(userJson.toString())) {
                throw new ApiException("Invalid JSON at index " + i, Constants.HttpCodes.BAD_REQUEST)
            }

            // Check for valid ID
            if (!userJson.id || !(userJson.id instanceof String) || !userJson.id.length()) {
                throw new ApiException("Invalid ID " + userJson.id + " at index " + i, Constants.HttpCodes.BAD_REQUEST)
            }

            // Check for mandatory parameters & their data types
            if (!userJson.name || !(userJson.name instanceof String)) {
                throw new ApiException("Invalid parameter 'name' for user id " + userJson.id, Constants.HttpCodes.BAD_REQUEST)
            }
            if (!userJson.phone || !(userJson.phone instanceof String)) {
                throw new ApiException("Invalid parameter 'phone' for user id " + userJson.id, Constants.HttpCodes.BAD_REQUEST)
            }
            if (!userJson.countryCode || !(userJson.countryCode instanceof String)) {
                userJson.countryCode = "91"
            }
            if (!userJson.managerId || !(userJson.managerId instanceof String)) {
                throw new ApiException("Invalid parameter 'managerId' for user id " + userJson.id, Constants.HttpCodes.BAD_REQUEST)
            }

            // Add default country code to phone number if no country code was sent
            if (!userJson.phone.contains('+')) {
                userJson.phone = "+91" + userJson.phone
            }

            // Check if manager exists
            def existingManager = User.findByAccountAndExtIdAndRoleGreaterThanEquals(account, userJson.managerId, Role.MANAGER)
            if (!existingManager) {
                throw new ApiException("Unknown manager ID " + userJson.managerId + " for user " + userJson.id, Constants.HttpCodes.BAD_REQUEST)
            }

            // Check for existing registrations with this phone
            def existingUser = User.findByPhoneAndCountryCodeAndRole(userJson.phone, userJson.countryCode, Role.REP)
            if (existingUser) {
                // Check if user belongs to another account
                if (existingUser.account && existingUser.account != account) {
                    throw new ApiException("Unauthorized access to User with phone " + existingUser.phone, Constants.HttpCodes.BAD_REQUEST)
                }

                // Check for ext ID conflicts
                if (existingUser.extId && existingUser.extId != userJson.id) {
                    throw new ApiException("User with phone " + existingUser.phone + " already exists with id " + existingUser.extId, Constants.HttpCodes.BAD_REQUEST)
                }
            }
        }
    }

    // Method to validate leads data
    private def validateLeads(Account account, JSONArray leadsJson) {
        // Iterate through data
        leadsJson.eachWithIndex {leadJson, i ->
            // Validate JSON object type
            if (!DomainToJson.isJsonObjectValid(leadJson.toString())) {
                throw new ApiException("Invalid JSON at index " + i, Constants.HttpCodes.BAD_REQUEST)
            }

            // Check for valid ID
            if (!leadJson.id || !(leadJson.id instanceof String) || !leadJson.id.length()) {
                throw new ApiException("Invalid ID " + leadJson.id + " at index " + i, Constants.HttpCodes.BAD_REQUEST)
            }

            // Check for mandatory parameters & their data types
            if (!leadJson.title || !(leadJson.title instanceof String)) {
                throw new ApiException("Invalid parameter 'title' for lead id " + leadJson.id, Constants.HttpCodes.BAD_REQUEST)
            }
            if (!leadJson.address || !(leadJson.address instanceof String)) {
                throw new ApiException("Invalid parameter 'address' for lead id " + leadJson.id, Constants.HttpCodes.BAD_REQUEST)
            }
            if (!leadJson.template || !(leadJson.template instanceof String)) {
                throw new ApiException("Invalid parameter 'template' for lead id " + leadJson.id, Constants.HttpCodes.BAD_REQUEST)
            }
            if (!leadJson.templateData || !DomainToJson.isJsonObjectValid(leadJson.templateData.toString())) {
                throw new ApiException("Invalid parameter 'templateData' for lead id " + leadJson.id, Constants.HttpCodes.BAD_REQUEST)
            }

            // Check for optional parameters & their data types
            if (leadJson.ownerId && !(leadJson.ownerId instanceof String)) {
                throw new ApiException("Invalid parameter 'ownerId' for lead id " + leadJson.id, Constants.HttpCodes.BAD_REQUEST)
            }

            // Check if owner exists
            if (leadJson.ownerId) {
                def owner = User.findByExtIdAndAccountAndRoleGreaterThanEquals(leadJson.ownerId, account, Role.MANAGER)
                if (!owner) {
                    throw new ApiException("Owner ID " + leadJson.ownerId + " not found for lead id " + leadJson.id, Constants.HttpCodes.BAD_REQUEST)
                }
            }

            // Check if template exists
            def template = Template.findByAccountAndNameAndType(account, leadJson.template, Constants.Template.TYPE_LEAD)
            if (!template) {
                throw new ApiException("Template " + leadJson.template + " not found for lead id " + leadJson.id, Constants.HttpCodes.BAD_REQUEST)
            }

            // Validate template data
            def templateKeys = leadJson.templateData.keySet()
            templateKeys.each {key ->
                // Ensure passed value is string
                if (leadJson.templateData[key] && !(leadJson.templateData[key] instanceof String)) {
                    throw new ApiException("Invalid value for field " + key + " in lead id " + leadJson.id, Constants.HttpCodes.BAD_REQUEST)
                }

                // Ensure that field by this name exists in templaye
                def field = template.fields.find {it -> it.title == key}
                if (!field) {
                    throw new ApiException("Field " + key + " not found in template " + template.name + " for lead id " + leadJson.id, Constants.HttpCodes.BAD_REQUEST)
                }

                // Validate value type against field type
                def valueString = leadJson.templateData[key]
                switch (field.type) {
                    case Constants.Template.FIELD_TYPE_NUMBER:
                        // Values should be parseable to long
                        long numValue
                        try {
                            numValue = Long.parseLong(valueString)
                        } catch (Exception e) {
                            throw new ApiException("Unable to parse value for field " + key + " to long in lead id " + leadJson.id, Constants.HttpCodes.BAD_REQUEST)
                        }
                        break

                    case Constants.Template.FIELD_TYPE_DATE:
                        // Ensure date is in correct format
                        try {
                            SimpleDateFormat df = new SimpleDateFormat('dd-MM-yyyy')
                            Date date = df.parse(valueString)
                        } catch (Exception e) {
                            // Bad date formatting
                            throw new ApiException("Unable to parse value for field " + key + " to date (dd-MM-yyyy) in lead id " + leadJson.id, Constants.HttpCodes.BAD_REQUEST)
                        }
                        break

                    case Constants.Template.FIELD_TYPE_RADIOLIST:
                        // Values should be parseable to integer
                        int numValue
                        try {
                            numValue = Integer.parseInt(valueString)
                        } catch (Exception e) {
                            throw new ApiException("Unable to parse value for field " + key + " to integer in lead id " + leadJson.id, Constants.HttpCodes.BAD_REQUEST)
                        }

                        // Value should be less than radio list length
                        String defaultValue = field.value
                        def defValueJson = JSON.parse(defaultValue)
                        if (numValue >= defValueJson.options.length() || numValue < 0) {
                            throw new ApiException("Invalid index " + numValue + " for field " + key + " in lead id " + leadJson.id, Constants.HttpCodes.BAD_REQUEST)
                        }
                        break

                    case Constants.Template.FIELD_TYPE_CHECKLIST:
                        // Split array of booleans
                        ArrayList<String> selections
                        try {
                            selections = valueString.split ()
                        } catch (Exception e) {
                            throw new ApiException("Unable to parse values for field " + key + " to booleans in lead id " + leadJson.id, Constants.HttpCodes.BAD_REQUEST)
                        }

                        // Ensure each selection is a boolean string
                        selections.each {selection ->
                            if ((selection != 'true') && (selection != 'false')) {
                                throw new ApiException("Invalid checklist value for field " + key + " in lead id " + leadJson.id, Constants.HttpCodes.BAD_REQUEST)
                            }
                        }

                        // Ensure number of booleans are equal to default value length
                        String defaultValue = field.value
                        def defValueJson = JSON.parse(defaultValue)
                        if (defValueJson.length() != selections.size()) {
                            throw new ApiException("Mismatch in number of items for field " + key + " in lead id " + leadJson.id, Constants.HttpCodes.BAD_REQUEST)
                        }
                        break
                    case Constants.Template.FIELD_TYPE_CHECKBOX:
                        // Value should be true or false
                        if ((valueString != 'true') && (valueString != 'false')) {
                            throw new ApiException("Invalid checkbox value for field " + key + " in lead id " + leadJson.id, Constants.HttpCodes.BAD_REQUEST)
                        }
                        break
                }

            }
        }
    }

    // Method to validate leads data
    private def validateTasks(Account account, JSONArray tasksJson) {
        // Iterate through data
        tasksJson.eachWithIndex {taskJson, i ->
            // Validate JSON object type
            if (!DomainToJson.isJsonObjectValid(taskJson.toString())) {
                throw new ApiException("Invalid JSON at index " + i, Constants.HttpCodes.BAD_REQUEST)
            }

            // Check for valid ID
            if (!taskJson.id || !(taskJson.id instanceof String) || !taskJson.id.length()) {
                throw new ApiException("Invalid ID " + taskJson.id + " at index " + i, Constants.HttpCodes.BAD_REQUEST)
            }

            // Check for mandatory parameters & their data types
            if (!taskJson.leadId || !(taskJson.leadId instanceof String)) {
                throw new ApiException("Invalid parameter 'leadId' for id " + taskJson.id, Constants.HttpCodes.BAD_REQUEST)
            }
            if (!taskJson.formTemplate || !(taskJson.formTemplate instanceof String)) {
                throw new ApiException("Invalid parameter 'formTemplate' for id " + taskJson.id, Constants.HttpCodes.BAD_REQUEST)
            }
            if (!taskJson.taskTemplate || !(taskJson.taskTemplate instanceof String)) {
                throw new ApiException("Invalid parameter 'taskTemplate' for id " + taskJson.id, Constants.HttpCodes.BAD_REQUEST)
            }
            if (!taskJson.templateData || !DomainToJson.isJsonObjectValid(taskJson.templateData.toString())) {
                throw new ApiException("Invalid parameter 'templateData' for id " + taskJson.id, Constants.HttpCodes.BAD_REQUEST)
            }

            // Check for optional parameters & their data types
            if (taskJson.managerId && !(taskJson.managerId instanceof String)) {
                throw new ApiException("Invalid parameter 'managerId' for id " + taskJson.id, Constants.HttpCodes.BAD_REQUEST)
            }
            if (taskJson.repId && !(taskJson.repId instanceof String)) {
                throw new ApiException("Invalid parameter 'repId' for id " + taskJson.id, Constants.HttpCodes.BAD_REQUEST)
            }
            if (taskJson.status && !(taskJson.status instanceof Integer)) {
                throw new ApiException("Invalid parameter 'status' for id " + taskJson.id, Constants.HttpCodes.BAD_REQUEST)
            }
            if (taskJson.peiod && !(taskJson.period instanceof Integer)) {
                throw new ApiException("Invalid parameter 'period' for id " + taskJson.id, Constants.HttpCodes.BAD_REQUEST)
            }

            // Check if manager exists
            if (taskJson.managerId) {
                def manager = User.findByExtIdAndAccountAndRoleGreaterThanEquals(taskJson.managerId, account, Role.MANAGER)
                if (!manager) {
                    throw new ApiException("Manager ID " + taskJson.managerId + " not found for task id " + taskJson.id, Constants.HttpCodes.BAD_REQUEST)
                }
            }

            // Check if rep exists
            if (taskJson.repId) {
                def rep = User.findByExtIdAndAccountAndRole(taskJson.repId, account, Role.REP)
                if (!rep) {
                    throw new ApiException("Rep ID " + taskJson.repId + " not found for task id " + taskJson.id, Constants.HttpCodes.BAD_REQUEST)
                }
            }

            // Check if lead exists
            if (taskJson.leadId) {
                def lead = Lead.findByExtIdAndAccount(taskJson.leadId, account)
                if (!lead) {
                    throw new ApiException("Lead ID " + taskJson.leadId + " not found for task id " + taskJson.id, Constants.HttpCodes.BAD_REQUEST)
                }
            }

            // Ensure status is valid
            if (taskJson.status) {
                if (taskJson.status != 1 && taskJson.status != 2) {
                    throw new ApiException("Status " + taskJson.status + " is not valid for task id " + taskJson.id, Constants.HttpCodes.BAD_REQUEST)
                }
            }

            // Check if templates exists
            def formTemplate = Template.findByAccountAndNameAndType(account, taskJson.formTemplate, Constants.Template.TYPE_FORM)
            if (!formTemplate) {
                throw new ApiException("Form Template " + taskJson.formTemplate + " not found for task id " + taskJson.id, Constants.HttpCodes.BAD_REQUEST)
            }

            def taskTemplate = Template.findByAccountAndNameAndType(account, taskJson.taskTemplate, Constants.Template.TYPE_TASK)
            if (!taskTemplate) {
                throw new ApiException("Task Template " + taskJson.taskTemplate + " not found for task id " + taskJson.id, Constants.HttpCodes.BAD_REQUEST)
            }

            // Validate template data
            def templateKeys = taskJson.templateData.keySet()
            templateKeys.each {key ->
                // Ensure passed value is string
                if (taskJson.templateData[key] && !(taskJson.templateData[key] instanceof String)) {
                    throw new ApiException("Invalid value for field " + key + " in task id " + taskJson.id, Constants.HttpCodes.BAD_REQUEST)
                }

                // Ensure that field by this name exists in template
                def field = taskTemplate.fields.find {it -> it.title == key}
                if (!field) {
                    throw new ApiException("Field " + key + " not found in template " + taskTemplate.name + " for task id " + taskJson.id, Constants.HttpCodes.BAD_REQUEST)
                }

                // Validate value type against field type
                def valueString = taskJson.templateData[key]
                switch (field.type) {
                    case Constants.Template.FIELD_TYPE_NUMBER:
                        // Values should be parseable to long
                        long numValue
                        try {
                            numValue = Long.parseLong(valueString)
                        } catch (Exception e) {
                            throw new ApiException("Unable to parse value for field " + key + " to long in task id " + taskJson.id, Constants.HttpCodes.BAD_REQUEST)
                        }
                        break

                    case Constants.Template.FIELD_TYPE_DATE:
                        // Ensure date is in correct format
                        try {
                            SimpleDateFormat df = new SimpleDateFormat('dd-MM-yyyy')
                            Date date = df.parse(valueString)
                        } catch (Exception e) {
                            // Bad date formatting
                            throw new ApiException("Unable to parse value for field " + key + " to date (dd-MM-yyyy) in task id " + taskJson.id, Constants.HttpCodes.BAD_REQUEST)
                        }
                        break

                    case Constants.Template.FIELD_TYPE_RADIOLIST:
                        // Values should be parseable to integer
                        int numValue
                        try {
                            numValue = Integer.parseInt(valueString)
                        } catch (Exception e) {
                            throw new ApiException("Unable to parse value for field " + key + " to integer in task id " + taskJson.id, Constants.HttpCodes.BAD_REQUEST)
                        }

                        // Value should be less than radio list length
                        String defaultValue = field.value
                        def defValueJson = JSON.parse(defaultValue)
                        if (numValue >= defValueJson.options.length() || numValue < 0) {
                            throw new ApiException("Invalid index " + numValue + " for field " + key + " in task id " + taskJson.id, Constants.HttpCodes.BAD_REQUEST)
                        }
                        break

                    case Constants.Template.FIELD_TYPE_CHECKLIST:
                        // Split array of booleans
                        ArrayList<String> selections
                        try {
                            selections = valueString.split ()
                        } catch (Exception e) {
                            throw new ApiException("Unable to parse values for field " + key + " to booleans in task id " + taskJson.id, Constants.HttpCodes.BAD_REQUEST)
                        }

                        // Ensure each selection is a boolean string
                        selections.each {selection ->
                            if ((selection != 'true') && (selection != 'false')) {
                                throw new ApiException("Invalid checklist value for field " + key + " in task id " + taskJson.id, Constants.HttpCodes.BAD_REQUEST)
                            }
                        }

                        // Ensure number of booleans are equal to default value length
                        String defaultValue = field.value
                        def defValueJson = JSON.parse(defaultValue)
                        if (defValueJson.length() != selections.size()) {
                            throw new ApiException("Mismatch in number of items for field " + key + " in task id " + taskJson.id, Constants.HttpCodes.BAD_REQUEST)
                        }
                        break
                    case Constants.Template.FIELD_TYPE_CHECKBOX:
                        // Value should be true or false
                        if ((valueString != 'true') && (valueString != 'false')) {
                            throw new ApiException("Invalid checkbox value for field " + key + " in task id " + taskJson.id, Constants.HttpCodes.BAD_REQUEST)
                        }
                        break
                }

            }
        }
    }

    // Method to add managers to database
    private def addManagers(Account account, JSONArray usersJson) {
        // Iterate through users
        usersJson.each {userJson ->
            // Get existing user by email
            def user = User.findByEmailAndRoleGreaterThanEquals(userJson.email, Role.MANAGER)

            // If user does not exist, find by ext ID
            if (!user) {
                user = User.findByAccountAndExtIdAndRoleGreaterThanEquals(account, userJson.id, Role.MANAGER)
            }

            // If user not found by ext Id or Email, create a new one
            if (!user) {
                user  = new User(   account:    account,
                                    role:       navimateforbusiness.Role.MANAGER)
            }

            // Update user info
            user.extId          = userJson.id
            user.name           = userJson.name
            user.email          = userJson.email
            user.password       = userJson.password

            // Save user
            user.save(flush: true, failOnError: true)
        }
    }

    // Method to add reps to database
    private def addReps(Account account, JSONArray usersJson) {
        // Iterate through users
        usersJson.each {userJson ->
            // Get existing user by phone number
            def user = User.findByPhoneAndCountryCodeAndRole(userJson.phone, userJson.countryCode, Role.REP)

            // If user does not exist, find by ext ID
            if (!user) {
                user = User.findByAccountAndExtIdAndRole(account, userJson.id, Role.REP)
            }

            // If user not found by ext Id or Email, create a new one
            if (!user) {
                user  = new User(   role:       navimateforbusiness.Role.REP)
            }

            // Update user info
            user.extId          = userJson.id
            user.account        = account
            user.name           = userJson.name
            user.phone          = userJson.phone
            user.countryCode    = userJson.countryCode
            user.manager        = User.findByAccountAndExtIdAndRoleGreaterThanEquals(account, userJson.managerId, Role.MANAGER)

            // Save user
            user.save(flush: true, failOnError: true)
        }
    }

    // Method to add reps to database
    private def addLeads(Account account, JSONArray leadsJson) {
        // Iterate through users
        leadsJson.each {leadJson ->
            // Get existing lead by ext ID
            def lead = Lead.findByExtIdAndAccount(leadJson.id, account)

            // If lead not found by ext Id, create a new one
            if (!lead) {
                lead  = new Lead(account:    account,
                                 visibility: navimateforbusiness.Visibility.PUBLIC)
            }

            // Populate extID, title and isRemoved Status
            lead.extId          = leadJson.id
            lead.name           = leadJson.name
            lead.isRemoved      = false

            // Populate owner
            if (leadJson.ownerId) {
                lead.manager = User.findByExtIdAndAccountAndRoleGreaterThanEquals(leadJson.ownerId, account, Role.MANAGER)
            } else {
                lead.manager = account.admin
            }

            // Populate address and latlng
            lead.address          = leadJson.address
            String[] addresses = [lead.address]
            def latlngs = googleApiService.geocode(addresses)
            lead.latitude          = latlngs[0].latitude
            lead.longitude         = latlngs[0].longitude

            // Get template to be used
            def template = Template.findByAccountAndNameAndType(account, leadJson.template, Constants.Template.TYPE_LEAD)

            // Check if lead has an existing data object
            Data templateData = lead.templateData
            if (!templateData) {
                // Create new data object
                templateData = new Data(account: account)
            }

            // Update owner and template
            templateData.owner = lead.manager
            templateData.template = template

            // Get values
            def dataJsonKeys = leadJson.templateData.keySet()
            def values = []
            template.fields.each {field ->
                // Check if values for this field exists in template Data
                Value value = templateData.values.find {it -> it.field.id == field.id}

                // Create new value if not existing
                if (!value) {
                    value = new Value(account: account, data: templateData, field: field)
                }

                // Get default value from template for this field
                String defaultValue = field.value

                // Find this field name in passed params
                if (dataJsonKeys.contains(field.title)) {
                    // Get string value
                    def valueString = leadJson.templateData[field.title]

                    // Check for value type
                    switch (field.type) {
                        case Constants.Template.FIELD_TYPE_TEXT:
                        case Constants.Template.FIELD_TYPE_NUMBER:
                        case Constants.Template.FIELD_TYPE_CHECKBOX:
                        case Constants.Template.FIELD_TYPE_DATE:
                            value.value = valueString
                            break

                        case Constants.Template.FIELD_TYPE_RADIOLIST:
                            // Parse to number
                            int selectionIndex = Integer.parseInt(valueString)

                            // Parse default value to JSON
                            def defValueJson = JSON.parse(defaultValue)

                            // Update selection index
                            defValueJson.selection = selectionIndex

                            // Save value as string
                            value.value = defValueJson.toString()
                            break

                        case Constants.Template.FIELD_TYPE_CHECKLIST:
                            // Get array of booleans
                            ArrayList<String> selections = valueString.split ()

                            // Parse default value as JSON
                            def defValueJson = JSON.parse(defaultValue)

                            // Iterate through array and update default values
                            selections.eachWithIndex {selection, i ->
                                defValueJson[i].selection = Boolean.valueOf(selection)
                            }

                            // Save value as string
                            value.value = defValueJson
                            break
                    }
                } else if (!value.id) {
                    // Assign default value if value was not pre-populated
                    value.value = defaultValue
                }

                // Add to values
                values.push(value)
            }
            templateData.values = values
            lead.templateData = templateData

            // Save lead
            lead.save(flush: true, failOnError: true)
        }
    }

    // Method to add reps to database
    private def addTasks(Account account, JSONArray tasksJson) {
        def fcms = []

        // Iterate through users
        tasksJson.each {taskJson ->
            // Get existing lead by ext ID
            def task = Task.findByExtIdAndAccount(taskJson.id, account)

            // If lead not found by ext Id, create a new one
            if (!task) {
                task  = new Task(account:    account)
            }

            // Populate extID, title and isRemoved Status
            task.extId          = taskJson.id
            task.period         = taskJson.period ? taskJson.period : 0
            task.isRemoved      = false

            // Populate lead
            task.lead = Lead.findByExtIdAndAccount(taskJson.leadId, account)

            // Populate manager
            if (taskJson.managerId) {
                task.manager = User.findByExtIdAndAccountAndRoleGreaterThanEquals(taskJson.managerId, account, Role.MANAGER)
            } else {
                task.manager = account.admin
            }

            // Populate Rep
            if (taskJson.repId) {
                task.rep = User.findByExtIdAndAccountAndRole(taskJson.repId, account, Role.REP)

                // Add FCM ID to send notification
                if (!fcms.contains(task.rep.fcmId)) {
                    fcms.push(task.rep.fcmId)
                }
            }

            // Add task status
            if (taskJson.status) {
                task.status = (taskJson.status == 1) ? TaskStatus.OPEN : TaskStatus.CLOSED
            } else {
                task.status = TaskStatus.OPEN
            }

            // Get template to be used
            task.formTemplate = Template.findByAccountAndNameAndType(account, taskJson.formTemplate, Constants.Template.TYPE_FORM)
            def taskTemplate = Template.findByAccountAndNameAndType(account, taskJson.taskTemplate, Constants.Template.TYPE_TASK)

            // Check if lead has an existing data object
            Data templateData = task.templateData
            if (!templateData) {
                // Create new data object
                templateData = new Data(account: account)
            }

            // Update owner and template
            templateData.owner = task.manager
            templateData.template = taskTemplate

            // Get values
            def dataJsonKeys = taskJson.templateData.keySet()
            def values = []
            taskTemplate.fields.each {field ->
                // Check if values for this field exists in template Data
                Value value = templateData.values.find {it -> it.field.id == field.id}

                // Create new value if not existing
                if (!value) {
                    value = new Value(account: account, data: templateData, field: field)
                }

                // Get default value from template for this field
                String defaultValue = field.value

                // Find this field name in passed params
                if (dataJsonKeys.contains(field.title)) {
                    // Get string value
                    def valueString = taskJson.templateData[field.title]

                    // Check for value type
                    switch (field.type) {
                        case Constants.Template.FIELD_TYPE_TEXT:
                        case Constants.Template.FIELD_TYPE_NUMBER:
                        case Constants.Template.FIELD_TYPE_CHECKBOX:
                        case Constants.Template.FIELD_TYPE_DATE:
                            value.value = valueString
                            break

                        case Constants.Template.FIELD_TYPE_RADIOLIST:
                            // Parse to number
                            int selectionIndex = Integer.parseInt(valueString)

                            // Parse default value to JSON
                            def defValueJson = JSON.parse(defaultValue)

                            // Update selection index
                            defValueJson.selection = selectionIndex

                            // Save value as string
                            value.value = defValueJson.toString()
                            break

                        case Constants.Template.FIELD_TYPE_CHECKLIST:
                            // Get array of booleans
                            ArrayList<String> selections = valueString.split ()

                            // Parse default value as JSON
                            def defValueJson = JSON.parse(defaultValue)

                            // Iterate through array and update default values
                            selections.eachWithIndex {selection, i ->
                                defValueJson[i].selection = Boolean.valueOf(selection)
                            }

                            // Save value as string
                            value.value = defValueJson
                            break
                    }
                } else if (!value.id) {
                    // Assign default value if value was not pre-populated
                    value.value = defaultValue
                }

                // Add to values
                values.push(value)
            }
            templateData.values = values
            task.templateData = templateData

            // Save lead
            task.save(flush: true, failOnError: true)
        }

        // Send Notifications
        fcms.each {fcm ->
            fcmService.notifyApp(fcm)
        }
    }*/
}
