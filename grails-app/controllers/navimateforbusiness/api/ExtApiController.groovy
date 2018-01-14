package navimateforbusiness.api

import grails.converters.JSON
import navimateforbusiness.Account
import navimateforbusiness.ApiException
import navimateforbusiness.ApiKey
import navimateforbusiness.Constants
import navimateforbusiness.DomainToJson
import navimateforbusiness.Role
import navimateforbusiness.User
import org.grails.web.json.JSONArray

class ExtApiController {

    def syncManagers() {
        // Get account for this request
        def account = ApiKey.findByKey(request.getHeader("X-Api-Key")).account

        // Validate users array
        def users = request.JSON.users
        if( !users || !DomainToJson.isJsonArrayValid(users.toString())) {
            throw new ApiException("Invalid users data. Please check input.", Constants.HttpCodes.BAD_REQUEST)
        }

        // Validate data
        if (validateManagers(account, users)) {
            // Add all users to database
            addManagers(account, users)
        }

        // Send success response
        def resp = [success: true]
        render resp as JSON
    }

    def syncReps() {
        // Get account for this request
        def account = ApiKey.findByKey(request.getHeader("X-Api-Key")).account

        // Validate users array
        def users = request.JSON.users
        if( !users || !DomainToJson.isJsonArrayValid(users.toString())) {
            throw new ApiException("Invalid users data. Please check input.", Constants.HttpCodes.BAD_REQUEST)
        }

        // Validate data
        if (validateReps(account, users)) {
            // Add all users to database
            addReps(account, users)
        }

        // Send success response
        def resp = [success: true]
        render resp as JSON
    }

    // Method to validate manager data
    private def validateManagers(Account account, JSONArray usersJson) {
        // Iterate through data
        usersJson.eachWithIndex {userJson, i ->
            // Validate JSON object type
            if (!DomainToJson.isJsonObjectValid(userJson.toString())) {
                throw new ApiException("Invalid JSON at index " + i, Constants.HttpCodes.BAD_REQUEST)
            }

            // Check for valid ID
            if (!userJson.id || !(userJson.id instanceof String) || !userJson.id.length()) {
                throw new ApiException("Invalid ID " + userJson.id + " at index " + i, Constants.HttpCodes.BAD_REQUEST)
            }

            // Check for mandatory parameters & their data types
            if (!userJson.name || !(userJson.name instanceof String)) {
                throw new ApiException("Invalid parameter 'name' for user id " + userJson.id, Constants.HttpCodes.BAD_REQUEST)
            }
            if (!userJson.email || !(userJson.email instanceof String)) {
                throw new ApiException("Invalid parameter 'email' for user id " + userJson.id, Constants.HttpCodes.BAD_REQUEST)
            }
            if (!userJson.password || !(userJson.password instanceof String)) {
                throw new ApiException("Invalid parameter 'password' for user id " + userJson.id, Constants.HttpCodes.BAD_REQUEST)
            }

            // Check for optional parameters & their data types
            if (userJson.phone && !(userJson.phone instanceof String)) {
                throw new ApiException("Invalid parameter 'phone' for user id " + userJson.id, Constants.HttpCodes.BAD_REQUEST)
            }

            // Check for existing registrations with this email
            def existingUser = User.findByEmailAndRoleGreaterThanEquals(userJson.email, Role.MANAGER)
            if (existingUser) {
                // Check if user belongs to another account
                if (existingUser.account != account) {
                    throw new ApiException("Unauthorized access to User with email " + existingUser.email, Constants.HttpCodes.BAD_REQUEST)
                }

                // Check for ext ID conflicts
                if (existingUser.extId && existingUser.extId != userJson.id) {
                    throw new ApiException("User with email " + existingUser.email + " already exists with id " + existingUser.extId, Constants.HttpCodes.BAD_REQUEST)
                }
            }
        }
    }

