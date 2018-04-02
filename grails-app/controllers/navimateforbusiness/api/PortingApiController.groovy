package navimateforbusiness.api

import navimateforbusiness.User

class PortingApiController {
    def bfPhone () {
        // Fix duplicate number
        def sid = User.findById(65055)
        sid.countryCode = "919"
        sid.save(flush: true, failOnError: true)

        def sid2 = User.findById(65056)
        sid2.countryCode = "9191"
        sid2.save(flush: true, failOnError: true)

        def vish = User.findById(10)
        vish.countryCode = "919"
        vish.save(flush: true, failOnError: true)
    }
}
