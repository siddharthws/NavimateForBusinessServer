package navimateforbusiness.api

import grails.converters.JSON
import navimateforbusiness.ApiException
import navimateforbusiness.User

import javax.xml.bind.Marshaller

class UserApiController {

    def redisService

    def getMyProfile() {
        def accessToken = request.getHeader("X-Auth-Token")
        if (!accessToken) {
            throw new ApiException("Unauthorized", 401)
        }
        def sessionDataStr = redisService.get("accessToken:$accessToken")
        def sessionData
        if (sessionDataStr) {
            sessionData = JSON.parse(sessionDataStr)
        }
        if (!sessionData) {
            throw new ApiException("Unauthorized", 401)
        }
        User user = User.get(sessionData.userId)
        render navimateforbusiness.Marshaller.serializeUser(user) as JSON
    }

    def updateMyProfile() {
        //TODO:
    }
}
