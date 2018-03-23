/**
 * Created by Siddharth on 13-09-2017.
 */

app.service('GoogleApiService', function($q, $http, $localStorage) {

    // Canceller related vars
    var bSearchOngoing = false, bAddressOngoing = false, bLatlngOngoing = false
    var searchCanceller = null
    var addressCanceller = null
    var latlngCanceller = null

    // API to show toast on screen
    this.search = function (text)
    {
        // Cancel ongoing request
        if (bSearchOngoing) {
            searchCanceller.resolve()
            bSearchOngoing = false
        }
        searchCanceller = $q.defer()

        // Prepare deferred object
        var deferred = $q.defer()

        // Send HTTP request to get place suggestions
        bSearchOngoing = true
        $http({
            method:     'GET',
            url:        '/api/googleapis/autocomplete',
            timeout: searchCanceller.promise,
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            },
            params:       {
                input:  text
            }
        }).then(
            function (response) {
                // Reset flag
                bSearchOngoing = false

                // Resolve promise with data
                deferred.resolve(response.data)
            },
            function (error) {
                // Ignore if request was cancelled
                if (error.status == -1) {
                    return
                }

                // Reset ongoing flag
                bSearchOngoing = false

                // Reject promise
                deferred.reject()
            }
        )

        // Return promise
        return deferred.promise
    }

    // API to convert form address to latlng
    this.addressToLatlng = function (address)
    {
        // Cancel ongoing request
        if (bAddressOngoing) {
            addressCanceller.resolve()
            bAddressOngoing = false
        }
        addressCanceller = $q.defer()

        // Prepare deferred object
        var deferred = $q.defer()

        // Send HTTP request to get place suggestions
        bAddressOngoing = true
        $http({
            method:     'GET',
            url:        '/api/googleapis/geocode',
            timeout:    addressCanceller.promise,
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            },
            params:       {
                address:  address
            }
        }).then(
            function (response) {
                // Reset flag
                bAddressOngoing = false

                // Resolve promise with data
                deferred.resolve(response.data)
            },
            function (error) {
                // Ignore if request was cancelled
                if (error.status == -1) {
                    return
                }

                // Reset ongoing flag
                bAddressOngoing = false

                // Reject promise
                deferred.reject()
            }
        )

        // Return promise
        return deferred.promise
    }

    // API to convert form latlng to address
    this.latlngToAddress = function (lat, lng)
    {
        // Cancel ongoing request
        if (bLatlngOngoing) {
            latlngCanceller.resolve()
            bLatlngOngoing = false
        }
        latlngCanceller = $q.defer()

        // Prepare deferred object
        var deferred = $q.defer()

        // Send HTTP request to get place suggestions
        $http({
            method:     'GET',
            url:        '/api/googleapis/geocode/reverse',
            timeout:    latlngCanceller.promise,
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            },
            params:       {
                latitude:   lat,
                longitude:  lng
            }
        }).then(
            function (response) {
                // Reset flag
                bLatlngOngoing = false

                // Resolve promise with data
                deferred.resolve(response.data.address)
            },
            function (error) {
                // Ignore if request was cancelled
                if (error.status == -1) {
                    return
                }

                // Reset ongoing flag
                bLatlngOngoing = false

                // Reject promise
                deferred.reject()
            }
        )

        return deferred.promise
    }
})
