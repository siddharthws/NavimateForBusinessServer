/**
 * Created by Siddharth on 24-02-2018.
 */

app.controller('TableCtrl', function (  $scope, $window, $state,
                                        TableService, DialogService) {
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

    // Sync table data if required
    if (!vm.table.columns.length) {
        sync(true)
    }

    /* ----------------------------- Public APIs --------------------------------*/
    // API to show photo on new page
    vm.showImage = function (filename) {
        $window.open($state.href('photos', {name: filename}), "_blank")
    }

    // API to show location on map
    vm.showLocation = function (latlng) {
        // Split location into lat & lng
        var latLngArr = latlng.split(',')

        // Prepare Location array to send to location viewer
        var locations = []
        locations.push({
            title:      "Picked Location",
            latitude:   latLngArr[0],
            longitude:  latLngArr[1]
        })

        // Open Location Viewer dialog
        DialogService.locationViewer(locations)
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
        pager.startIdx = 0

        // Sync Data
        sync(false)
    }

    // Method to open a certain page number
    vm.openPage = function (pageNum) {
        // Get pager
        var pager = vm.table.pager

        // Set start index appropriately
        pager.startIdx = (pageNum - 1) * pager.count

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
        vm.table.pager.startIdx = 0

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
        vm.table.pager.startIdx = 0

        // Sync Data
        sync(false)
    }

    // Method to perform action when any filters are changed
    vm.filterUpdated = function () {
        // Reset start Index in pager
        vm.table.pager.startIdx = 0

        // Sync data
        sync(false)
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
        // Sync data
        sync(false)
    })

    // Clear Filter Event
    $scope.$on(Constants.Events.TABLE_CLEAR_FILTERS, function (event, args) {
        // Reset selection / paging etc...
        vm.table.pager.startIdx = 0

        // Re-initialize filters
        vm.table.clearFilters()

        // Sync data
        sync(false)
    })
})
