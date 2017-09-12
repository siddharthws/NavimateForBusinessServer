/**
 * Created by Siddharth on 04-09-2017.
 */

app.controller("LeadReportCtrl", function ($scope, $http, $localStorage, $state, ExcelService) {

    // Set menu and option
    $scope.selection.item       = MENU_ITEMS[MENU_ITEM_LEADS]
    $scope.selection.option     = ITEM_OPTIONS[ITEM_OPTION_REPORT]

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

    $scope.export = function () {
        // Get table to export
        var table = $('.lead-report-table').get(0)

        // Call Excel Service
        ExcelService.export(table, "Lead-Report")
    }
})