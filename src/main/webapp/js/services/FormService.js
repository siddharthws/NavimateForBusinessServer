/**
 * Created by Siddharth on 31-05-2018.
 */

app.service('FormService', function($q, $http, $localStorage, ObjForm) {
    /* ----------------------------- INIT --------------------------------*/
    var vm = this

    // Sync Serialization related variables
    var canceller = null
    var bOngoing = false

    /* ----------------------------- APIs --------------------------------*/
    // API to get task data
    vm.sync = function (ids){
        if (bOngoing) {
            canceller.resolve()
            bOngoing = false
        }
        canceller = $q.defer()

        // Create deferred object
        var deferred = $q.defer()

        // Trigger request
        bOngoing = true
        $http({
            method:     'POST',
            url:        '/api/manager/forms/getByIds',
            timeout: canceller.promise,
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            },
            data : {
                ids: ids
            }
        }).then(
            // Success
            function (response) {
                // Reset ongoing flag
                bOngoing = false

                // Parse response into Form Object
                var forms = []
                response.data.forEach(function (formJson) {
                    forms.push(ObjForm.fromJson(formJson))
                })

                // Resolve promise
                deferred.resolve(forms)
            },
            // Error
            function (error) {
                // Ignore if request was cancelled
                if (error.status == -1) {
                    return
                }

                // Reset ongoing flag
                bOngoing = false

                // Resolve promise
                deferred.reject()
            })

        return deferred.promise
    }
})
