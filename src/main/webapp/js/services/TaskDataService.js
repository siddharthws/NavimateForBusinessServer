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
        $rootScope.showWaitingDialog("Please wait while we are fetching Tasks...")
        $http({
            method:     'GET',
            url:        '/api/users/task',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            }
        }).then(
            function (response) {
                $rootScope.hideWaitingDialog()

                // Update cache data
                vm.cache.data = response.data
                $rootScope.$broadcast(Constants.Events.TASK_DATA_READY)
            },
            function (error) {
                $rootScope.hideWaitingDialog()
                ToastService.toast("Unable to load Tasks !!!")
            })
    }

})