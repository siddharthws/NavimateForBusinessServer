package navimateforbusiness.api

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class ExtApiInterceptorSpec extends Specification implements InterceptorUnitTest<ExtApiInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test extApi interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"extApi")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
