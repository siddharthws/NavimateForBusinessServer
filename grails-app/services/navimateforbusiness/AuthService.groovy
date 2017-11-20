package navimateforbusiness

import grails.converters.JSON
import grails.gorm.transactions.Transactional

@Transactional
class AuthService {

    def redisService
    def emailService

    def register(input) {
        // Create and save a new account
        Account account = new Account(name: input.name)
        account.save(flush: true, failOnError: true)

        // Create and save a new Admin user
        User manager = new User(    name:           input.name,
                                    email:          input.email,
                                    password:       input.password,
                                    account:        account,
                                    role:           navimateforbusiness.Role.ADMIN,
                                    status:         navimateforbusiness.UserStatus.ACTIVE)

        // Add Sales Template By Default
        manager.addToForms(getDefaultTemplate(manager))

        // Save
        manager.save(flush: true, failOnError: true)

        // Assign admin to account
        account.admin = manager
        account.save(flush: true, failOnError: true)

        // Send invitation email
        emailService.sendMail(  manager.email, "Your Navimate Credentials",
                                "\nHi " + manager.name + ",\n\nThank you for registering on Navimate. You credentials are given below.\n\nEmail : " + manager.email + "\nPassword: " + manager.password)

        return manager
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

    private def getDefaultTemplate(User manager) {
        Form defaultTemplate = new Form(
                account: manager.account,
                owner: manager,
                name: "Default Template",
                data:('[{"title":"Amount", "type":"number", "value":"0"},' +
                       '{"title":"Notes", "type":"text", "value":"" },' +
                       '{"title":"Photo", "type":"photo", "value":""},' +
                       '{"title":"Sign", "type":"signature", "value":""},' +
                       '{"title":"Status", "type":"radioList", "value":{"options": ["Failed",' +
                                                                                   '"Waiting",' +
                                                                                   '"Done"],' +
                                                                       '"selection": "Waiting"}},' +
                       '{"title":"To-Do", "type":"checkList", "value":{"options":["Meet Client",' +
                                                                                 '"Collect Information"],' +
                                                                      '"selection":["false", "false"]}}]')
        )

        defaultTemplate
    }
}
