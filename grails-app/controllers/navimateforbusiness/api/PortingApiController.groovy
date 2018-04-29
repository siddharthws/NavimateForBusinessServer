package navimateforbusiness.api

import navimateforbusiness.Constants
import navimateforbusiness.Field
import navimateforbusiness.Task

import grails.converters.JSON
import navimateforbusiness.Lead
import navimateforbusiness.LeadM
import navimateforbusiness.Value

import java.text.SimpleDateFormat

class PortingApiController {

    // Port task creator column from owner
    def taskCreator() {
        def tasks = Task.findAll()
        tasks.each {task ->
            task.creator = task.manager
            task.save(flush: true, failOnError: true)
        }
    }

    def fixDates() {
        // Iterate through all date fields
        def values = []
        Field.findAllByType(Constants.Template.FIELD_TYPE_DATE).each { field ->
            values.addAll(Value.findAllByField(field))
        }

        SimpleDateFormat df = new SimpleDateFormat(navimateforbusiness.Constants.Date.FORMAT_FRONTEND)
        int numValues = values.size()
        values.eachWithIndex {Value value, j ->
            // Update date value to new format
            if (value.value) {
                String newVal = df.parse(value.value).format(Constants.Date.FORMAT_BACKEND)
                value.value = newVal
                value.save(flush: true, failOnError: true)

                log.error("Fixing Dates Done with " + j + " : " + numValues)
            }
        }

        def resp = [success: true]
        render resp as JSON
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
