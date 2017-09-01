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
                                    phoneNumber:    input.phoneNumber,
                                    password:       input.password,
                                    account:        account,
                                    role:           navimateforbusiness.Role.ADMIN,
                                    status:         navimateforbusiness.UserStatus.ACTIVE)
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
}
