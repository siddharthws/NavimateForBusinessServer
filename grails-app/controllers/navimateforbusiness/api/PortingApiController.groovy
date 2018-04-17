package navimateforbusiness.api

import navimateforbusiness.Task

import grails.converters.JSON
import navimateforbusiness.Lead
import navimateforbusiness.LeadM

class PortingApiController {

    // Port task creator column from owner
    def taskCreator() {
        def tasks = Task.findAll()
        tasks.each {task ->
            task.creator = task.manager
            task.save(flush: true, failOnError: true)
        }
    }

    // Porting APi to move leads into mongo database
    def mongoLeads() {
        // Port each lead to mongo DB
        def leads = Lead.findAll()
        leads.eachWithIndex {lead, i ->
            LeadM mongoLead = new LeadM(
                    accountId: lead.accountId,
                    ownerId: lead.managerId,
                    extId: lead.extId,
                    isRemoved: lead.isRemoved,
                    visibility: lead.visibility,
                    name: lead.name,
                    address: lead.address,
                    latitude: lead.latitude,
                    longitude: lead.longitude,
                    templateId: lead.templateData.templateId)

            // Add templated data
            lead.templateData.values.each {value ->
                mongoLead[String.valueOf(value.fieldId)] = value.value
            }

            // Save lead
            mongoLead.save()

            log.error("Done with " + i + " leads out of " + leads.size())
        }

        def resp = [success: true]
        render resp as JSON
    }
}
