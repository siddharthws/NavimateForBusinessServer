package navimateforbusiness.api

import grails.converters.JSON

class AdminApiController {

    // Service dependencies
    def authService
    def userService

    def updateAccountSettings() {
        // Get user
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Update account settings
        userService.updateAccountSettings(user, request.JSON)

        // Send success response
        def resp = [success: true]
        render resp as JSON
    }
}