    // Method to validate manager data
    private def validateReps(Account account, JSONArray usersJson) {
        // Iterate through data
        usersJson.eachWithIndex {userJson, i ->
            // Validate JSON object type
            if (!DomainToJson.isJsonObjectValid(userJson.toString())) {
                throw new ApiException("Invalid JSON at index " + i, Constants.HttpCodes.BAD_REQUEST)
            }

            // Check for valid ID
            if (!userJson.id || !(userJson.id instanceof String) || !userJson.id.length()) {
                throw new ApiException("Invalid ID " + userJson.id + " at index " + i, Constants.HttpCodes.BAD_REQUEST)
            }

            // Check for mandatory parameters & their data types
            if (!userJson.name || !(userJson.name instanceof String)) {
                throw new ApiException("Invalid parameter 'name' for user id " + userJson.id, Constants.HttpCodes.BAD_REQUEST)
            }
            if (!userJson.phone || !(userJson.phone instanceof String)) {
                throw new ApiException("Invalid parameter 'phone' for user id " + userJson.id, Constants.HttpCodes.BAD_REQUEST)
            }
            if (!userJson.managerId || !(userJson.managerId instanceof String)) {
                throw new ApiException("Invalid parameter 'managerId' for user id " + userJson.id, Constants.HttpCodes.BAD_REQUEST)
            }

            // Check for optional parameters & their data types
            if (userJson.email && !(userJson.email instanceof String)) {
                throw new ApiException("Invalid parameter 'email' for user id " + userJson.id, Constants.HttpCodes.BAD_REQUEST)
            }

            // Check if manager exists
            def existingManager = User.findByExtIdAndRoleGreaterThanEquals(userJson.managerId, Role.MANAGER)
            if (!existingManager) {
                throw new ApiException("Unknown manager ID " + userJson.managerId + " for user " + userJson.id, Constants.HttpCodes.BAD_REQUEST)
            }

            // Check for existing registrations with this phone
            def existingUser = User.findByPhoneNumberAndRole(userJson.phone, Role.REP)
            if (existingUser) {
                // Check if user belongs to another account
                if (existingUser.account != account) {
                    throw new ApiException("Unauthorized access to User with phone " + existingUser.phoneNumber, Constants.HttpCodes.BAD_REQUEST)
                }

                // Check for ext ID conflicts
                if (existingUser.extId && existingUser.extId != userJson.id) {
                    throw new ApiException("User with phone " + existingUser.phoneNumber + " already exists with id " + existingUser.extId, Constants.HttpCodes.BAD_REQUEST)
                }
            }
        }
    }

    // Method to add managers to database
    private def addManagers(Account account, JSONArray usersJson) {
        // Iterate through users
        usersJson.each {userJson ->
            // Get existing user by email
            def user = User.findByEmailAndRoleGreaterThanEquals(userJson.email, Role.MANAGER)

            // If user does not exist, find by ext ID
            if (!user) {
                user = User.findByExtIdAndRoleGreaterThanEquals(userJson.id, Role.MANAGER)
            }

            // If user not found by ext Id or Email, create a new one
            if (!user) {
                user  = new User(   account:    account,
                                    role:       navimateforbusiness.Role.MANAGER,
                                    status:     navimateforbusiness.UserStatus.ACTIVE)
            }

            // Update user info
            user.extId          = userJson.id
            user.name           = userJson.name
            user.email          = userJson.email
            user.password       = userJson.password
            user.phoneNumber    = userJson.phone

            // Save user
            user.save(flush: true, failOnError: true)
        }
    }

    // Method to add reps to database
    private def addReps(Account account, JSONArray usersJson) {
        // Iterate through users
        usersJson.each {userJson ->
            // Get existing user by email
            def user = User.findByPhoneNumberAndRole(userJson.phone, Role.REP)

            // If user does not exist, find by ext ID
            if (!user) {
                user = User.findByExtIdAndRole(userJson.id, Role.REP)
            }

            // If user not found by ext Id or Email, create a new one
            if (!user) {
                user  = new User(   account:    account,
                        role:       navimateforbusiness.Role.REP,
                        status:     navimateforbusiness.UserStatus.ACTIVE)
            }

            // Update user info
            user.extId          = userJson.id
            user.name           = userJson.name
            user.phoneNumber    = userJson.phone
            user.manager        = User.findByExtIdAndRoleGreaterThanEquals(userJson.managerId, Role.MANAGER)
            user.email          = userJson.email

            // Save user
            user.save(flush: true, failOnError: true)
        }
    }
}
