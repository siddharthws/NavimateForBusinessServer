package navimateforbusiness.api

import grails.converters.JSON
import navimateforbusiness.Account
import navimateforbusiness.ApiException
import navimateforbusiness.Constants
import navimateforbusiness.Role
import navimateforbusiness.User

class AuthApiController {

    def authService

    def register() {
        def input = request.JSON

        // Validate User
        if (!input.name || !input.email || !input.password) {
            throw new ApiException("Please check input", Constants.HttpCodes.BAD_REQUEST)
        }
        def existingUser = User.findByEmailAndRole(input.email, Role.ADMIN)
        if (existingUser) {
            throw new ApiException("Admin with email already exists", Constants.HttpCodes.CONFLICT)
        }

        // Register User
        authService.register(input)

        // Send response
        def resp = [success: true]
        render resp as JSON
    }

    def login() {
        def input = request.JSON
        User user = User.findByEmailAndPassword(input.email, input.password)
        if (!user) {
            throw new ApiException("Invalid email or password", Constants.HttpCodes.UNAUTHORIZED)
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
