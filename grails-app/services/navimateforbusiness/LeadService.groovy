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

    ArrayList<Lead> parseToLeads(User manager, JSONArray excelJson){
        ArrayList<Lead> leads = new ArrayList<>()

        // Get all Column Names
        ArrayList<String> columns = excelJson.getJSONArray(0)

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
            throw new navimateforbusiness.ApiException("Mandatory columns missing", 400)
        }

        // Create Lead Objects
        for (int i = 1; i < excelJson.length(); i++){
            JSONArray rowData = excelJson.get(i)
            String name = rowData.get(nameIdx)
            String phone = rowData.get(phoneIdx)
            String address = rowData.get(addressIdx)

            // Ensure data is present for mandatory columns
            if ((name == null) || (name.length() == 0) ||
                (phone == null) || (phone.length() == 0) ||
                (address == null) || (address.length() == 0)){
                throw new navimateforbusiness.ApiException("Data in mandatory columns missing", 400)
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
                String email = rowData.get(emailIdx)
                if ((email != null) && (email.size() != 0))
                {
                    lead.email = email
                }
            }

            if (companyIdx != -1)
            {
                String company = rowData.get(companyIdx)
                if ((company != null) && (company.size() != 0))
                {
                    lead.company = company
                }
            }

            if ((latIdx != -1) && (lngIdx != -1))
            {
                Double lat = rowData.get(latIdx)
                Double lng = rowData.get(lngIdx)
                if ((lat != null) && (lng != null))
                {
                    lead.latitude = lat
                    lead.longitude = lng
                }
            }

            leads.add(lead)
        }

        return leads
    }
}
