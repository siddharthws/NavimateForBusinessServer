package navimateforbusiness.api

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class UserApiInterceptorSpec extends Specification implements InterceptorUnitTest<UserApiInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test userApi interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"userApi")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
