package navimateforbusiness

import grails.converters.JSON
import grails.gorm.transactions.Transactional

@Transactional
class AuthService {

    def redisService

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
        manager.addToForms(getSalesTemplate(manager))

        // Save
        manager.save(flush: true, failOnError: true)

        // Assign admin to account
        account.admin = manager
        account.save(flush: true, failOnError: true)

        return manager
    }

    def login(long id) {
        // Generate Access Token
        def accessToken = UUID.randomUUID().toString()

        // Login user
        redisService.set("accessToken:$accessToken", ([
                userId   : id,
                loginTime: new Date()
        ] as JSON).toString())

        return accessToken
    }

    def logout(String accessToken) {
        redisService.del("accessToken:$accessToken")
    }

    def getUserFromAccessToken(String accessToken) {
        def sessionDataStr = redisService.get("accessToken:$accessToken")
        def sessionData
        if (sessionDataStr) {
            sessionData = JSON.parse(sessionDataStr)
        }
        if (!sessionData) {
            throw new navimateforbusiness.ApiException("Unauthorized", navimateforbusiness.Constants.HttpCodes.UNAUTHORIZED)
        }
        return User.get(sessionData.userId)
    }

    private def getSalesTemplate(User manager) {
        Form salesTemplate = new Form(
                account: manager.account,
                owner: manager,
                name: "Sales Template",
                data: '[ { "title":  "Sales", "type":   "number", "value":  "0" }, { "title":  "Notes", "type":   "text", "value":  "" }, { "title":  "status", "type":   "radioList", "value":  { "options": ["Failed", "Waiting", "Done"], "selection": "Waiting" } }]'
        )

        salesTemplate
    }
}
