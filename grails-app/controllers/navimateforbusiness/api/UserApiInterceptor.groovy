package navimateforbusiness.api

class UserApiInterceptor {

    def authService

    boolean before() {
        return authService.authenticate(request.getHeader("X-Auth-Token"))
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
