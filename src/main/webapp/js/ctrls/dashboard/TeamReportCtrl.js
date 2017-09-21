/**
 * Created by Siddharth on 22-08-2017.
 */

app.controller("TeamReportCtrl", function ($scope, $http, $localStorage, $state, ExcelService) {

    // Set menu and option
    $scope.selection.item       = MENU_ITEMS[MENU_ITEM_TEAM]
    $scope.selection.option     = ITEM_OPTIONS[ITEM_OPTION_REPORT]

    // Init Variables
    $scope.report = []

    // Get team report
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

    // Hack to persist multiselect dropdowns after clicking on dropdown items
    $('body').on('click', function (e) {
        if (!$('.dropdown.dropdown-multiselect').is(e.target)
            && $('.dropdown.dropdown-multiselect').has(e.target).length === 0
            && $('.show').has(e.target).length === 0) {
            $('.dropdown.dropdown-multiselect .dropdown-menu').removeClass('show')
        }
    })

    $('.dropdown.dropdown-multiselect > button').on('click', function (e) {
        $($(this).parent()).find('.dropdown-menu').toggleClass('show')
    })

    $scope.export = function () {
        // Get table to export
        var table = $('.team-report-table').get(0)

        // Call Excel Service
        ExcelService.export(table, "Team-Report")
    }
})
