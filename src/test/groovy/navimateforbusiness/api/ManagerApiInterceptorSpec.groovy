package navimateforbusiness.api

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class ManagerApiInterceptorSpec extends Specification implements InterceptorUnitTest<ManagerApiInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test managerApi interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"managerApi")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
