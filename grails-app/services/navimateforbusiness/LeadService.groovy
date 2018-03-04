package navimateforbusiness

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import org.grails.web.json.JSONArray

@Transactional
class LeadService {
    // ----------------------- Dependencies ---------------------------//
    def googleApiService

    // ----------------------- Public APIs ---------------------------//
    // Method to get leads for a specific user
    def getForUser(User user) {
        def leads = []

        // Get role as per access level
        switch (user.role) {
            case navimateforbusiness.Role.ADMIN:
                // Get all unremoved leads of the account
                leads = Lead.findAllByAccountAndIsRemoved(user.account, false)
                break

            case navimateforbusiness.Role.MANAGER:
                // Get all leads owned by this manager
                leads = Lead.findAllByAccountAndIsRemovedAndManager(user.account, false, user)

                // Add all public leads of the company
                leads.addAll(Lead.findAllByAccountAndIsRemovedAndVisibility(user.account, false, navimateforbusiness.Visibility.PUBLIC))
                break
        }

        // Sort leads in ascending order of title by default
        leads = leads.sort {it -> it.title}

        // Return leads
        leads
    }

    // ----------------------- Private APIs ---------------------------//

    // ----------------------- Unclean APIs ---------------------------//
    def getLeadData (List<Lead> leads) {
        def leadsJson = []

        leads.each {lead ->
            // Create lead JSON object
            def leadJson = [
                    id: lead.id,
                    ownerId: lead.manager.id,
                    title: lead.title,
                    address: lead.address,
                    latitude: lead.latitude,
                    longitude: lead.longitude,
                    visibility: lead.visibility.value,
                    templateId: lead.templateData.template.id,
                    templateData: [
                        id: lead.templateData.id,
                        values: []
                    ]
            ]

            // Add values to templated data
            def values = lead.templateData.values.sort {it -> it.id}
            values.each {value ->

                def val = value.value
                if (value.field.type == navimateforbusiness.Constants.Template.FIELD_TYPE_RADIOLIST ||
                    value.field.type == navimateforbusiness.Constants.Template.FIELD_TYPE_CHECKLIST) {
                    val = JSON.parse(value.value)
                } else if (value.field.type == navimateforbusiness.Constants.Template.FIELD_TYPE_CHECKBOX) {
                    val = Boolean.valueOf(value.value)
                }

                leadJson.templateData.values.push([
                        id: value.id,
                        fieldId: value.fieldId,
                        value: val
                ])
            }

            // Add to JSON Array
            leadsJson.push(leadJson)
        }

        leadsJson
    }
}
