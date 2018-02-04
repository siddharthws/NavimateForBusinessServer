package navimateforbusiness.api

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class AdminApiInterceptorSpec extends Specification implements InterceptorUnitTest<AdminApiInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test adminApi interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"adminApi")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
