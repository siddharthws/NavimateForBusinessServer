/**
 * Created by Siddharth on 26-02-2018.
 */

app.service('SearchService', function($q, $http, $localStorage) {
    /* ----------------------------- INIT --------------------------------*/
    var vm  = this

    var canceller = null
    var bOngoing = false

    /* ----------------------------- Public APIs --------------------------------*/
    vm.search = function (url, text, pager) {
        // Cancel on going request if any
        if (bOngoing) {
            canceller.resolve()
            bOngoing = false
        }
        canceller = $q.defer()

        // Init deferred object
        var deferred = $q.defer()

        // Trigger Search Http Request
        bOngoing = true
        $http({
            method: 'POST',
            url: url,
            headers: {
                'X-Auth-Token': $localStorage.accessToken
            },
            timeout: canceller.promise,
            data: {
                text: text,
                pager: pager
            }
        }).then(
            // Success callback
            function (response) {
                // Reset ongoing flag
                bOngoing = false

                // Resolve promise
                deferred.resolve(response.data)
            },
            // Error Callback
            function (error) {
                // Ignore if request was cancelled
                if (error.status == -1) {
                    return
                }

                // Reset ongoing flag
                bOngoing = false

                // Resolve promise
                deferred.reject()
            }
        )

        return deferred.promise
    }
    /* ----------------------------- Private APIs --------------------------------*/
})
