/**
 * Created by Siddharth on 13-09-2017.
 */

app.service('GoogleApiService', function($http, $localStorage) {

    // API to show toast on screen
    this.autoComplete = function (input, resultCb, errorCb)
    {
        // Send HTTP request to get place suggestions
        $http({
            method:     'GET',
            url:        '/api/googleapis/autocomplete',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            },
            params:       {
                input:  input
            }
        }).then(
            function (response) {
                // Trigger callback
                resultCb(response.data)
            },
            function (error) {
                console.log("Cannot get autocomplete results : " + error)
                errorCb()
            }
        )
    }

    // API to convert form address to latlng
    this.addressToLatlng = function (address, resultCb)
    {
        // Send HTTP request to get place suggestions
        $http({
            method:     'GET',
            url:        '/api/googleapis/geocode',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            },
            params:       {
                address:  address
            }
        }).then(
            function (response) {
                // Trigger callback
                resultCb(response.data.latitude, response.data.longitude)
            },
            function (error) {
                console.log("Cannot convert from address to latlng : " + error)
            }
        )
    }

    // API to convert form latlng to address
    this.latlngToAddress = function (lat, lng, resultCb)
    {
        // Send HTTP request to get place suggestions
        $http({
            method:     'GET',
            url:        '/api/googleapis/geocode/reverse',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            },
            params:       {
                latitude:   lat,
                longitude:  lng
            }
        }).then(
            function (response) {
                // Trigger callback
                resultCb(response.data.address)
            },
            function (error) {
                console.log("Cannot convert from latlng to address : " + error)
            }
        )
    }
})
