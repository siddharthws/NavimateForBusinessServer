package navimateforbusiness

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import navimateforbusiness.enums.Role
import navimateforbusiness.util.ApiException
import navimateforbusiness.util.Constants
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
        Role role = Role.fromValue(input.role)

        //register a new admin
        if(role == Role.ADMIN) {
            // Create and save a new account
            account = new Account(name: input.companyName)
            account.save(flush: true, failOnError: true)

            // Create and save account settings
            AccountSettings accSettings = new AccountSettings(account: account,
                                                              startHr: 10,
                                                              endHr: 18)
            accSettings.save(flush: true, failOnError: true)

            // Create and save an api key for this account
            ApiKey apiKey = new ApiKey(key: generateApiKey(), account: account)
            apiKey.save(flush: true, failOnError: true)

            // Create and save a new Admin
            user  = new User(name: input.name,
                    email: input.email,
                    password: input.password,
                    account: account,
                    role: Role.ADMIN)

            // Save
            user.save(flush: true, failOnError: true)

            // Assign admin to account
            account.admin = user
            account.save(flush: true, failOnError: true)

            // Save default templates for this user
            Template defFormTemplate = getDefaultTemplate(user)
            defFormTemplate.save(flush: true, failOnError: true)

            Template defLeadTemplate = createDefaultLeadTemplate(user)
            defLeadTemplate.save(flush: true, failOnError: true)

            Template defTaskTemplate = createDefaultTaskTemplate(user)
            defTaskTemplate.save(flush: true, failOnError: true)

            Template defInventoryTemplate = createDefaultInventoryTemplate(user)
            defInventoryTemplate.save(flush: true, failOnError: true)
        }

        //register a new manager
        else if(role == Role.MANAGER) {
            // Open existing account
            account = Account.findByName(input.companyName)
            if(!account){
                throw new ApiException("Company name does not exist", Constants.HttpCodes.BAD_REQUEST)
            }

            // Create and save a new Manager
            user  = new User(name: input.name,
                    email: input.email,
                    password: input.password,
                    account: account,
                    role: Role.MANAGER)

            // Save
            user.save(flush: true, failOnError: true)
        }
        else if(role == Role.CC) {
            // Open existing account
            account = Account.findByName(input.companyName)
            if(!account){
                throw new ApiException("Company name does not exist", Constants.HttpCodes.BAD_REQUEST)
            }

            // Create and save a new customer care
            user  = new User(   name: input.name,
                                email: input.email,
                                password: input.password,
                                account: account,
                                role: Role.CC)

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
            throw new ApiException("Unauthorized", Constants.HttpCodes.UNAUTHORIZED)
        }

        // Check if access token is logged in
        def sessionDataStr = redisService.get("accessToken:$accessToken")
        def sessionData
        if (!sessionDataStr) {
            throw new ApiException("Unauthorized", Constants.HttpCodes.UNAUTHORIZED)
        } else {
            sessionData = JSON.parse(sessionDataStr)
            if (!sessionData) {
                throw new ApiException("Unauthorized", Constants.HttpCodes.UNAUTHORIZED)
            }
        }

        // Check if user is valid
        def user = User.get(sessionData.userId)
        if (!user) {
            throw new ApiException("Unauthorized", Constants.HttpCodes.UNAUTHORIZED)
        }

        // Check if token is expired
        def currentTime = System.currentTimeMillis()
        def elapsedTime = currentTime - sessionData.accessTime
        if (elapsedTime > (60 * 60 * 1000)) {
            throw new ApiException("Unauthorized", Constants.HttpCodes.UNAUTHORIZED)
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
                type: Constants.Template.TYPE_FORM,
                dateCreated: new Date(),
                lastUpdated: new Date()
        )

        // Create some default fields
        Field notesField = new Field(account: manager.account, type: Constants.Template.FIELD_TYPE_TEXT, title: "Notes", bMandatory: false, value: "")
        Field amountField = new Field(account: manager.account, type: Constants.Template.FIELD_TYPE_NUMBER, title: "Amount", bMandatory: false, value: "0")
        Field photoField = new Field(account: manager.account, type: Constants.Template.FIELD_TYPE_PHOTO, title: "Photo", bMandatory: false, value: "")

        // Add fields to template
        defaultTemplate.addToFields(notesField)
        defaultTemplate.addToFields(amountField)
        defaultTemplate.addToFields(photoField)

        defaultTemplate
    }

    Template createDefaultLeadTemplate(User user) {
        // Create a default Lead template
        Template template = new Template(account: user.account, owner: user, name: "Default", type: Constants.Template.TYPE_LEAD, dateCreated: new Date(), lastUpdated: new Date())

        // Create Fields for the template
        Field descField     = new Field(account: user.account, type: Constants.Template.FIELD_TYPE_TEXT, title: "Description", bMandatory: false, value: "")
        Field phoneField    = new Field(account: user.account, type: Constants.Template.FIELD_TYPE_TEXT, title: "Phone", bMandatory: false, value: "")
        Field emailField    = new Field(account: user.account, type: Constants.Template.FIELD_TYPE_TEXT, title: "Email", bMandatory: false, value: "")

        // Add fields to template
        template.addToFields(descField)
        template.addToFields(phoneField)
        template.addToFields(emailField)

        template
    }

    Template createDefaultTaskTemplate(User admin) {
        // Create a default Task template
        Template template = new Template(account: admin.account, owner: admin, name: "Default", type: Constants.Template.TYPE_TASK, dateCreated: new Date(), lastUpdated: new Date())

        // Create Fields for the template
        Field descField     = new Field(account: admin.account, type: Constants.Template.FIELD_TYPE_TEXT, title: "Description", bMandatory: false, value: "")

        // Add fields to template
        template.addToFields(descField)

        template
    }

    Template createDefaultInventoryTemplate(User admin) {
        // Create a default Inventory template
        Template template = new Template(account: admin.account, owner: admin, name: "Default", type: Constants.Template.TYPE_INVENTORY, dateCreated: new Date(), lastUpdated: new Date())

        // Create Fields for the template
        Field unitsField            = new Field(account: admin.account, type: Constants.Template.FIELD_TYPE_NUMBER, title: "Units", bMandatory: false, value: "0")
        Field pricePerUnitField     = new Field(account: admin.account, type: Constants.Template.FIELD_TYPE_NUMBER, title: "Price Per Unit", bMandatory: false, value: "0")

        // Add fields to template
        template.addToFields(unitsField)
        template.addToFields(pricePerUnitField)

        template
    }
}
