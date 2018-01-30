package navimateforbusiness

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import org.grails.web.json.JSONArray

@Transactional
class LeadService {

    def googleApiService

    def parseExcel(User manager, JSONArray excelJson) {
        // Parse JSON to Lead Object
        def leads = parseToLeads(manager, excelJson)

        leads = addressToLatLng(leads)

        leads
    }

    def parseToLeads(User manager, JSONArray excelJson){
        def leads = []

        // Get all Column Names
        def columns = excelJson[0]

        // Get indexes of columns
        int titleIdx = columns.indexOf("Title*")
        int descIdx = columns.indexOf("Description")
        int phoneIdx = columns.indexOf("Phone*")
        int addressIdx = columns.indexOf("Address*")
        int emailIdx = columns.indexOf("Email")
        int latIdx = columns.indexOf("Latitude")
        int lngIdx = columns.indexOf("Longitude")

        // Ensure Mandatory Columns are present
        if ((titleIdx == -1) || (phoneIdx == -1) || (addressIdx == -1)){
            throw new navimateforbusiness.ApiException("Mandatory columns missing", navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
        }

        // Create Lead Objects
        for (int i = 1; i < excelJson.length(); i++){
            def row = excelJson[i]

            String title = row[titleIdx]
            String phone = row[phoneIdx]
            String address = row[addressIdx]
            String email = ""
            String description = ""
            Double lat = 0
            Double lng = 0

            // Ignore entry if all mandatory fields are blank
            if (!title && !phone && !address) {
                continue
            }

            // Ensure data is present for mandatory columns
            if (!title || !phone || !address){
                throw new navimateforbusiness.ApiException("Data in mandatory columns missing (" + title + ":" + phone + ":" + address + ")", navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
            }

            // Populate Optional
            if (emailIdx != -1)
            {
                email = row[emailIdx]
            }

            if (descIdx != -1)
            {
                description = row[descIdx]
            }

            if ((latIdx != -1) && (lngIdx != -1) && row[latIdx] && row[lngIdx] )
            {
                lat = Double.parseDouble(row[latIdx])
                lng = Double.parseDouble(row[lngIdx])
            }

            // check if lead already present in database
            Lead lead = Lead.findByManagerAndTitleAndDescriptionAndPhoneAndAddressAndEmailAndIsRemoved(manager,title,description,phone,address,email, false)
            if(!lead) {
                // Create Lead Objects
                  lead = new Lead(
                          title:          title,
                          phone:          phone,
                          address:        address,
                          manager:        manager,
                          account:        manager.account,
                          description:    description,
                          email:          email,
                          latitude:       lat,
                          longitude:      lng,
                          isRemoved:      false,
                          visibility:     navimateforbusiness.Visibility.PRIVATE)
            }

            leads.push(lead)
        }

        // Throw exception if no leads found
        if (!leads.size()) {
            throw new navimateforbusiness.ApiException("No leads found in excel...", navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
        }

        return leads
    }

    def   addressToLatLng(def leads){
        leads.each { lead ->
                if(!(lead.latitude && lead.longitude)) {
                    def address = lead.address
                    String[] addresses = [lead.address]

                    try {
                        def latlngs = googleApiService.geocode(addresses)

                        lead.latitude = latlngs[0].latitude
                        lead.longitude = latlngs[0].longitude
                    }
                    catch (navimateforbusiness.ApiException e){
                        lead.latitude = 0
                        lead.longitude = 0
                        lead.address = ""
                    }
                }
            }
        leads
    }

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
