/**
 * Created by Siddharth on 04-09-2017.
 */

app.controller("LeadReportCtrl", function ($scope, $http, $localStorage, $state) {

    // Init Variables
    $scope.report = []

    // Get Report
    $http({
        method:     'GET',
        url:        '/api/reports/lead',
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
})