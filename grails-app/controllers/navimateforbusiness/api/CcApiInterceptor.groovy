package navimateforbusiness.api

import navimateforbusiness.ApiException
import navimateforbusiness.Constants
import navimateforbusiness.Role


class CcApiInterceptor {

    def authService

    boolean before() {
        // Authenticate access token
        authService.authenticate(request.getHeader("X-Auth-Token"))

        // Check permission level
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))
        if (user.role < Role.CC) {
            throw new ApiException("Unauthorized access from non cc account...", Constants.HttpCodes.UNAUTHORIZED)
        }

        true
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
