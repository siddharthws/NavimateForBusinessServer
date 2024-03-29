/**
 * Created by Chandel on 06-02-2018.
 */
app.service('LocReportDS', function($http, $localStorage, $q) {

    /* ----------------------------- INIT --------------------------------*/
    var vm = this
    vm.cache = []

    /* ----------------------------- APIs --------------------------------*/
    // Method to reset Service
    vm.reset = function () {
        // Reset cache
        vm.cache = []
    }

    //API to get Lead data
    vm.sync = function (repId, date){
        var deferred = $q.defer()
        $http({
            method:     'GET',
            url:        '/api/users/locationReport',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            },
            params:     {
                'repId':    repId,
                'selectedDate': date
            }
        }).then(
            function (response) {
                // Update cache data
                vm.cache = response.data
                deferred.resolve(null)
            },
            function (error) {
                deferred.reject(null)
            })
        return deferred.promise
    }
})