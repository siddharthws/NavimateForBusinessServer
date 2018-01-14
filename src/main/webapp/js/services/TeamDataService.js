/**
 * Created by amit on 28-12-2017.
 */
app.service('TeamDataService', function($rootScope, $http, $localStorage) {

    /* ----------------------------- INIT --------------------------------*/
    var vm = this
    vm.cache ={
        data: []
    }

    /* ----------------------------- APIs --------------------------------*/
    //API to get team data
    vm.sync = function (){
        $rootScope.showWaitingDialog("Please wait while we are fetching team details...")
        $http({
            method:     'GET',
            url:        '/api/users/team',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            }
        })
        .then(
            function (response) {
                    $rootScope.hideWaitingDialog()

                    // Update cache data
                    vm.cache.data = response.data
                    $rootScope.$broadcast(Constants.Events.TEAM_DATA_READY)
            },
            function (error) {
                    $rootScope.hideWaitingDialog()
                    ToastService.toast("Unable to load team !!!")
            })
    }

})