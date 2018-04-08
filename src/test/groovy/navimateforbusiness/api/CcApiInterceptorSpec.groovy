package navimateforbusiness.api

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class CcApiInterceptorSpec extends Specification implements InterceptorUnitTest<CcApiInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test ccApi interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"ccApi")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
