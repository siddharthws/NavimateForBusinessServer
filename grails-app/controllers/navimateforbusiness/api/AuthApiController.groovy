package navimateforbusiness.api

import grails.converters.JSON
import navimateforbusiness.Account
import navimateforbusiness.ApiException
import navimateforbusiness.Role
import navimateforbusiness.User

class AuthApiController {

    def authService

    def register() {
        def input = request.JSON
        if (!input.name || !input.phoneNumber || !input.password) {
            throw new ApiException("Please check input", 400)
        }
        def existingUser = User.findByPhoneNumber(input.phoneNumber)
        if (existingUser) {
            throw new ApiException("User with phone umber already exists", 409)
        }
        authService.register(input)
        def resp = [success: true]
        render resp as JSON
    }

    def login() {

    }
}
