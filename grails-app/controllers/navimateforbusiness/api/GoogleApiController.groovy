package navimateforbusiness.api

import grails.converters.JSON
import grails.plugins.rest.client.RestBuilder
import navimateforbusiness.ApiException
import navimateforbusiness.Constants

class GoogleApiController {

    private final API_KEY = "AIzaSyAlTGdM8F8vrAcLW9dBaH-hPku8JJaFgu4"

    def authService

    def autocomplete() {
        // Authenticate user
        def accessToken = request.getHeader("X-Auth-Token")
        if (!accessToken) {
            throw new ApiException("Unauthorized", Constants.HttpCodes.UNAUTHORIZED)
        }
        def user = authService.getUserFromAccessToken(accessToken)

        if (!params.input) {
            throw new ApiException("No input for autocomplete", Constants.HttpCodes.BAD_REQUEST)
        }

        // Access Google APIs
        def urlParams = [
                key: API_KEY,
                input: params.input
        ]
        def rest = new RestBuilder(connectTimeout:1000, readTimeout:20000)
        def googleResp = rest.get("https://maps.googleapis.com/maps/api/place/autocomplete/json?key={key}&input={input}") {
            urlVariables urlParams
        }

        // Parse google response into results
        def resp = []
        if (googleResp.json.status == "OK") {
            def predictions = googleResp.json.predictions
            predictions.each { prediction ->
                def result = [
                        address: prediction.description,
                        placeId: prediction.place_id
                ]
                resp.push(result)
            }
        }

        render resp as JSON
    }

    def geocode() {
        // Authenticate user
        def accessToken = request.getHeader("X-Auth-Token")
        if (!accessToken) {
            throw new ApiException("Unauthorized", Constants.HttpCodes.UNAUTHORIZED)
        }
        def user = authService.getUserFromAccessToken(accessToken)

        if (!params.address) {
            throw new ApiException("No address for geocoding", Constants.HttpCodes.BAD_REQUEST)
        }

        // Access Google APIs
        def urlParams = [
                key: API_KEY,
                address: params.address
        ]
        def rest = new RestBuilder(connectTimeout:1000, readTimeout:20000)
        def googleResp = rest.get("https://maps.googleapis.com/maps/api/geocode/json?key={key}&address={address}") {
            urlVariables urlParams
        }

        // Parse google response into results
        def resp = []
        if ((googleResp.json.status == "OK") && (googleResp.json.results)) {
            resp = [
                    latitude: googleResp.json.results[0].geometry.location.lat,
                    longitude: googleResp.json.results[0].geometry.location.lng
            ]
        }

        render resp as JSON
    }

    def reverseGeocode() {
        // Authenticate user
        def accessToken = request.getHeader("X-Auth-Token")
        if (!accessToken) {
            throw new ApiException("Unauthorized", Constants.HttpCodes.UNAUTHORIZED)
        }
        def user = authService.getUserFromAccessToken(accessToken)

        if (!params.latitude || !params.longitude) {
            throw new ApiException("No latlng for reverse geocoding", Constants.HttpCodes.BAD_REQUEST)
        }

        // Access Google APIs
        def urlParams = [
                key:    API_KEY,
                latlng: params.latitude + "," + params.longitude
        ]
        def rest = new RestBuilder(connectTimeout:1000, readTimeout:20000)
        def googleResp = rest.get("https://maps.googleapis.com/maps/api/geocode/json?key={key}&latlng={latlng}") {
            urlVariables urlParams
        }

        // Parse google response into results
        def resp = []
        if ((googleResp.json.status == "OK") && (googleResp.json.results)) {
            resp = [
                    address: googleResp.json.results[0].formatted_address
            ]
        }

        render resp as JSON
    }
}
