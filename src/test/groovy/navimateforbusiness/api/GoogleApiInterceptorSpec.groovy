package navimateforbusiness.api

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class GoogleApiInterceptorSpec extends Specification implements InterceptorUnitTest<GoogleApiInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test googleApi interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"googleApi")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
