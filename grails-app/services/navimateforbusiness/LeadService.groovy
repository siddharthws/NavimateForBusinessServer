package navimateforbusiness

import grails.converters.JSON
import grails.gorm.transactions.Transactional

@Transactional
class LeadService {
    // ----------------------- Dependencies ---------------------------//
    def googleApiService
    def templateService
    def valueService

    // ----------------------- Getter APIs ---------------------------//
    // Method to get leads for a specific user
    def getForUser(User user) {
        def leads = []

        // Get leads as per access level
        switch (user.role) {
            case navimateforbusiness.Role.ADMIN:
            case navimateforbusiness.Role.CC:
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
        leads = leads.sort {it -> it.name}

        // Return leads
        leads
    }

    // Method to get lead for user by id
    def getForUserById(User user, long id) {
        // Get all leads for this user
        def leads = getForUser(user)

        // Get lead with this ID
        def lead = leads.find {it -> it.id == id}

        lead
    }

    // Method to get lead for user by id
    def getForUserByExtId(User user, String extId) {
        // Get all leads for this user
        def leads = getForUser(user)

        // Get lead with this ID
        def lead = leads.find {it -> it.extId == extId}

        lead
    }

    // ----------------------- Public APIs ---------------------------//
    // Methods to convert lead objects to / from JSON
    def toJson(Lead lead) {
        // Convert template properties to JSON
        def json = [
                id:         lead.id,
                name:       lead.name,
                address:    lead.address,
                lat:        lead.latitude,
                lng:        lead.longitude,
                templateId: lead.templateData.template.id,
                values:     []
        ]

        // Convert template values to JSON
        def values = lead.templateData.values.sort {it -> it.id}
        values.each {value ->
            json.values.push([fieldId: value.field.id, value: value.value])
        }

        json
    }

    Lead fromJson(def json, User user) {
        Lead lead = null

        // Get existing template or create new
        if (json.id) {
            lead = getForUserById(user, json.id)
            if (!lead) {
                throw new navimateforbusiness.ApiException("Illegal access to lead", navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
            }
        } else if (json.extId) {
            lead = getForUserByExtId(user, json.extId)
        }

        // Create new lead object if not found
        if (!lead) {
            lead = new Lead(
                    account: user.account,
                    manager: user,
                    visibility: navimateforbusiness.Visibility.PRIVATE,
                    isRemoved: false,
                    extId: json.extId
            )
        }

        // Set name
        lead.name = json.name

        // Update address and latlng if changed
        if (json.address && lead.address != json.address) {
            lead.address = json.address

            if (!json.lat || !json.lng) {
                // Get latlng from google for this address
                String[] addresses = [json.address]
                def latlngs = googleApiService.geocode(addresses)
                lead.latitude = latlngs[0].latitude
                lead.longitude = latlngs[0].longitude
            } else {
                // Assign lat lng from JSON
                lead.latitude = json.lat
                lead.longitude = json.lng
            }
        }

        // Prepare template data
        def template = templateService.getForUserById(user, json.templateId)
        if (!lead.templateData || lead.templateData.template != template) {
            lead.templateData = new Data(account: user.account, owner: user, template: template)
        }

        // Prepare values
        json.values.each {valueJson ->
            Value value = valueService.fromJson(valueJson, lead.templateData)

            if (!value.id) {
                lead.templateData.addToValues(value)
            }
        }

        lead
    }

    // ----------------------- Private APIs ---------------------------//
}
