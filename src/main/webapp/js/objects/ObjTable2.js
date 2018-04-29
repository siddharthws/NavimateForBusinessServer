/**
 * Created by Siddharth on 26-04-2018.
 */

app.factory('ObjTable2', function($http, $q, $localStorage, FileService) {
    ObjTable2 = function (type, getColumnsCb, parseServerResponseCb, parseFilterCb, parseSorterCb, exportParamsCb) {
        // ----------------------------------- INIT ------------------------------------//
        var vm = this

        // Type of table
        vm.type = type

        // Table Columns (ObjColumn)
        vm.columns      = []

        // Row cache, count & selection status
        vm.rows         = []
        vm.rowCount     = 0
        vm.selection    = []

        // Table Pager
        vm.pager = {
            startIdx:   0,
            count:      Constants.Table.DEFAULT_COUNT_PER_PAGE
        }
        vm.pageCount = 0

        // Sorting
        vm.sortOrder = []

        // Sync request serializing logic
        var syncCanceller = null
        var bSyncOngoing = false

        // ----------------------------------- Public Methods ------------------------------------//
        // Method to sync tabular data with filters from backend
        vm.sync = function () {
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
                timeout:    syncCanceller.promise,
                headers:    {
                    'X-Auth-Token':    $localStorage.accessToken
                },
                data:       {
                    filter: parseFilterCb(),
                    sorter: parseSorterCb(),
                    pager: vm.pager
                }
            }).then(
                function (response) {
                    // Mark sync complete flag
                    bSyncOngoing = false

                    // Reset active table columns and rows
                    vm.rows = parseServerResponseCb(response.data)

                    // Set page & row count
                    vm.rowCount = response.data.rowCount
                    vm.pageCount = Math.ceil(vm.rowCount / vm.pager.count)

                    // Update column sizes
                    refreshColSizes()

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

        vm.reset = function () {
            // Reset Pager
            vm.pager = {
                startIdx:   0,
                count:      Constants.Table.DEFAULT_COUNT_PER_PAGE
            }

            // Reset sorter
            vm.sortOrder = []

            // Reset Columns
            vm.columns = getColumnsCb()

            // Sync Data
            return vm.sync()
        }

        // Method to export data to excel
        vm.export = function () {
            // Defer object to resolve request
            var deferred = $q.defer()

            // Trigger http request to export
            $http({
                method:     'POST',
                url:        "/api/manager/" + Constants.Table.URL_PREFIX[vm.type] + "/export",
                headers:    {
                    'X-Auth-Token':    $localStorage.accessToken
                },
                responseType: 'arraybuffer',
                data:       {
                    filter: parseFilterCb(),
                    sorter: parseSorterCb(),
                    exportParams: exportParamsCb()
                }
            }).then(
                function (response) {
                    FileService.download(response, "Navimate-" + Constants.Table.URL_PREFIX[vm.type])

                    // Resolve promise
                    deferred.resolve(null)
                },
                function (error) {
                    // Reject promise
                    deferred.reject(error)
                })

            // Return promise
            return deferred.promise
        }

        // Method to get all IDs for given filter
        vm.selectAll = function () {
            var deferred = $q.defer()

            // Trigger http request to get lead IDs
            $http({
                method:     'POST',
                url:        "/api/manager/" + Constants.Table.URL_PREFIX[vm.type] + "/getIds",
                headers:    {
                    'X-Auth-Token':    $localStorage.accessToken
                },
                data:       {
                    filter: parseFilterCb(),
                    sorter: parseSorterCb(),
                    pager: vm.pager
                }
            }).then(
                function (response) {
                    // Add all IDs to selected Row IDs
                    vm.selection = response.data

                    // Resolve promise
                    deferred.resolve(null)
                },
                function (error) {
                    // Reject promise
                    deferred.reject(error)
                })

            return deferred.promise
        }

        // Method to get IDs of all selected objects
        vm.getSelectedIds = function () {
            var ids = []
            vm.selection.forEach(function (row) {
                ids.push(row.id)
            })

            return ids
        }

        // Method to check if a given id is selected
        vm.getSelectionIndex = function (id) {
            for (var i = 0; i < vm.selection.length; i++) {
                var row = vm.selection[i]
                if (row.id == id) {
                    return i
                }
            }

            return -1
        }

        // ----------------------------------- Private Methods ------------------------------------//

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
                        if (value) {
                            charCount += value.length
                        }
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

        // Method to get column by id
        vm.getColumnById = function (id) {
            for (var i = 0; i < vm.columns.length; i++) {
                if (vm.columns[i].id == id) {
                    return vm.columns[i]
                }
            }

            return null
        }
    }

    return ObjTable2
})
