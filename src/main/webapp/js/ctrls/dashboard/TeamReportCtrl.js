/**
 * Created by Siddharth on 22-08-2017.
 */

app.controller("TeamReportCtrl", function ($scope, $http, $localStorage, $state, NgTableParams) {

    $scope.init = function ()
    {
        // Init Variables
        $scope.report = []

        // Table Parameters
        $scope.reportTableParams = new NgTableParams({}, {dataset: $scope.report})

        $http({
            method:     'GET',
            url:        '/api/reports/team',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            }
        })
            .then(
                function (response) {
                    $scope.report = response.data
                },
                function (error) {
                    console.log(error)
                    $state.go('home')
                }
            )
    }

    // Init View
    $scope.init()
})
