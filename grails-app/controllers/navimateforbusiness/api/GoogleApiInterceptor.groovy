package navimateforbusiness.api

import navimateforbusiness.ApiException
import navimateforbusiness.Constants


class GoogleApiInterceptor {

    def authService

    boolean before() {
        auth()
        true
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }

    private void auth() {
        def accessToken = request.getHeader("X-Auth-Token")
        if (!accessToken) {
            throw new ApiException("Unauthorized", Constants.HttpCodes.UNAUTHORIZED)
        }
        def user = authService.getUserFromAccessToken(accessToken)
    }
}
