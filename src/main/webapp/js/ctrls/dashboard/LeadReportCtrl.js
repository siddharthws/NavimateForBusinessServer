/**
 * Created by Siddharth on 04-09-2017.
 */

app.controller("LeadReportCtrl", function ($scope, $http, $localStorage, $state, $filter, ExcelService, ToastService) {

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

    // Init Filter
    $scope.filter = {
        rep: {
            selection: []
        },
        lead: {
            selection: []
        },
        status: {
            selection: []
        },
        sort: []
    }

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

    // Filter Related APIs
    $scope.filterSort = function(property, bReverse) {
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

    $scope.filterSelect = function(property, value) {
        var idx = $scope.filter[property].selection.indexOf(value)
        if (idx == -1) {
            // Add to filter.
            $scope.filter[property].selection.push(value)
        } else {
            // Remove from filter
            $scope.filter[property].selection.splice(idx, 1)
        }

        // Reapply filters on data
        $scope.applyFilters()
    }

    // API to apply all filters
    $scope.applyFilters = function () {
        // Reset Report
        $scope.filteredReport = []

        // Apply select filters
        $scope.report.forEach(function (row) {
            if (($scope.filter.lead.selection.indexOf(row.lead) == -1) &&
                ($scope.filter.rep.selection.indexOf(row.rep) == -1) &&
                ($scope.filter.status.selection.indexOf(row.status) == -1))
            {
                $scope.filteredReport.push(row)
            }
        })

        // Apply sorting
        $scope.filteredReport = $filter('orderBy')($scope.filteredReport, $scope.filter.sort)
    }
})