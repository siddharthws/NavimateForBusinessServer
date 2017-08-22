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

        // Validate User
        if (!input.name || !input.phoneNumber || !input.password) {
            throw new ApiException("Please check input", 400)
        }
        def existingUser = User.findByPhoneNumber(input.phoneNumber)
        if (existingUser) {
            throw new ApiException("User with phone umber already exists", 409)
        }

        // Register User
        authService.register(input)

        // Send response
        def resp = [success: true]
        render resp as JSON
    }

    def login() {
        def input = request.JSON
        User user = User.findByPhoneNumberAndPassword(input.phoneNumber, input.password)
        if (!user) {
            throw new ApiException("Invalid phone number or password", 401)
        }
        // log the user in
        def accessToken = authService.login(user.id)

        // Send response
        def resp = [
                accessToken: accessToken,
                name  : user.name
        ]
        render resp as JSON
    }

    def logout() {
        def accessToken = request.getHeader("X-Auth-Token")
        if (accessToken) {
            authService.logout(accessToken)
        }
        def resp = [success: true]
        render resp as JSON
    }
}
