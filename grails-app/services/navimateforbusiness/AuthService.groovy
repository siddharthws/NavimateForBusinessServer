package navimateforbusiness

import grails.gorm.transactions.Transactional

@Transactional
class AuthService {

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
}
