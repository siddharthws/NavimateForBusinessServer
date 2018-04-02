/**
 * Created by Siddharth on 20-03-2018.
 */
app.service('LeadService', function($q, $http, $localStorage, ObjLead) {
    /* ----------------------------- INIT --------------------------------*/
    var vm = this
    vm.cache = []

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
            url:        '/api/manager/leads/getByIds',
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

                // Update cache
                vm.cache = []
                response.data.forEach(function (leadJson) {
                    vm.cache.push(ObjLead.fromJson(leadJson))
                })

                // Resolve promise
                deferred.resolve()
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

    // API to edit task data
    vm.edit = function (leads){
        // Create deferred object
        var deferred = $q.defer()

        // Convert tasks to JSON
        var leadsJson = []
        leads.forEach(function (lead) {
            leadsJson.push(ObjLead.toJson(lead))
        })

        // Trigger request
        $http({
            method:     'POST',
            url:        '/api/admin/leads/edit',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            },
            data : {
                leads: leadsJson
            }
        }).then(
            // Success
            function (response) {
                // Resolve promise
                deferred.resolve()
            },
            // Error
            function (error) {
                // Reject promise
                deferred.reject(error.data.error)
            })

        return deferred.promise
    }

    // API to get task by ID
    vm.getById = function (id) {
        for (var i = 0; i < vm.cache.length; i++) {
            var lead = vm.cache[i]
            if (lead.id == id) {
                return lead
            }
        }

        return null
    }
})
