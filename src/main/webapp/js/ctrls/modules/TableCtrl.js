/**
 * Created by Siddharth on 24-02-2018.
 */

app.controller('TableCtrl', function (  $rootScope, $scope, $window, $state, $http, $localStorage,
                                        TableService, DialogService, ToastService, FileService) {
    /* ----------------------------- INIT --------------------------------*/
    var vm  = this

    // Add Constants
    vm.TableConst = Constants.Table
    vm.FilterConst = Constants.Filter
    vm.TemplateConst = Constants.Template

    // Error / Waiting flags
    vm.bError = false
    vm.bWaiting = false

    // Get table object from service
    vm.table = TableService.activeTable

    // Init table properties
    vm.props = {
        bSingleSelect: false
    }
    if ($scope.tableProps) {
        vm.props.bSingleSelect = $scope.tableProps.bSingleSelect ? true : false
    }

    // Sync table data if required
    if (!vm.table.columns.length) {
        sync(true)
    }

    /* ----------------------------- Public APIs --------------------------------*/
    // API to show photo on new page
    vm.showImage = function (filename, colIdx) {
        // Get photo from server
        $rootScope.showWaitingDialog("Downloading..")

        // Get Photo
        $http({
            method:     'GET',
            url:        '/api/photos/get',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            },
            responseType: 'arraybuffer',
            params: {
                'filename': filename
            }
        })
            .then(
                function (response) {
                    FileService.downloadPhoto(response, vm.table.columns[colIdx].name + '_' + filename)
                    $rootScope.hideWaitingDialog()
                },
                function (error) {
                    ToastService.toast("Download Error : " + error.data.error)
                    $rootScope.hideWaitingDialog()
                    $scope.bError = true
                }
            )
    }

    // API to show location on map
    vm.showLocation = function (latlng, name) {
        // Split location into lat & lng
        var latLngArr = latlng.split(',')

        // Prepare Location array to send to location viewer
        var locations = []
        locations.push({
            title:      name,
            latitude:   latLngArr[0],
            longitude:  latLngArr[1]
        })

        // Open Location Viewer dialog
        DialogService.locationViewer(locations)
    }

    //API to show Lead Information Dialog
    vm.viewLead = function (id) {
        // Open Lead Viewer dialog
        DialogService.leadViewer(id)
    }

    //API to show Task Information Dialog
    vm.viewTask = function (id) {
        // Open Task Viewer dialog
        DialogService.taskViewer(id)
    }

    /*
     * Paging related methods
     */
    // Method to update rows per page
    vm.updateCount = function () {
        // Get pager
        var pager = vm.table.pager

        // Ensure count is with min - max range
        if (pager.count < 5) {
            pager.count = 5
        } else if (pager.count > 50) {
            pager.count = 50
        }

        // Reset start index
        pager.start = 0

        // Sync Data
        sync(false)
    }

    // Method to open a certain page number
    vm.openPage = function (pageNum) {
        // Get pager
        var pager = vm.table.pager

        // Set start index appropriately
        pager.start = (pageNum - 1) * pager.count

        // Sync data
        sync(false)
    }

    // Method to toggle sort status of a column
    vm.toggleSorting = function (colIdx) {
        // Get column object
        var column = vm.table.columns[colIdx]
        var colId = column.id

        switch (column.sortType) {
            case Constants.Table.SORT_NONE: {
                // Add ascending sortign for this column
                vm.table.sortOrder.push(colId)
                column.sortType = Constants.Table.SORT_ASC
                break
            }
            case Constants.Table.SORT_ASC: {
                // Update sorting to descing order
                column.sortType = Constants.Table.SORT_DESC
                break
            }
            case Constants.Table.SORT_DESC: {
                // Remove sorting for table
                vm.table.sortOrder.splice(vm.table.sortOrder.indexOf(colId), 1)
                column.sortType = Constants.Table.SORT_NONE
                break
            }
        }

        // Reset start Index in pager
        vm.table.pager.start = 0

        // Sync Data
        sync(false)
    }

    // method to toggle blanks filter for given column
    vm.toggleBlanks = function (colIdx) {
        // Get column object
        var column = vm.table.columns[colIdx]

        // Toggle blank filter
        column.bNoBlanks = !column.bNoBlanks

        // Reset start Index in pager
        vm.table.pager.start = 0

        // Reset selected rows
        vm.table.selectedRows = []

        // Sync Data
        sync(false)
    }

    // Method to perform action when any filters are changed
    vm.filterUpdated = function () {
        // Reset start Index in pager
        vm.table.pager.start = 0

        // Reset selected rows
        vm.table.selectedRows = []

        // Sync data
        sync(false)
    }

    // Method to toggle row selection status
    vm.toggleRowSelection = function (row) {
        // Check if this row is already selected
        var selectionIdx = vm.table.getSelectionIndex(row.id)
        if (selectionIdx != -1) {
            // If selected, mark as unselected
            vm.table.selectedRows.splice(selectionIdx, 1)
        } else {
            // If not selected, add to selection
            if (vm.props.bSingleSelect) {
                // Single selection
                vm.table.selectedRows = []
                vm.table.selectedRows.push({id: row.id, name: row.name})
            } else {
                // Multiple selection
                vm.table.selectedRows.push({id: row.id, name: row.name})
            }
        }
    }

    // Method to select all rows
    vm.toggleAll = function () {
        // Check if all rows are selected
        var bAllChecked = (vm.table.selectedRows.length == vm.table.totalRows)

        if (!bAllChecked) {
            // Check for maximum selection limit
            if (vm.table.totalRows > Constants.Table.MAX_SELECTION_COUNT) {
                ToastService.toast("Too many rows. Maximum " + Constants.Table.MAX_SELECTION_COUNT + " rows can be selected at once.")
                return
            }

            // Get all IDs from backend for given filter
            vm.bToggleWaiting = true
            vm.table.selectAll().then(
                function (response) {
                    vm.bToggleWaiting = false
                },
                function (error) {
                    vm.bToggleWaiting = false
                }
            )
        } else {
            // Reset selection array
            vm.table.selectedRows = []
        }
    }

    /* ----------------------------- Private APIs --------------------------------*/
    // Method to re-initialize data
    function sync(bColumns) {
        // Set flags
        vm.bError = false
        vm.bWaiting = true

        // Start Async Request to get data
        vm.table.sync(bColumns).then(
            // Success Callback
            function (success) {
                // Reset Waiting flag
                vm.bWaiting = false
            },
            // Error Callback
            function (error) {
                // Reset Waiting flag
                vm.bWaiting = false

                // Set Error flag
                vm.bError = true
            }
        )
    }

    /* ----------------------------- Event Listeners --------------------------------*/
    // Re sync data when Sync Event is triggered
    $scope.$on(Constants.Events.TABLE_SYNC, function (event, args) {
        // Reset selected rows
        vm.table.selectedRows = []

        // Sync data
        sync(false)
    })

    // Clear Filter Event
    $scope.$on(Constants.Events.TABLE_RESET, function (event, args) {
        // Reset table
        vm.table.reset()

        // Sync data along with columns
        sync(true)
    })

    // Toggle columns event
    $scope.$on(Constants.Events.TABLE_TOGGLE_COLUMNS, function (event, args) {
        DialogService.toggleColumns(vm.table.columns)
    })

    // Toggle columns event
    $scope.$on(Constants.Events.TABLE_EXPORT, function (event, args) {
        $rootScope.showWaitingDialog("Exporting...")
        vm.table.export().then(
            // Success Callback
            function (success) {
                $rootScope.hideWaitingDialog()
            },
            // Error Callback
            function (error) {
                $rootScope.hideWaitingDialog()
                ToastService.toast("Error : " + error.data.error + " !!!")
            }
        )
    })
})
