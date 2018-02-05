/**
 * Created by aroha on 18-01-2018.
 */
app.service('TaskDataService', function($rootScope, $http, $localStorage) {

    /* ----------------------------- INIT --------------------------------*/
    var vm = this
    vm.cache ={
        data: []
    }

    /* ----------------------------- APIs --------------------------------*/
    //API to get task data
    vm.sync = function (){
        $http({
            method:     'GET',
            url:        '/api/users/task',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            }
        }).then(
            function (response) {
                // Update cache data
                vm.cache.data = response.data
                $rootScope.$broadcast(Constants.Events.TASK_DATA_READY)
            },
            function (error) {
                $rootScope.$broadcast(Constants.Events.DATA_LOAD_ERROR)
            })
    }

    // APi to get lead by ID
    vm.getById = function (id) {
        for (var i = 0; i < vm.cache.data.length; i++) {
            var task = vm.cache.data[i]
            if (task.id == id) {
                return task
            }
        }

        return null
    }

})