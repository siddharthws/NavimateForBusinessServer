package navimateforbusiness.api

import grails.converters.JSON
import navimateforbusiness.ApiException
import navimateforbusiness.Constants

class ReportApiController {

    def authService
    def reportService

    def getTeamReport() {
        def accessToken = request.getHeader("X-Auth-Token")
        if (!accessToken) {
            throw new ApiException("Unauthorized", Constants.HttpCodes.UNAUTHORIZED)
        }
        def user = authService.getUserFromAccessToken(accessToken)

        // Get Report
        def resp = reportService.getTeamReport(user)

        // Send response
        render resp as JSON
    }
}
