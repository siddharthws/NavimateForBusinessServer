package navimateforbusiness.api

import navimateforbusiness.ApiException
import navimateforbusiness.ApiKey
import navimateforbusiness.Constants
import org.grails.web.json.JSONException
import org.grails.web.json.JSONObject


class ExtApiInterceptor {

    boolean before() {
        // Get API key passed in header
        String apiKey = request.getHeader("X-Api-Key")

        // Check for missing API Key
        if (!apiKey) {
            throw new ApiException("API key is missing", Constants.HttpCodes.UNAUTHORIZED)
        }

        // Check for Invalid API Key
        if (!ApiKey.findByKey(apiKey)) {
            throw new ApiException("Invalid API Key", Constants.HttpCodes.UNAUTHORIZED)
        }

        // validate POST input JSON
        if (request.method == 'POST') {
            // Catch exception thrown internally by grails while converting request.JSON first time
            try {
                def input = request.JSON
            } catch (JSONException ex) {
                throw new ApiException("Invalid input.", Constants.HttpCodes.BAD_REQUEST)
            }

            // Print JSON for debugging
            println("Incoming JSON from External API = " + request.JSON.toString())

            // Input should always be a valid JSON
            if (!isJsonObjectValid(request.JSON.toString())) {
                throw new ApiException("Invalid input.", Constants.HttpCodes.BAD_REQUEST)
            }
        }

        // Key Validation Success
        return true
    }

    // Methods to validate JSON Strings
    boolean isJsonObjectValid(String string) {
        try {
            new JSONObject(string)
        } catch (JSONException ex1) {
            return false
        }

        return true
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
