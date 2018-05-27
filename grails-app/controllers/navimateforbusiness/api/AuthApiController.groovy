package navimateforbusiness.api

import grails.converters.JSON
import navimateforbusiness.Account
import navimateforbusiness.AccountSettings
import navimateforbusiness.util.ApiException
import navimateforbusiness.ApiKey
import navimateforbusiness.util.Constants
import navimateforbusiness.enums.Role
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

    def validateRegistration() {
        def input = request.JSON

        // Validate User
        if (!input.name || !input.email || !input.password || !input.role || !input.companyName) {
            throw new ApiException("Please check input", Constants.HttpCodes.BAD_REQUEST)
        }

        //parsing the role as Enum
        Role role = Role.fromValue(input.role)

        // Check if the user with email already exist
        def existingUser = User.findByEmail(input.email)
        if (existingUser) {
            throw new ApiException("User with email already exists", Constants.HttpCodes.CONFLICT)
        }

        // Validate company name for Admin / Manager appropriately
        def existingCompany = Account.findByName(input.companyName)
        if(existingCompany && (role == Role.ADMIN)) {
            throw new ApiException("Company name already exists", Constants.HttpCodes.CONFLICT)
        } else if ((role == Role.MANAGER || role == Role.CC) && !existingCompany) {
            throw new ApiException("Company name does not exist", Constants.HttpCodes.CONFLICT)
        }

        // generate OTP
        Random rnd = new Random()
        int otpInt = 100000 + rnd.nextInt(900000)
        String otp = String.valueOf(otpInt)

        // Send verification Email with OTP
        def message = otp + " is your OTP for navimate verification."
        def subject = "Navimate OTP Verification"
        emailService.sendMail(input.email, subject, message)

        def resp = [otp: otp]
        render resp as JSON
    }

    def register() {
        def input = request.JSON

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
            resp += [
                    startHr: settings.startHr,
                    endHr: settings.endHr,
            ]
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
}
