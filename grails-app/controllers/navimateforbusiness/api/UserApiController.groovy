package navimateforbusiness.api

import grails.converters.JSON
import navimateforbusiness.ApiException
import navimateforbusiness.User

import javax.xml.bind.Marshaller

class UserApiController {

    def authService

    def getMyProfile() {
        def accessToken = request.getHeader("X-Auth-Token")
        if (!accessToken) {
            throw new ApiException("Unauthorized", 401)
        }
        def user = authService.getUserFromAccessToken(accessToken)
        render navimateforbusiness.Marshaller.serializeUser(user) as JSON
    }

    def updateMyProfile() {
        def input = request.JSON
        //TODO:
    }
}
