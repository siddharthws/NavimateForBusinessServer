package navimateforbusiness.api

import grails.converters.JSON
import navimateforbusiness.util.ApiException
import navimateforbusiness.util.Constants

class UserApiController {

    def authService
    def reportService
    def formService
    def userService

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

        // Get date from params
        String rawDate = params.selectedDate
        Date date = Constants.Formatters.LONG.parse(rawDate)
        Date dateOnly = new Date(date.time).clearTime()

        // Get rep from params
        long repId = params.long('repId')
        def rep = userService.getRepForUserById(user, repId)

        // Get Report for this rep
        def report = reportService.getLocationReport(rep, dateOnly)

        // Add forms submitted by rep on this date with valid latlng
        def forms = formService.getForUser(rep)
        forms = forms.findAll {it.dateCreated.clearTime() == dateOnly}
        forms = forms.findAll {it.latitude || it.longitude}
        def formResp = forms.collect {[id: it.id, lat: it.latitude, lng: it.longitude]}

        // Send response
        def resp = [report: report, forms: formResp]
        render resp as JSON
    }
}
