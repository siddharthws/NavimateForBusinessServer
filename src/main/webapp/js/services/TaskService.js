/**
 * Created by aroha on 18-01-2018.
 */
app.service('TaskService', function($q, $http, $localStorage, ObjTask) {
    /* ----------------------------- INIT --------------------------------*/
    var vm = this
    vm.cache = []

    // Sync Serialization related variables
    var canceller = null
    var bOngoing = false

    /* ----------------------------- APIs --------------------------------*/
    // Method to reset Service
    vm.reset = function () {
        // Reset cache
        vm.cache = []
    }

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
            url:        '/api/manager/tasks/getByIds',
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
                response.data.forEach(function (taskJson) {
                    vm.cache.push(ObjTask.fromJson(taskJson))
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
    vm.edit = function (tasks){
        // Create deferred object
        var deferred = $q.defer()

        // Convert tasks to JSON
        var tasksJson = []
        tasks.forEach(function (task) {
            tasksJson.push(ObjTask.toJson(task))
        })

        // Trigger request
        $http({
            method:     'POST',
            url:        '/api/manager/tasks/edit',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            },
            data : {
                tasks: tasksJson
            }
        }).then(
            // Success
            function (response) {
                // Resolve promise
                deferred.resolve()
            },
            // Error
            function (error) {
                // Resolve promise
                deferred.reject(error.data.error)
            })

        return deferred.promise
    }

    // API to get task by ID
    vm.getById = function (id) {
        for (var i = 0; i < vm.cache.length; i++) {
            var task = vm.cache[i]
            if (task.id == id) {
                return task
            }
        }

        return null
    }

})