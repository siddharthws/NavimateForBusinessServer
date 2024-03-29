package navimateforbusiness.api

import navimateforbusiness.util.ApiException
import navimateforbusiness.util.Constants
import navimateforbusiness.enums.Role


class AdminApiInterceptor {

    def authService

    boolean before() {
        // Authenticate access token
        authService.authenticate(request.getHeader("X-Auth-Token"))

        // Check permission level
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))
        if (user.role < Role.ADMIN) {
            throw new ApiException("Unauthorized access from non admin account...", Constants.HttpCodes.UNAUTHORIZED)
        }

        true
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
