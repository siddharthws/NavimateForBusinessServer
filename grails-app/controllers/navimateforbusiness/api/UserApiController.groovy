package navimateforbusiness.api

import grails.converters.JSON
import navimateforbusiness.ApiException
import navimateforbusiness.Constants
import navimateforbusiness.User

class UserApiController {

    def authService
    def reportService

    def changePassword() {
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Compare current password
        if (!request.JSON.oldPassword.equals(user.password)) {
            throw new ApiException("Password Validation Failed...", Constants.HttpCodes.BAD_REQUEST)
        }

        // Update password
        user.password = request.JSON.newPassword
        user.save(flush: true, failOnError: true)

        def resp = [success: true]
        render resp as JSON
    }

    def getLocationReport() {
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get and validate params
        long repId = params.long('repId')
        String date = params.selectedDate

        // Get rep from params
        def rep = User.findByAccountAndId(user.account, repId)

        // Get Report for this rep
        def resp = reportService.getLocationReport(rep, date)

        // Send response
        render resp as JSON
    }
}
