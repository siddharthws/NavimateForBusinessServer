/**
 * Created by Siddharth on 04-12-2017.
 */

// Controller for Table View
app.controller('TableCtrl', function ($scope, $rootScope, $state, $window, $filter, ExcelService, DialogService) {
    var vm = this

    /*------------------------------------ HTML APIs --------------------------------*/
    // Filter Related APIs
    vm.resetFilters = function () {
        // Init Blank Filter
        vm.filter = []

        // Initialize filter data
        for (var i = 0; i < vm.columns.length; i++) {
            var column = vm.columns[i]
            var filter = {
                type: column.filterType
            }

            // Init filter object
            if (column.filterType == $rootScope.Constants.Filter.TYPE_SELECTION) {
                // Init selection filter object
                filter.selection = { options : []}

                // Get all unique values for this column
                var optionNames = []
                for (var j = 0; j < vm.values.length; j++) {
                    var value = vm.values[j]['Col' + column.id]

                    // Add unique entries to selection options
                    if (optionNames.indexOf(value) == -1) {
                        optionNames.push(value)
                    }
                }

                // Create selection option for each value
                for (var j = 0; j < optionNames.length; j++) {
                    filter.selection.options.push({
                        name: optionNames[j],
                        checked: true
                    })
                }
            } else if (column.filterType == $rootScope.Constants.Filter.TYPE_NUMBER) {
                filter.number = {}
            } else if (column.filterType == $rootScope.Constants.Filter.TYPE_TEXT) {
                filter.text = {}
            } else if (column.filterType == $rootScope.Constants.Filter.TYPE_DATE) {
                filter.date = {}
            }

            // Add sorting object
            filter.sort = { up: false, down: false}

            // Add as column filter
            column.filter = filter
        }
    }

    vm.isFilterActive = function (colIdx) {
        var bActive = false
        var filter = vm.columns[colIdx].filter

        if (filter.type == $rootScope.Constants.Filter.TYPE_SELECTION) {
            var options = filter.selection.options

            // Check if any item in selection is unchecked
            for (var i = 0; i < options.length; i++) {
                if (!options[i].checked) {
                    bActive = true
                    break
                }
            }
        } else if (filter.type == $rootScope.Constants.Filter.TYPE_TEXT) {
            if (filter.text.search) {
                bActive = true
            }
        } else if (filter.type == $rootScope.Constants.Filter.TYPE_NUMBER) {
            if (filter.number.greaterThan || filter.number.lesserThan) {
                bActive = true
            }
        } else if (filter.type == $rootScope.Constants.Filter.TYPE_DATE) {
            if (filter.date.from || filter.date.to) {
                bActive = true
            }
        }

        // Check for sorting
        if (filter.sort.up || filter.sort.down) {
            bActive = true
        }

        return bActive
    }

    // API to apply all filters
    vm.applyFilters = function () {
        // Reset Report
        vm.filteredValues = []

        // Apply filters
        for (var i = 0; i < vm.values.length; i++) {
            var bAdd = true
            for (var j = 0; j < vm.columns.length; j++) {
                var filter = vm.columns[j].filter
                var value = vm.values[i]['Col' + vm.columns[j].id]

                if (filter.type == $rootScope.Constants.Filter.TYPE_SELECTION) {
                    // Check if this option is selected
                    filter.selection.options.forEach(function (option) {
                        if ((option.name == value) && (!option.checked)) {
                            bAdd = false
                        }
                    })
                } else if (filter.type == $rootScope.Constants.Filter.TYPE_NUMBER) {
                    var lessThan = filter.number.lesserThan
                    var greaterThan = filter.number.greaterThan
                    if ((value == "-") && (lessThan || greaterThan)) {
                        bAdd = false
                    } else if ((lessThan && (value > lessThan)) || (greaterThan && (value < greaterThan))) {
                        bAdd = false
                    }
                } else if (filter.type == $rootScope.Constants.Filter.TYPE_TEXT) {
                    var searchText = filter.text.search
                    if (searchText && (value.toLowerCase().search(searchText.toLowerCase()) == -1)) {
                        bAdd = false
                    }
                } else if (filter.type == $rootScope.Constants.Filter.TYPE_DATE) {
                    var from  = filter.date.from
                    var to  = filter.date.to
                    if ((from && (value < from)) || (to && (value > to))) {
                        bAdd = false
                    }
                }
            }

            if (bAdd) {
                vm.filteredValues.push(vm.values[i])
            }
        }

        // Apply sorting
        applySorting()
    }

    // Date filter related APIs
    vm.fromDateUpdate = function (fromDate, colIdx) {
        vm.columns[colIdx].filter.date.from = $filter('date')(fromDate, 'yyyy-MM-dd HH:mm:ss')
        vm.applyFilters()
    }

    vm.toDateUpdate = function (toDate, colIdx) {
        vm.columns[colIdx].filter.date.to = $filter('date')(toDate, 'yyyy-MM-dd HH:mm:ss')
        vm.applyFilters()
    }

    // Sorting APIs
    vm.sortUp = function (colIdx) {
        // Get column filter
        var filter = vm.columns[colIdx].filter

        // Toggle sorting
        if (filter.sort.up) {
            filter.sort.up = false
        } else {
            filter.sort.up = true
            filter.sort.down = false
        }

        // Re-apply Sorting
        applySorting()
    }

    vm.sortDown = function (colIdx) {
        // Get column filter
        var filter = vm.columns[colIdx].filter

        // Toggle sorting
        if (filter.sort.down) {
            filter.sort.down = false
        } else {
            filter.sort.down = true
            filter.sort.up = false
        }

        // Re-apply Sorting
        applySorting()
    }

    // APis to check what type of value need to be displayed as table-data
    vm.isTextValue = function (column, value) {
        if ((value == '-') ||
            (column.type == $rootScope.Constants.Template.FIELD_TYPE_TEXT) ||
            (column.type == $rootScope.Constants.Template.FIELD_TYPE_NUMBER) ||
            (column.type == $rootScope.Constants.Template.FIELD_TYPE_RADIOLIST) ||
            (column.type == $rootScope.Constants.Template.FIELD_TYPE_CHECKLIST) ||
            (column.type == $rootScope.Constants.Template.FIELD_TYPE_DATE)) {
            return true
        }

        return false
    }

    vm.isImageValue = function (column, value) {
        if ((value != '-') &&
            ((column.type == $rootScope.Constants.Template.FIELD_TYPE_PHOTO) ||
            (column.type == $rootScope.Constants.Template.FIELD_TYPE_SIGN))) {
            return true
        }

        return false
    }

    vm.isLocationValue = function (column, value) {
        if ((value != '-') &&
            (column.type == $rootScope.Constants.Template.FIELD_TYPE_LOCATION)) {
            return true
        }

        return false
    }

    // API to show photo on new page
    vm.showImage = function (filename) {
        $window.open($state.href('photos', {name: filename}), "_blank")
    }

    // API to show location on map
    vm.showLocation = function (latlng) {
        var latLngArr = latlng.split(',')
        DialogService.locationViewer(latLngArr[0], latLngArr[1])
    }

    // API to show / hide columns
    vm.toggleColumns = function () {
        DialogService.toggleColumns(vm.columns, function (columns) {
            vm.columns = columns
        })
    }

    // API to export data to excel
    vm.export = function () {
        // Don't export if no values present
        if (!vm.filteredValues.length) {
            ToastService.toast("No data to export !!!")
            return
        }

        // Check if any columns are showing
        var bValidColumns = false
        for (var i  = 0; i < vm.columns.length; i++) {
            if (vm.columns[i].show) {
                bValidColumns = true
                break
            }
        }

        // Don't export if no columns selected
        if (!bValidColumns) {
            ToastService.toast("No columns to export !!!")
            return
        }

        // Call Excel Service to perform export
        ExcelService.export(vm.filteredValues, vm.columns, $scope.tableParams.exportName)
    }

    // APi to sync table is provided by parent
    vm.syncTable = $scope.tableParams.functions.syncTable

    /*------------------------------------ Local APIs --------------------------------*/

    // Data Init Callback. Triggered by parent when data syncing is complete
    $scope.tableParams.functions.initTable = function () {
        // Init Columns
        vm.columns = []
        for (var i = 0; i < $scope.tableParams.columns.length; i++) {
            var column = $scope.tableParams.columns[i]
            column.show = true
            column.id = i
            vm.columns.push(column)
        }

        // Init Values
        vm.values            = []
        for (var i = 0; i < $scope.tableParams.values.length; i++) {
            var row = {}
            for (var j = 0; j < vm.columns.length; j++) {
                row['Col' + vm.columns[j].id] = $scope.tableParams.values[i][j]
            }

            vm.values.push(row)
        }
        vm.filteredValues = vm.values

        // Reset Filters
        vm.resetFilters()
    }

    // API to update filtered values with current sorting selection
    function applySorting () {
        // Create sorting array for $filter
        var sortingArray = []
        vm.columns.forEach(function (column, i) {
            if (column.filter.sort.down) {
                sortingArray.push('Col' + column.id)
            } else if (column.filter.sort.up) {
                sortingArray.push('-Col' + column.id)
            }
        })

        // Apply sort on filtered values
        vm.filteredValues = $filter('orderBy')(vm.filteredValues, sortingArray)
    }

    /*------------------------------------ INIT --------------------------------*/
    // Init Objects
    vm.columns           = []
    vm.values            = []
    vm.filteredValues    = []
    vm.style             = $scope.tableParams.style

    // Hack to persist multiselect dropdowns after clicking on dropdown items
    $(document).on('click', '.dropdown-multiselect .dropdown-menu', function (e) {
        e.stopPropagation();
    })
})
