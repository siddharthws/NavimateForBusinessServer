package navimateforbusiness.api

import grails.converters.JSON
import navimateforbusiness.ApiException
import navimateforbusiness.Lead
import navimateforbusiness.Marshaller
import org.grails.web.json.JSONArray

class LeadApiController {

    def leadService
    def authService

    def upload() {
        // Authenticate User
        def accessToken = request.getHeader("X-Auth-Token")
        if (!accessToken) {
            throw new ApiException("Unauthorized", 401)
        }
        def user = authService.getUserFromAccessToken(accessToken)

        // Parse Input
        JSONArray excelJson = JSON.parse(request.JSON.excelData)
        ArrayList<Lead> leads = leadService.parseExcel(user, excelJson)

        // Send response
        def resp = new JSONArray();
        leads.each { lead ->
            resp.add(Marshaller.serializeLead(lead))
        }
        render resp as JSON
    }
}
