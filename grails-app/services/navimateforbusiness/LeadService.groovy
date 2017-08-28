package navimateforbusiness

import grails.gorm.transactions.Transactional
import org.grails.web.json.JSONArray

@Transactional
class LeadService {

    def parseExcel(JSONArray excelJson) {
        ArrayList<Lead> leads = new ArrayList<>()

        // Parse JSON to Lead Object

        // Init Lat / Lng from Address

        return leads
    }
}
