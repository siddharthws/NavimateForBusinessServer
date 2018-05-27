package navimateforbusiness.api

import navimateforbusiness.util.ApiException
import navimateforbusiness.util.Constants
import navimateforbusiness.enums.Role

// Authentication for ManagerApiController
class ManagerApiInterceptor {
    // ----------------------- Dependencies ---------------------------//
    def authService

    // ----------------------- Methods ----------------------- //
    boolean before() {
        // Authenticate access token
        authService.authenticate(request.getHeader("X-Auth-Token"))

        // Check permission level of user
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))
        if (user.role < Role.MANAGER) {
            throw new ApiException("Unauthorized", Constants.HttpCodes.UNAUTHORIZED)
        }

        true
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
