/**
 * Created by Siddharth on 04-09-2017.
 */

app.controller("LeadManageCtrl", function ($scope, $http, $localStorage, $state, ExcelService, DialogService, NgTableParams) {

    // Init Variables
    $scope.leads = []

    // Table Parameters
    $scope.leadTableParams = new NgTableParams({}, {dataset: $scope.leads})

    // Send request to get list of leads
    $http({
        method:     'GET',
        url:        '/api/users/lead',
        headers:    {
            'X-Auth-Token':    $localStorage.accessToken
        }
    })
    .then(
        function (response) {
            $scope.leads = response.data
        },
        function (error) {
            console.log(error)
            $state.go('home')
        }
    )

    // Excel related APIs
    $scope.excelRead = function (workbook) {
        ExcelService.excelRead(workbook).then(
            function (response) {
                // Launch Map Editor Dialod
                DialogService.mapEditor(response.data)
            },
            function (error) {
                // Excel File Parse Error
                console.log(error)
            }
        )
    }

    $scope.excelError = function (e) {
        console.log("Excel Read Error = " + e)
    }

})
