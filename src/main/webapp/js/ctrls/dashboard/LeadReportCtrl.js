/**
 * Created by Siddharth on 04-09-2017.
 */

app.controller("LeadReportCtrl", function ($scope, $rootScope, $http, $localStorage, $state, $filter, ExcelService, ToastService, DialogService) {

    /*-------------------------------------- Scope APIs ---------------------------------------*/
    $scope.export = function () {
        // Export only if some valid data is being displayed
        if ($scope.filteredValues.length) {
            // Call Excel Service
            ExcelService.export($scope.filteredValues, $scope.columns, "Lead-Report")
        } else {
            // Error toast
            ToastService.toast("Nothing to export !!!")
        }
    }

    $scope.showImage = function (filename) {
        DialogService.photoViewer(filename)
    }

    $scope.showLocation = function (latlng) {
        var latLngArr = latlng.split(',')
        DialogService.locationViewer(latLngArr[0], latLngArr[1])
    }

    /*-------------------------------------- Local APIs ---------------------------------------*/

    // Filter Related APIs
    $scope.resetFilters = function () {
        // Init Blank Filter
        $scope.filter = {
            selection:  {},
            text:       {},
            number:     {},
            date:       {},
            sort:       []
        }

        // Initialize filter data
        initFilterData()
    }

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

    $scope.filterSelect = function(column, value) {
        var idx = $scope.filter.selection[column].unselected.indexOf(value)
        if (idx == -1) {
            // Add to filter
            $scope.filter.selection[column].unselected.push(value)
        } else {
            // Remove from filter
            $scope.filter.selection[column].unselected.splice(idx, 1)
        }

        // Reapply filters on data
        $scope.applyFilters()
    }

    $scope.fromDateUpdate = function (fromDate, column) {
        $scope.filter.date[column].from = $filter('date')(fromDate, 'yyyy-MM-dd HH:mm:ss')
        $scope.applyFilters()
    }

    $scope.toDateUpdate = function (toDate, column) {
        $scope.filter.date[column].to = $filter('date')(toDate, 'yyyy-MM-dd HH:mm:ss')
        $scope.applyFilters()
    }

    // API to apply all filters
    $scope.applyFilters = function () {
        // Reset Report
        $scope.filteredValues = []

        // Apply filters
        for (var i = 0; i < $scope.values.length; i++) {
            var bAdd = true
            for (var j = 0; j < $scope.columns.length; j++) {
                var column = $scope.columns[j]
                var value = $scope.values[i][column.title]

                if ((column.type == 'selection') || (column.type == 'radioList')) {
                    if ($scope.filter.selection[column.title].unselected.indexOf(value) != -1) {
                        bAdd = false
                    }
                } else if (column.type == 'number') {
                    var lessThan = $scope.filter.number[column.title].lesserThan
                    var greaterThan = $scope.filter.number[column.title].greaterThan
                    if ((value == "-") && (lessThan || greaterThan)) {
                        bAdd = false
                    } else if ((lessThan && (value > lessThan)) || (greaterThan && (value < greaterThan))) {
                        bAdd = false
                    }
                } else if (column.type == 'text') {
                    var searchText = $scope.filter.text[column.title].search
                    if (searchText && (value.toLowerCase().search(searchText.toLowerCase()) == -1)) {
                        bAdd = false
                    }
                } else if (column.type == 'date') {
                    var from  = $scope.filter.date[column.title].from
                    var to  = $scope.filter.date[column.title].to
                    if ((from && (value < from)) || (to && (value > to))) {
                        bAdd = false
                    }
                }
            }

            if (bAdd) {
                $scope.filteredValues.push($scope.values[i])
            }
        }

        // Apply sorting
        $scope.filteredValues = $filter('orderBy')($scope.filteredValues, $scope.filter.sort)
    }
    
    // API to show / hide columns
    $scope.toggleColumns = function () {
        DialogService.toggleColumns($scope.columns, function (columns) {
            $scope.columns = columns
        })
    }

    /*-------------------------------------- Local APIs ---------------------------------------*/

    function initFilterData() {
        // Init Selection Options for each selection column type
        for (var i = 0; i < $scope.columns.length; i++) {
            var column = $scope.columns[i]
            if ((column.type == 'selection') || (column.type == 'radioList')) {
                // Init Selection options for the filter
                var selectFilter = {}
                selectFilter.options = []
                selectFilter.unselected = []

                // Iterate through values of this column
                for (var j = 0; j < $scope.values.length; j++) {
                    var value = $scope.values[j][column.title]

                    // Add unique entries to selection options
                    if (selectFilter.options.indexOf(value) == -1) {
                        selectFilter.options.push(value)
                    }
                }

                // Assign Selection filter for this column
                $scope.filter.selection[column.title] = selectFilter
            } else if (column.type == 'number') {
                $scope.filter.number[column.title] = {}
            } else if (column.type == 'text') {
                $scope.filter.text[column.title] = {}
            } else if (column.type == 'date') {
                $scope.filter.date[column.title] = {}
            }
        }
    }

    /*-------------------------------------- INIT ---------------------------------------------*/
    // Set menu and option
    $scope.nav.item       = MENU_ITEMS[MENU_ITEM_LEADS]
    $scope.nav.option     = ITEM_OPTIONS[ITEM_OPTION_REPORT]

    // Init Report Variables
    $scope.values = []
    $scope.columns = []
    $scope.filteredValues = []

    // Init filter
    $scope.resetFilters()

    // Get Lead report
    $scope.init = function () {
        $rootScope.showWaitingDialog("Please wait while we are fetching lead report...")

        $http({
            method:     'GET',
            url:        '/api/reports/lead',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            }
        })
        .then(
            function (response) {
                $rootScope.hideWaitingDialog()

                // Reset columns and values
                $scope.columns = []
                $scope.values = []
                $scope.filteredValues = []

                // Init columns
                var columns = response.data.columns
                for (var i = 0; i < columns.length; i++) {
                    var column = columns[i]
                    column.show = true
                    $scope.columns.push(column)
                }

                // Process report into columns
                var values = response.data.values
                for (var i = 0; i < values.length; i++) {
                    var row = {}
                    for (var j = 0; j < $scope.columns.length; j++) {
                        var column = $scope.columns[j]
                        row[column.title] = values[i][j]
                    }

                    $scope.values.push(row)
                }
                $scope.filteredValues = $scope.values

                // Init Filters
                $scope.resetFilters()
            },
            function (error) {
                $rootScope.hideWaitingDialog()
                // Show Error Toast
                ToastService.toast("Unable to load report !!!")
            }
        )
    }

    $scope.init()

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