package navimateforbusiness.api

import navimateforbusiness.ApiException
import navimateforbusiness.Constants
import navimateforbusiness.Role

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
