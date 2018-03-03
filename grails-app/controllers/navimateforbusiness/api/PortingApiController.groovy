package navimateforbusiness.api

import grails.converters.JSON
import navimateforbusiness.Role
import navimateforbusiness.User

class PortingApiController {

    // For some reps, manager is set to null while account is not. correct these.
    def dbFix1() {
        def reps = User.findAllByRole(Role.REP)

        // Find all buggy entries
        reps = reps.findAll {it -> ((it.manager && !it.account) || (it.account && !it.manager))}

        // Fix each entry
        reps.each {it ->
            it.account = null
            it.manager = null
            it.save(failOnError: true, flush: true)
        }

        def resp = [success: true]
        render resp as JSON
    }
}
