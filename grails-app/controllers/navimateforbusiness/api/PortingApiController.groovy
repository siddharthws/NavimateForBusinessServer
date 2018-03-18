package navimateforbusiness.api

import grails.converters.JSON
import navimateforbusiness.Role
import navimateforbusiness.User

class PortingApiController {

    def portUserContact() {
        // Iterate through all user
        User.findAll().each {user ->
            switch (user.role) {
                case Role.REP:
                    // Mark email as null
                    user.email = null
                    break
                case Role.MANAGER:
                case Role.ADMIN:
                    // Mark phone number as null
                    user.phoneNumber = null
                    break
            }

            // Save user
            user.save(failOnError: true, flush: true)
        }

        def resp = [success: true]
        render resp as JSON
    }
}
