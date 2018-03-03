/**
 * Created by Siddharth on 03-03-2018.
 */

app.service("ImportService", function ($q, $http, $localStorage) {
    /* ----------------------------- Globals --------------------------------*/
    var vm = this

    /* ----------------------------- Public APIs --------------------------------*/
    vm.import = function (url, file) {
        var deferred = $q.defer()

        // Create form data to send
        var formData = new FormData()
        formData.append('importFile', file)

        // Perform http request
        $http({
            method: 'POST',
            url: url,
            headers: {
                'Content-Type': undefined,
                'X-Auth-Token': $localStorage.accessToken
            },
            data: formData
        }).then(
            // Success callback
            function (response) {
                deferred.resolve()
            },
            // Error callback
            function (error) {
                var errorMessage = error.data.error
                deferred.reject(errorMessage)
            }
        )

        // Return promise
        return deferred.promise
    }

    /* ----------------------------- Private APIs --------------------------------*/
})
