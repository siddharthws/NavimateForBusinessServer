/**
 * Created by Siddharth on 22-08-2017.
 */

app.controller("TeamReportCtrl", function ($scope, $http, $localStorage, $state, $filter, ExcelService) {

    // Set menu and option
    $scope.selection.item       = MENU_ITEMS[MENU_ITEM_TEAM]
    $scope.selection.option     = ITEM_OPTIONS[ITEM_OPTION_REPORT]

    // Init Report Variables
    $scope.report = []
    $scope.filteredReport = []

    // Init Filter
    $scope.filter = {
        sort: []
    }

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
            $scope.filteredReport = $scope.report
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

    // Filter Related APIs
    $scope.filterSort = function(property, bReverse) {
        console.log(property + "  " + bReverse)
        if (bReverse) {
            // Remove forward if present
            if ($scope.filter.sort.indexOf(property) != -1) {
                $scope.filter.sort.splice($scope.filter.sort.indexOf(property), 1)
            }

            property = '-' + property
        } else {
            // Remove reverse if present
            if ($scope.filter.sort.indexOf('-' + property) != -1) {
                $scope.filter.sort.splice($scope.filter.sort.indexOf('-' + property), 1)
            }
        }

        // Add / remove property
        if ($scope.filter.sort.indexOf(property) != -1) {
            $scope.filter.sort.splice($scope.filter.sort.indexOf(property), 1)
        } else {
            $scope.filter.sort.push(property)
        }

        // Re-apply filters
        $scope.applyFilters()
    }

    // API to apply all filters
    $scope.applyFilters = function () {
        console.log("Filtering")
        // Reset Report
        $scope.filteredReport = []

        // Apply select filters
        $scope.report.forEach(function (row) {
            $scope.filteredReport.push(row)
        })

        // Apply sorting
        $scope.filteredReport = $filter('orderBy')($scope.filteredReport, $scope.filter.sort)
    }
})
