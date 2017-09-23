/**
 * Created by Siddharth on 04-09-2017.
 */

app.controller("LeadReportCtrl", function ($scope, $http, $localStorage, $state, ExcelService, ToastService) {

    /*-------------------------------------- Scope APIs ---------------------------------------*/
    $scope.export = function () {
        // Export only if some valid data is being displayed
        if ($scope.filteredReport.length) {
            // Get table to export
            var table = $('.lead-report-table').get(0)

            // Call Excel Service
            ExcelService.export(table, "Lead-Report")
        } else {
            // Error toast
            ToastService.toast("Nothing to export !!!")
        }
    }

    /*-------------------------------------- Local APIs ---------------------------------------*/
    /*-------------------------------------- INIT ---------------------------------------------*/
    // Set menu and option
    $scope.selection.item       = MENU_ITEMS[MENU_ITEM_LEADS]
    $scope.selection.option     = ITEM_OPTIONS[ITEM_OPTION_REPORT]

    // Init Report Variables
    $scope.report           = []
    $scope.filteredReport   = []

    // Get team report
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
            $scope.filteredReport = $scope.report
        },
        function (error) {
            // Show Error Toast
            ToastService.toast("Unable to load report !!!")
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
})