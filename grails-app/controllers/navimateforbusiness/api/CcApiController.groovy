package navimateforbusiness.api

import grails.converters.JSON

class CcApiController {

    def authService
    def userService

    def getManagers() {
        // Get user
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get all managers under this user
        def managers = userService.getManagersForUser(user)

        // Convert and send JSON response
        def resp = []
        managers.each {manager -> resp.push(userService.toJson(manager))}
        render resp as JSON
    }
}
