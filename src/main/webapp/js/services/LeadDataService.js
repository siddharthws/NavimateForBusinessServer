/**
 * Created by aroha on 13-01-2017.
 */
app.service('LeadDataService', function($rootScope, $http, $localStorage) {

    /* ----------------------------- INIT --------------------------------*/
    var vm = this
    vm.cache ={
        data: []
    }

    /* ----------------------------- APIs --------------------------------*/
    //API to get Lead data
    vm.sync = function (){
        $rootScope.showWaitingDialog("Please wait while we are fetching lead details...")
        $http({
            method:     'GET',
            url:        '/api/users/lead',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            }
        }).then(
                function (response) {
                    $rootScope.hideWaitingDialog()

                    // Update cache data
                    vm.cache.data = response.data
                    $rootScope.$broadcast(Constants.Events.LEAD_DATA_READY)
                },
                function (error) {
                    $rootScope.hideWaitingDialog()
                    ToastService.toast("Unable to load leads !!!")
                })
    }

})