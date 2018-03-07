/**
 * Created by Siddharth on 26-02-2018.
 */

app.service('SearchService', function($q, $http, $localStorage) {
    /* ----------------------------- INIT --------------------------------*/
    var vm  = this

    /* ----------------------------- Public APIs --------------------------------*/
    vm.search = function (text, name, pager, canceller) {
        // Init deferred object
        var deferred = $q.defer()

        // Trigger Search Http Request
        $http({
            method: 'POST',
            url: '/api/manager/' + name + "/search",
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
                // Resolve promise
                deferred.resolve(response.data)
            },
            // Error Callback
            function (error) {
                // Ignore if request was cancelled
                if (error.status == -1) {
                    return
                }

                // Reject promise
                deferred.reject()
            }
        )

        return deferred.promise
    }
    /* ----------------------------- Private APIs --------------------------------*/
})
