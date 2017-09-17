package navimateforbusiness.api

import grails.converters.JSON
import grails.plugins.rest.client.RestBuilder
import navimateforbusiness.ApiException
import navimateforbusiness.Constants
import navimateforbusiness.LatLng

class GoogleApiController {

    def authService
    def googleApiService

    def autocomplete() {
        // Authenticate user
        def accessToken = request.getHeader("X-Auth-Token")
        if (!accessToken) {
            throw new ApiException("Unauthorized", Constants.HttpCodes.UNAUTHORIZED)
        }
        def user = authService.getUserFromAccessToken(accessToken)

        // Validate params
        if (!params.input) {
            throw new ApiException("No input for autocomplete", Constants.HttpCodes.BAD_REQUEST)
        }

        // Access Google APIs
        def resp = googleApiService.autocomplete(params.input)
        render resp as JSON
    }

    def geocode() {
        // Authenticate user
        def accessToken = request.getHeader("X-Auth-Token")
        if (!accessToken) {
            throw new ApiException("Unauthorized", Constants.HttpCodes.UNAUTHORIZED)
        }
        def user = authService.getUserFromAccessToken(accessToken)

        // Validate params
        if (!params.address) {
            throw new ApiException("No address for geocoding", Constants.HttpCodes.BAD_REQUEST)
        }

        // Access Google APIs
        String[] addresses = [params.address]
        def latlngs = googleApiService.geocode(addresses)

        // Send single response
        def resp = latlngs[0]
        render resp as JSON
    }

    def reverseGeocode() {
        // Authenticate user
        def accessToken = request.getHeader("X-Auth-Token")
        if (!accessToken) {
            throw new ApiException("Unauthorized", Constants.HttpCodes.UNAUTHORIZED)
        }
        def user = authService.getUserFromAccessToken(accessToken)

        // Validate params
        if (!params.latitude || !params.longitude) {
            throw new ApiException("No latlng for reverse geocoding", Constants.HttpCodes.BAD_REQUEST)
        }

        // Access Google APIs
        LatLng[] latlngs = [new LatLng( latitude: params.latitude,
                                        longitude: params.longitude)]
        def addresses = googleApiService.reverseGeocode(latlngs)

        // Send single response
        def resp = addresses[0]
        render resp as JSON
    }
}
