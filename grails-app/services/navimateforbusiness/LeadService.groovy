package navimateforbusiness

import grails.gorm.transactions.Transactional
import org.grails.web.json.JSONArray

@Transactional
class LeadService {

    def parseExcel(User manager, JSONArray excelJson) {
        // Parse JSON to Lead Object
        def leads = parseToLeads(manager, excelJson)

        // TODO: Init Lat / Lng from Address using Places API

        return leads
    }

    def parseToLeads(User manager, JSONArray excelJson){
        def leads = []

        // Get all Column Names
        def columns = excelJson[0]

        // Get indexes of columns
        int nameIdx = columns.indexOf("Name")
        int phoneIdx = columns.indexOf("Phone")
        int addressIdx = columns.indexOf("Address")
        int companyIdx = columns.indexOf("Company")
        int emailIdx = columns.indexOf("Email")
        int latIdx = columns.indexOf("Latitude")
        int lngIdx = columns.indexOf("Longitude")

        // Ensure Mandatory Columns are present
        if ((nameIdx == -1) || (phoneIdx == -1) || (addressIdx == -1)){
            throw new navimateforbusiness.ApiException("Mandatory columns missing", navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
        }

        // Create Lead Objects
        for (int i = 1; i < excelJson.length(); i++){
            def row = excelJson[i]
            String name = row[nameIdx]
            String phone = row[phoneIdx]
            String address = row[addressIdx]

            // Ensure data is present for mandatory columns
            if (!name || !phone || !address){
                throw new navimateforbusiness.ApiException("Data in mandatory columns missing", navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
            }

            // Create Lead Objects
            Lead lead = new Lead(   name: name,
                                    phone: phone,
                                    address: address,
                                    manager: manager,
                                    account: manager.account)

            // Populate Optional
            if (emailIdx != -1)
            {
                String email = row[emailIdx]
                if (email)
                {
                    lead.email = email
                }
            }

            if (companyIdx != -1)
            {
                String company = row[companyIdx]
                if (company)
                {
                    lead.company = company
                }
            }

            if ((latIdx != -1) && (lngIdx != -1))
            {
                Double lat = row[latIdx]
                Double lng = row[lngIdx]
                if (lat && lng)
                {
                    lead.latitude = lat
                    lead.longitude = lng
                }
            }

            leads.push(lead)
        }

        return leads
    }
}
