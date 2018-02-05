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
        $http({
            method:     'GET',
            url:        '/api/users/lead',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            }
        }).then(
                function (response) {
                    // Update cache data
                    vm.cache.data = response.data
                    $rootScope.$broadcast(Constants.Events.LEAD_DATA_READY)
                },
                function (error) {
                    $rootScope.$broadcast(Constants.Events.DATA_LOAD_ERROR)
                })
    }

    // APi to get lead by ID
    vm.getById = function (id) {
        for (var i = 0; i < vm.cache.data.length; i++) {
            var lead = vm.cache.data[i]
            if (lead.id == id) {
                return lead
            }
        }

        return null
    }
})