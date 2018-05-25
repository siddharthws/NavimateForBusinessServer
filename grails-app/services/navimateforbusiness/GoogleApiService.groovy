package navimateforbusiness

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.plugins.rest.client.RestBuilder
import navimateforbusiness.LatLng
import org.grails.web.json.JSONArray

@Transactional
class GoogleApiService {

    private final API_KEY = "AIzaSyAlTGdM8F8vrAcLW9dBaH-hPku8JJaFgu4"

    def autocomplete(String input) {

        // Access Google APIs
        def urlParams = [
                key: API_KEY,
                input: input
        ]
        def request = new RestBuilder(connectTimeout:1000, readTimeout:20000)
        def response = request.get("https://maps.googleapis.com/maps/api/place/autocomplete/json?key={key}&input={input}") {
            urlVariables urlParams
        }
        def respJson = response.json

        // Parse google response into results
        def results = []
        if (respJson.status == "OK") {
            respJson.predictions.each { prediction ->  results.push(prediction.description) }
        }
        else {
            throw new navimateforbusiness.ApiException("Google API call failed with status : " + respJson.status)
        }

        results
    }

    def geocode(String[] inputs) {
        // Prepare array of latlngs for all addresses
        def latlngs = []
        inputs.eachWithIndex {address, i ->
            // Access Google APIs & get response Json
            def urlParams = [
                    key: API_KEY,
                    address: address
            ]
            def request     = new RestBuilder(connectTimeout:1000, readTimeout:20000)
            def response    = request.get("https://maps.googleapis.com/maps/api/geocode/json?key={key}&address={address}") {
                urlVariables urlParams
            }
            def respJson = response.json

            // Check status
            if (respJson && (respJson.status == "OK")) {
                // Push new latlng into list
                latlngs.push(new navimateforbusiness.LatLng(respJson.results[0].geometry.location.lat,
                                                            respJson.results[0].geometry.location.lng))
            }
            else {
                // Push empty LatLng into the list
                latlngs.push(new navimateforbusiness.LatLng(0, 0))
            }

            // Mandatory 50 second delay between suucessive requests (recommended by google)
            if (i < (latlngs.size() - 1)) {
                sleep(50)
            }
        }

        latlngs
    }

    def reverseGeocode(navimateforbusiness.LatLng[] latlngs) {
        // Prepare array of latlngs for all addresses
        def addresses = []
        latlngs.eachWithIndex {latlng, i ->
            // Access Google APIs & get response Json
            def urlParams = [
                    key:    API_KEY,
                    latlng: latlng.lat + "," + latlng.lng
            ]
            def request = new RestBuilder(connectTimeout:1000, readTimeout:20000)
            def response = request.get("https://maps.googleapis.com/maps/api/geocode/json?key={key}&latlng={latlng}") {
                urlVariables urlParams
            }
            def respJson = response.json

            // Check status
            if (respJson && (respJson.status == "OK")) {
                // Push new latlng into list
                addresses.push([address: respJson.results[0].formatted_address])
            }
            else {
                throw new navimateforbusiness.ApiException("Google API call failed with status : " + respJson.status)
            }

            // Mandatory 50 second delay between suucessive requests (recommended by google)
            if (i < (latlngs.size() - 1)) {
                sleep(50)
            }
        }

        addresses
    }

    def snapToRoads(List<navimateforbusiness.LatLng> points) {
        // Get results from google using 100 points at a time
        int numPoints = points.size()
        int numLoops = Math.ceil(numPoints / 100)

        def snappedPoints = []
        for (int i = 0; i < numLoops; i++) {
            // Wait for 100 ms on all loops except first
            if (i > 0) {sleep(100)}

            // Get start Index
            int startIdx = i * 100

            // Get 100 points from start Index
            def snapPoints = points.subList(startIdx, Math.min(startIdx + 100, points.size()))

            // Get string from list of points
            String path = ""
            snapPoints.each { path += path.length() ? '|' + it.lat + "," + it.lng : it.lat + "," + it.lng}

            // Get snapped result from google
            def urlParams = [
                    key:   API_KEY,
                    path:  path
            ]
            def request = new RestBuilder(connectTimeout:1000, readTimeout:20000)
            def response = request.get("https://roads.googleapis.com/v1/snapToRoads?key={key}&path={path}&interpolate=true") {
                urlVariables urlParams
            }

            // Extract each snapped point from response and feed to local array
            def respJson = response.json
            respJson.snappedPoints.each {it ->
                // Check if this point is present in original data
                if (it.originalIndex) {
                    // Update original index
                    it.originalIndex += startIdx
                }

                // Add point to snapped points response
                snappedPoints.push(it)
            }
        }

        return snappedPoints
    }
}
