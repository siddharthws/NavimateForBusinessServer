package navimateforbusiness

import grails.converters.JSON
import grails.gorm.transactions.Transactional

@Transactional
class LeadService {
    // ----------------------- Dependencies ---------------------------//
    def googleApiService
    def templateService
    def valueService
    def templateDataService

    // ----------------------- Getter APIs ---------------------------//
    // Method to get leads for a specific user
    def getForUser(User user) {
        def leads = []

        // Get leads as per access level
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
        leads = leads.sort {it -> it.name}

        // Return leads
        leads
    }

    // Method to get lead for user by id
    def getForUserById(User user, long id) {
        // Get lead by this id
        Lead lead = Lead.findByAccountAndIsRemovedAndId(user.account, false, id)

        // Validate lead and access
        if (!checkAccess(user, lead)) {
            lead = null
        }

        lead
    }

    // Method to get lead for user by id
    def getForUserByExtId(User user, String extId) {
        // Get lead by this id
        Lead lead = Lead.findByAccountAndIsRemovedAndExtId(user.account, false, extId)

        // Validate lead and access
        if (!checkAccess(user, lead)) {
            lead = null
        }

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
        def values = lead.templateData.values
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
    // API to check if a given user has access to the lead
    private def checkAccess(User user, Lead lead) {
        // Validate data
        if (lead == null) {
            return false
        }

        // Check if lead belongs to account and is not removed
        if ((lead.account != user.account) || lead.isRemoved) {
            return false
        }

        // Admin has all access
        if (user.role == navimateforbusiness.Role.ADMIN) {
            return true
        }

        // Publicly visible leads are accessible to everyone
        if (lead.visibility == navimateforbusiness.Visibility.PUBLIC) {
            return true
        }

        //  Owner of lead should be able to access it
        if (lead.manager == user) {
            return true
        }

        false
    }

    // ----------------------- Unclean APIs ---------------------------//
    def getLeadData (List<Lead> leads) {
        def leadsJson = []

        leads.each {lead ->
            // Create lead JSON object
            def leadJson = [
                    id: lead.id,
                    ownerId: lead.manager.id,
                    title: lead.name,
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
