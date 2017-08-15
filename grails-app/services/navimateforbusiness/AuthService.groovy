package navimateforbusiness

import grails.converters.JSON
import grails.gorm.transactions.Transactional

@Transactional
class AuthService {

    def redisService

    def register(input) {
        User user = new User(name: input.name, phoneNumber: input.phoneNumber,
                password: input.password)
        user.role = navimateforbusiness.Role.ADMIN
        user.save(flush: true, failOnError: true)
        Account account = new Account(name: input.name)
        account.admin = user
        account.save(flush: true, failOnError: true)
        user.account = account
        user.save(flush: true, failOnError: true)
        return user
    }

    def getUserFromAccessToken(String accessToken) {
        def sessionDataStr = redisService.get("accessToken:$accessToken")
        def sessionData
        if (sessionDataStr) {
            sessionData = JSON.parse(sessionDataStr)
        }
        if (!sessionData) {
            throw new navimateforbusiness.ApiException("Unauthorized", 401)
        }
        return User.get(sessionData.userId)
    }
}
