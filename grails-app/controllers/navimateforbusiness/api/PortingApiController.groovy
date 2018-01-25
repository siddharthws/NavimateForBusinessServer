package navimateforbusiness.api

import grails.converters.JSON
import navimateforbusiness.Lead
import navimateforbusiness.Visibility

class PortingApiController {

    def leadVisibility () {
        // Get All leads
        def leads = Lead.findAll()

        leads.each {lead ->
            // Assign visibility to leads based on EXT ID
            if (lead.extId) {
                lead.visibility = Visibility.PUBLIC
            } else {
                lead.visibility = Visibility.PRIVATE
            }

            // Save
            lead.save(failOnError: true, flush: true)
        }

        def resp = [success: true]
        render resp as JSON
    }
}
