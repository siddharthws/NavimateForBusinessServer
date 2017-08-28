package navimateforbusiness.api

import grails.converters.JSON
import navimateforbusiness.ApiException
import navimateforbusiness.Lead
import navimateforbusiness.Marshaller
import org.grails.web.json.JSONArray

class LeadApiController {

    def leadService

    def upload() {
        // Authenticate User
        def accessToken = request.getHeader("X-Auth-Token")
        if (!accessToken) {
            throw new ApiException("Unauthorized", 401)
        }

        // Parse Input
        JSONArray excelJson = JSON.parse(request.JSON.excelData)
        ArrayList<Lead> leads = leadService.parseExcel(excelJson)

        // Serialize Response
        JSONArray resp = new JSONArray()
        for (Lead lead : leads)
        {
            resp.add(Marshaller.serializeLead(lead))
        }

        // Send response
        render resp as JSON
    }
}
