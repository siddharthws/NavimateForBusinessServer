/**
 * Created by Siddharth on 15-03-2018.
 */
app.service('TeamService', function($q, $http, $localStorage, ObjUser) {
    /* ----------------------------- INIT --------------------------------*/
    var vm = this

    // User cache
    vm.cache = []
    vm.managers = []

    // Sync Serialization related variables
    var canceller = null
    var bOngoing = false

    /* ----------------------------- APIs --------------------------------*/
    // API to get task data
    vm.sync = function (ids){
        // Cancel on going request if any
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
            url:        '/api/manager/team/getByIds',
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
                response.data.forEach(function (userJson) {
                    vm.cache.push(ObjUser.fromJson(userJson))
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

    // API to get all managers
    vm.syncManagers = function (){
        // Create deferred object
        var deferred = $q.defer()

        // Trigger request
        $http({
            method:     'POST',
            url:        '/api/cc/team/getManagers',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            }
        }).then(
            // Success
            function (response) {
                // Update cache
                vm.managers = []
                response.data.forEach(function (userJson) {
                    vm.managers.push(ObjUser.fromJson(userJson))
                })

                // Resolve promise
                deferred.resolve()
            },
            // Error
            function (error) {
                // Resolve promise
                deferred.reject()
            })

        return deferred.promise
    }

    // API to edit task data
    vm.edit = function (team){
        // Create deferred object
        var deferred = $q.defer()

        // Convert user objects to JSON
        var teamJson = []
        team.forEach(function (user) {
            teamJson.push(ObjUser.toJson(user))
        })

        // Trigger request
        $http({
            method:     'POST',
            url:        '/api/admin/team/edit',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            },
            data : {
                team: teamJson
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

    // APi to get user by ID
    vm.getById = function (id) {
        for (var i = 0; i < vm.cache.length; i++) {
            var user = vm.cache[i]
            if (user.id == id) {
                return user
            }
        }

        return null
    }

    // APi to get manager by ID
    vm.getManagerById = function (id) {
        for (var i = 0; i < vm.managers.length; i++) {
            var user = vm.managers[i]
            if (user.id == id) {
                return user
            }
        }

        return null
    }

})
