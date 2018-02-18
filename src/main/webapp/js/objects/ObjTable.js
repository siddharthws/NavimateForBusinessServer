/**
 * Created by Siddharth on 25-02-2018.
 */

app.factory('ObjTable', function($http, $q, $localStorage) {
    ObjTable = function (type) {
        // ----------------------------------- INIT ------------------------------------//
        var vm = this

        // Type fo table
        vm.type = Constants.Table.TYPE_INVALID

        // Rows and columns
        vm.columns = []
        vm.rows = []
        vm.totalRows    = 0

        // Pagination parameters
        vm.pager = {
            startIdx:   0,
            count:      Constants.Table.DEFAULT_COUNT_PER_PAGE
        }
        vm.sortOrder = []

        // Sync request serializing logic
        var syncCanceller = null
        var bSyncOngoing = false

        // ----------------------------------- Public Methods ------------------------------------//
        // Method to sync tabular data with filters from backend
        vm.sync = function (bColumns) {
            // Cancel ongoing sync & create new canceller
            if (bSyncOngoing) {
                syncCanceller.resolve()
                bSyncOngoing = false
            }
            syncCanceller = $q.defer()

            // Defer object to resolve request
            var deferred = $q.defer()

            // Trigger http request to sync data
            bSyncOngoing = true
            $http({
                method:     'POST',
                url:        "/api/manager/" + Constants.Table.URL_PREFIX[vm.type] + "/getTable",
                headers:    {
                    'X-Auth-Token':    $localStorage.accessToken
                },
                data:       {
                    bColumns: bColumns,
                    filter: getFilter()
                }
            }).then(
                function (response) {
                    // Mark sync complete flag
                    bSyncOngoing = false

                    // Reset active table columns and rows
                    handleSyncResult(response.data)

                    // Resolve promise
                    deferred.resolve(null)
                },
                function (error) {
                    // Ignore if request was cancelled
                    if (error.status == -1) {
                        return
                    }

                    // Mark sync complete flag
                    bSyncOngoing = false

                    // Reject promise
                    deferred.reject(null)
                })

            // Return promise
            return deferred.promise
        }

        // Method to get number of pages based on data
        vm.pageCount = function () {
            return Math.ceil(vm.totalRows / vm.pager.count)
        }

        // ----------------------------------- Private Methods ------------------------------------//
        // Method to handle server data after sync request
        function handleSyncResult(data) {
            // Set columns
            if (data.columns) {
                // Reset current columns
                vm.columns = data.columns

                // Re-initialize filters
                initFilters()
            }

            // Set rows
            vm.rows         = data.rows

            // Update column sizes
            refreshColSizes()
        }

        // Method to get filter to be sent to server
        function getFilter () {
            // Prepare sort list for server
            var sortList = []
            vm.sortOrder.forEach(function (colId) {
                // Get column filter
                var column = getColumnById(colId)

                // Push a sort object into sort list
                sortList.push({
                    colId: colId,
                    type: column.sortType
                })
            })

            // Return filter object
            return {
                pager: vm.pager,
                sortList: sortList
            }
        }

        // Method to refresh column sizes as per latest data
        function refreshColSizes () {
            // Iterate through each column
            vm.columns.forEach(function (column) {
                // Set sizing for Location / Image columns as small
                if (column.type == Constants.Template.FIELD_TYPE_SIGN ||
                    column.type == Constants.Template.FIELD_TYPE_PHOTO ||
                    column.type == Constants.Template.FIELD_TYPE_LOCATION) {
                    column.size = Constants.Table.COL_SIZE_S
                } else {
                    // Get total number of character in columns
                    var charCount = column.name.length
                    vm.rows.forEach(function (row) {
                        var value = row.values[column.id]
                        charCount += value.length
                    })

                    // Get average character per row
                    var charAvg = charCount / (vm.rows.length + 1)

                    // Set column sizing as per max length
                    if (charAvg < 5) {
                        column.size = Constants.Table.COL_SIZE_S
                    } else if (charAvg < 20) {
                        column.size = Constants.Table.COL_SIZE_M
                    } else {
                        column.size = Constants.Table.COL_SIZE_L
                    }
                }
            })
        }

        // Method to init column filters
        function initFilters () {
            // Iterate through column
            vm.columns.forEach(function (column) {
                // Set properties
                column.sortType = Constants.Table.SORT_NONE
            })
        }

        // Method to get column by id
        function getColumnById (id) {
            for (var i = 0; i < vm.columns.length; i++) {
                if (vm.columns[i].id == id) {
                    return vm.columns[i]
                }
            }

            return null
        }
    }

    return ObjTable
})
