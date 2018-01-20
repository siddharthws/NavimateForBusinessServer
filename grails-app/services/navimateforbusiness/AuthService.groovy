package navimateforbusiness

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import org.apache.commons.lang.RandomStringUtils

@Transactional
class AuthService {

    def redisService
    def emailService

    def register(input) {
        //local variables of register
        def account
        def user

        //parsing the role as Enum
        navimateforbusiness.Role role = input.role as navimateforbusiness.Role

        //register a new admin
        if(role == navimateforbusiness.Role.ADMIN) {
            // Create and save a new account
            account = new Account(name: input.companyName)
            account.save(flush: true, failOnError: true)

            // Create and save an api key for this account
            ApiKey apiKey = new ApiKey(key: generateApiKey(), account: account)
            apiKey.save(flush: true, failOnError: true)

            // Create and save a new Admin
            user  = new User(name: input.name,
                    email: input.email,
                    password: input.password,
                    account: account,
                    role: navimateforbusiness.Role.ADMIN,
                    status: navimateforbusiness.UserStatus.ACTIVE)

            // Save
            user.save(flush: true, failOnError: true)

            // Assign admin to account
            account.admin = user
            account.save(flush: true, failOnError: true)

            // Save default templates for this user
            getDefaultTemplate(user).save(flush: true, failOnError: true)
            createDefaultLeadTemplate(user).save(flush: true, failOnError: true)
            createDefaultTaskTemplate(user).save(flush: true, failOnError: true)
        }

        //register a new manager
        else if(role == navimateforbusiness.Role.MANAGER) {
            // Open existing account
            account = Account.findByName(input.companyName)
            if(!account){
                throw new navimateforbusiness.ApiException("Company name does not exist", navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
            }

            // Create and save a new Manager
            user  = new User(name: input.name,
                    email: input.email,
                    password: input.password,
                    account: account,
                    role: navimateforbusiness.Role.MANAGER,
                    status: navimateforbusiness.UserStatus.ACTIVE)

            // Save
            user.save(flush: true, failOnError: true)
        }

        // Send invitation email
        emailService.sendMail(  user.email, "Your Navimate Credentials",
                                "\nHi " + user.name + ",\n\nThank you for registering on Navimate. You credentials are given below.\n\nEmail : " + user.email + "\nPassword: " + user.password)

        return user
    }

    def login(long id) {
        // Generate Access Token
        def accessToken = UUID.randomUUID().toString()

        // Login user
        redisService.set("accessToken:$accessToken", ([
                userId   : id,
                accessTime: System.currentTimeMillis()
        ] as JSON).toString())

        return accessToken
    }

    def logout(String accessToken) {
        redisService.del("accessToken:$accessToken")
    }

    def getUserFromAccessToken(String accessToken) {
        def sessionDataStr = redisService.get("accessToken:$accessToken")
        def sessionData = JSON.parse(sessionDataStr)
        def user = User.get(sessionData.userId)

        user
    }

    boolean authenticate(String accessToken) {
        // Validate Token
        if (!accessToken) {
            throw new navimateforbusiness.ApiException("Unauthorized", navimateforbusiness.Constants.HttpCodes.UNAUTHORIZED)
        }

        // Check if access token is logged in
        def sessionDataStr = redisService.get("accessToken:$accessToken")
        def sessionData
        if (!sessionDataStr) {
            throw new navimateforbusiness.ApiException("Unauthorized", navimateforbusiness.Constants.HttpCodes.UNAUTHORIZED)
        } else {
            sessionData = JSON.parse(sessionDataStr)
            if (!sessionData) {
                throw new navimateforbusiness.ApiException("Unauthorized", navimateforbusiness.Constants.HttpCodes.UNAUTHORIZED)
            }
        }

        // Check if user is valid
        def user = User.get(sessionData.userId)
        if (!user) {
            throw new navimateforbusiness.ApiException("Unauthorized", navimateforbusiness.Constants.HttpCodes.UNAUTHORIZED)
        }

        // Check if token is expired
        def currentTime = System.currentTimeMillis()
        def elapsedTime = currentTime - sessionData.accessTime
        if (elapsedTime > (60 * 60 * 1000)) {
            throw new navimateforbusiness.ApiException("Unauthorized", navimateforbusiness.Constants.HttpCodes.UNAUTHORIZED)
        }

        // Update token access time
        sessionData.accessTime = currentTime
        redisService.set("accessToken:$accessToken", sessionData.toString())

        true
    }

    def generateApiKey() {
        String key = ""

        // Add Label
        key += "NAVM8"

        // Add 20 random characters
        int randomStringLength = 20
        String charset = (('a'..'z') + ('A'..'Z') + ('0'..'9')).join()
        key += RandomStringUtils.random(randomStringLength, charset.toCharArray())

        key
    }

    // Needs to be updated
    private def getDefaultTemplate(User manager) {
        // Create template object
        Template defaultTemplate = new Template(
                owner: manager,
                account: manager.account,
                name: "Default",
                type: navimateforbusiness.Constants.Template.TYPE_FORM
        )

        // Create Data Object
        Data defaultData = new Data(
                owner: manager,
                account: manager.account,
                template: defaultTemplate
        )

        // Create some default fields
        Field notesField = new Field(
                account: manager.account,
                template: defaultTemplate,
                type: navimateforbusiness.Constants.Template.FIELD_TYPE_TEXT,
                title: "Notes",
                bMandatory: false
        )
        Field amountField = new Field(
                account: manager.account,
                template: defaultTemplate,
                type: navimateforbusiness.Constants.Template.FIELD_TYPE_NUMBER,
                title: "Amount",
                bMandatory: false
        )
        Field photoField = new Field(
                account: manager.account,
                template: defaultTemplate,
                type: navimateforbusiness.Constants.Template.FIELD_TYPE_PHOTO,
                title: "Photo",
                bMandatory: false
        )

        // Create values for these fields
        Value notesValue = new Value(
                account: manager.account,
                field: notesField,
                data: defaultData,
                value: ""
        )
        Value amountValue = new Value(
                account: manager.account,
                field: amountField,
                data: defaultData,
                value: String.valueOf(0)
        )
        Value photoValue = new Value(
                account: manager.account,
                field: photoField,
                data: defaultData,
                value: ""
        )

        // Add values to template
        defaultData.addToValues(notesValue)
        defaultData.addToValues(amountValue)
        defaultData.addToValues(photoValue)
        defaultTemplate.defaultData = defaultData

        // Add fields to template
        defaultTemplate.addToFields(notesField)
        defaultTemplate.addToFields(amountField)
        defaultTemplate.addToFields(photoField)

        defaultTemplate
    }

    Template createDefaultLeadTemplate(User user) {
        // Create a default Lead template
        Template template = new Template(account: user.account, owner: user, name: "Default", type: navimateforbusiness.Constants.Template.TYPE_LEAD)

        // Create default data for this template
        Data data = new Data(account: user.account, owner: user, template: template)

        // Create Fields for the template
        Field descField     = new Field(account: user.account, type: navimateforbusiness.Constants.Template.FIELD_TYPE_TEXT, title: "Description", bMandatory: false)
        Field phoneField    = new Field(account: user.account, type: navimateforbusiness.Constants.Template.FIELD_TYPE_TEXT, title: "Phone", bMandatory: false)
        Field emailField    = new Field(account: user.account, type: navimateforbusiness.Constants.Template.FIELD_TYPE_TEXT, title: "Email", bMandatory: false)

        // Create Values for the fields
        Value descValue = new Value(account: user.account, field: descField, value: "")
        Value phoneValue = new Value(account: user.account, field: phoneField, value: "")
        Value emailValue = new Value(account: user.account, field: emailField, value: "")

        // Add fields to template
        template.addToFields(descField)
        template.addToFields(phoneField)
        template.addToFields(emailField)

        // Add default data to template
        data.addToValues(descValue)
        data.addToValues(phoneValue)
        data.addToValues(emailValue)
        template.defaultData = data

        template
    }

    Template createDefaultTaskTemplate(User admin) {
        // Create a default Task template
        Template template = new Template(account: admin.account, owner: admin, name: "Default", type: navimateforbusiness.Constants.Template.TYPE_TASK)

        // Create default data for this template
        Data data = new Data(account: admin.account, owner: admin, template: template)

        // Create Fields for the template
        Field descField     = new Field(account: admin.account, type: navimateforbusiness.Constants.Template.FIELD_TYPE_TEXT, title: "Description", bMandatory: false)

        // Create Values for the fields
        Value descValue = new Value(account: admin.account, field: descField, value: "")

        // Add fields to template
        template.addToFields(descField)

        // Add default data to template
        data.addToValues(descValue)
        template.defaultData = data

        template
    }
}
