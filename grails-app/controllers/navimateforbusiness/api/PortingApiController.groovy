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

    def portPhoneNumbers () {
        List<User> users = User.findAll()

        // Iterate through users
        users.each {user ->
            // Ignore if no phone number
            if (!user.phoneNumber) {
                return
            }

            // Remove '+' from phone number
            String phoneNumber = user.phoneNumber
            if (phoneNumber.contains('+')) {
                phoneNumber = phoneNumber.replace("+", "")
            }

            // Get country code and phone from full number
            String countryCode = phoneNumber.substring(0, 2)
            String phone = phoneNumber.substring(2, phoneNumber.length())

            // Update and save user object
            user.phone = phone
            user.countryCode = countryCode
            user.save(flush: true, failOnError: true)
        }

        def resp = [success: true]
        render resp as JSON
    }
}
