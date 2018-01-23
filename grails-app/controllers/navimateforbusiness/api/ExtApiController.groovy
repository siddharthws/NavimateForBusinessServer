package navimateforbusiness.api

import grails.converters.JSON
import navimateforbusiness.Account
import navimateforbusiness.ApiException
import navimateforbusiness.ApiKey
import navimateforbusiness.Constants
import navimateforbusiness.DomainToJson
import navimateforbusiness.Data
import navimateforbusiness.Form
import navimateforbusiness.Lead
import navimateforbusiness.Role
import navimateforbusiness.Template
import navimateforbusiness.User
import navimateforbusiness.Value
import org.grails.web.json.JSONArray

class ExtApiController {

    def googleApiService

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

    def getFormReport() {
        // Get account for this request
        def account = ApiKey.findByKey(request.getHeader("X-Api-Key")).account

        // Validate to & from parameters
        if (!params.from || !(params.from instanceof long )) {
            throw new ApiException("Invalid 'from' date parameter", Constants.HttpCodes.BAD_REQUEST)
        }
        if (!params.to || !(params.to instanceof long )) {
            throw new ApiException("Invalid 'to' date parameter", Constants.HttpCodes.BAD_REQUEST)
        }

        // Get from and to dates
        Date from = new Date(params.from)
        Date to = new Date(params.to)

        // Filter data submissions
        List<Data> submissions = Data.findAll().findAll {it ->
            (it.account.id == account.id) &&        // Submission for this account
            (it.dateCreated >= from) &&             // Submission after from date
            (it.dateCreated <= to) &&               // Submissions before to date
            (it.owner.role == Role.REP)             // Submissions done by representative users
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
                    managerId: managerId,
                    userId: userId,
                    leadId: leadId,
                    taskId: taskId,
                    status: form.taskStatus.value,
                    date: submission.dateCreated.time,
                    location: [ lat: form.latitude, lng: form.longitude],
                    template: submission.template.name
            ]

            // Add submission values
            submission.values.each {value ->
                row += [(value.field.title): value.value]
            }

            // Add element to report
            report.push()
        }

        // Send success response
        def resp = [success: true]
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

            // Check for optional parameters & their data types
            if (userJson.phone && !(userJson.phone instanceof String)) {
                throw new ApiException("Invalid parameter 'phone' for user id " + userJson.id, Constants.HttpCodes.BAD_REQUEST)
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
            if (!userJson.managerId || !(userJson.managerId instanceof String)) {
                throw new ApiException("Invalid parameter 'managerId' for user id " + userJson.id, Constants.HttpCodes.BAD_REQUEST)
            }

            // Check for optional parameters & their data types
            if (userJson.email && !(userJson.email instanceof String)) {
                throw new ApiException("Invalid parameter 'email' for user id " + userJson.id, Constants.HttpCodes.BAD_REQUEST)
            }

            // Check if manager exists
            def existingManager = User.findByAccountAndExtIdAndRoleGreaterThanEquals(account, userJson.managerId, Role.MANAGER)
            if (!existingManager) {
                throw new ApiException("Unknown manager ID " + userJson.managerId + " for user " + userJson.id, Constants.HttpCodes.BAD_REQUEST)
            }

            // Check for existing registrations with this phone
            def existingUser = User.findByPhoneNumberAndRole(userJson.phone, Role.REP)
            if (existingUser) {
                // Check if user belongs to another account
                if (existingUser.account != account) {
                    throw new ApiException("Unauthorized access to User with phone " + existingUser.phoneNumber, Constants.HttpCodes.BAD_REQUEST)
                }

                // Check for ext ID conflicts
                if (existingUser.extId && existingUser.extId != userJson.id) {
                    throw new ApiException("User with phone " + existingUser.phoneNumber + " already exists with id " + existingUser.extId, Constants.HttpCodes.BAD_REQUEST)
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
            def template = Template.findByNameAndTypeAndAccount(leadJson.template, Constants.Template.TYPE_LEAD, account)
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

                    case Constants.Template.FIELD_TYPE_RADIOLIST:
                        // Values should be parseable to integer
                        int numValue
                        try {
                            numValue = Integer.parseInt(valueString)
                        } catch (Exception e) {
                            throw new ApiException("Unable to parse value for field " + key + " to integer in lead id " + leadJson.id, Constants.HttpCodes.BAD_REQUEST)
                        }

                        // Value should be less than radio list length
                        String defaultValue = template.defaultData.values.find {it -> it.field.id == field.id}.value
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
                        String defaultValue = template.defaultData.values.find {it -> it.field.id == field.id}.value
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
                                    role:       navimateforbusiness.Role.MANAGER,
                                    status:     navimateforbusiness.UserStatus.ACTIVE)
            }

            // Update user info
            user.extId          = userJson.id
            user.name           = userJson.name
            user.email          = userJson.email
            user.password       = userJson.password
            user.phoneNumber    = userJson.phone

            // Save user
            user.save(flush: true, failOnError: true)
        }
    }

    // Method to add reps to database
    private def addReps(Account account, JSONArray usersJson) {
        // Iterate through users
        usersJson.each {userJson ->
            // Get existing user by email
            def user = User.findByPhoneNumberAndRole(userJson.phone, Role.REP)

            // If user does not exist, find by ext ID
            if (!user) {
                user = User.findByAccountAndExtIdAndRole(account, userJson.id, Role.REP)
            }

            // If user not found by ext Id or Email, create a new one
            if (!user) {
                user  = new User(   account:    account,
                        role:       navimateforbusiness.Role.REP,
                        status:     navimateforbusiness.UserStatus.ACTIVE)
            }

            // Update user info
            user.extId          = userJson.id
            user.name           = userJson.name
            user.phoneNumber    = userJson.phone
            user.manager        = User.findByAccountAndExtIdAndRoleGreaterThanEquals(account, userJson.managerId, Role.MANAGER)
            user.email          = userJson.email

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
                lead  = new Lead(account:    account)
            }

            // Populate extID and title
            lead.extId          = leadJson.id
            lead.title          = leadJson.title

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
            def template = Template.findByNameAndTypeAndAccount(leadJson.template, Constants.Template.TYPE_LEAD, account)

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
                String defaultValue = template.defaultData.values.find {it -> it.field.id == field.id}.value

                // Find this field name in passed params
                if (dataJsonKeys.contains(field.title)) {
                    // Get string value
                    def valueString = leadJson.templateData[field.title]

                    // Check for value type
                    switch (field.type) {
                        case Constants.Template.FIELD_TYPE_TEXT:
                        case Constants.Template.FIELD_TYPE_NUMBER:
                        case Constants.Template.FIELD_TYPE_CHECKBOX:
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
}
