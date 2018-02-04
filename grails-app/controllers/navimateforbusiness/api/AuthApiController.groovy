package navimateforbusiness.api

import grails.converters.JSON
import navimateforbusiness.Account
import navimateforbusiness.AccountSettings
import navimateforbusiness.ApiException
import navimateforbusiness.ApiKey
import navimateforbusiness.Constants
import navimateforbusiness.DomainToJson
import navimateforbusiness.Role
import navimateforbusiness.User

class AuthApiController {

    def authService
    def emailService

    def forgotPassword() {
        // Validate user
        String email = request.JSON.email
        def user = User.findByEmailAndRole(email, Role.ADMIN) ?: User.findByEmailAndRole(email, Role.MANAGER)
        if (!user) {
            throw new ApiException("Unknown Email...", Constants.HttpCodes.BAD_REQUEST)
        }

        // Mail old password
        emailService.sendMail(user.email, "Your Navimate Password", "Your Navimate Password is " + user.password)

        def resp = [success: true]
        render resp as JSON
    }

    def register() {
        def input = request.JSON

        // Validate User
        if (!input.name || !input.email || !input.password || !input.role || !input.companyName) {
            throw new ApiException("Please check input", Constants.HttpCodes.BAD_REQUEST)
        }

        //parsing the role as Enum
        Role role = input.role as Role

        //check if company already exist for admin
        def existingCompany=Account.findByName(input.companyName)
        if(existingCompany && (role == Role.ADMIN)) {
            throw new ApiException("Company name already exists", Constants.HttpCodes.CONFLICT)
        }

        //check if the user with email already exist
        def existingUser = User.findByEmailAndRole(input.email, Role.ADMIN) ?: User.findByEmailAndRole(input.email, Role.MANAGER)
        if (existingUser) {
            throw new ApiException("User with email already exists", Constants.HttpCodes.CONFLICT)
        }

        //register the user
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
                name  : user.name,
                id: user.id,
                role : user.role.value,
                companyName : user.account.name
        ]

        // Add API Key to response for admin user only
        if (user.role == Role.ADMIN) {
            // Add API key
            ApiKey apiKey = ApiKey.findByAccount(user.account)
            resp += [apiKey: apiKey.key]

            // Add account settings
            AccountSettings settings = AccountSettings.findByAccount(user.account)
            resp += DomainToJson.AccountSettings(settings)
        }
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

    def email() {
        // Check Params
        def otp = request.JSON.otp
        def email = request.JSON.email
        if (!otp || !email) {
            throw new ApiException("Email / OTP not found...", Constants.HttpCodes.BAD_REQUEST)
        }

        // Send Mail
        def message = otp + " is your OTP for navimate verification."
        def subject = "Navimate OTP Verification"
        emailService.sendMail(email, subject, message)

        def resp = [success: true]
        render resp as JSON
    }
}
