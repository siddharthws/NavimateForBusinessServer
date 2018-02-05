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
        $http({
            method:     'GET',
            url:        '/api/users/team',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            }
        }).then(
            function (response) {
                    // Update cache data
                    vm.cache.data = response.data
                    $rootScope.$broadcast(Constants.Events.TEAM_DATA_READY)
            },
            function (error) {
                    $rootScope.$broadcast(Constants.Events.DATA_LOAD_ERROR)
            })
    }

    // APi to get lead by ID
    vm.getById = function (id) {
        for (var i = 0; i < vm.cache.data.length; i++) {
            var rep = vm.cache.data[i]
            if (rep.id == id) {
                return rep
            }
        }

        return null
    }

})