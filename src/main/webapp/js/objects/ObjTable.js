/**
 * Created by Siddharth on 25-02-2018.
 */

app.factory('ObjTable', function($http, $q, $localStorage, FileService) {
    ObjTable = function (type) {
        // ----------------------------------- INIT ------------------------------------//
        var vm = this

        // Type fo table
        vm.type = type

        // Rows and columns
        vm.columns = []
        vm.rows = []
        vm.totalRows    = 0
        vm.selectedRows = []

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
                timeout:    syncCanceller.promise,
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
                    filter: getFilter(),
                    exportParams: getExportParams()
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
                    filter: getFilter()
                }
            }).then(
                function (response) {
                    // Add all IDs to selected Row IDs
                    vm.selectedRows = response.data

                    // Resolve promise
                    deferred.resolve(null)
                },
                function (error) {
                    // Reject promise
                    deferred.reject(error)
                })

            return deferred.promise
        }

        // Method to clear all filters
        vm.clearFilters = function () {
            initFilters()
        }

        // Method to get number of pages based on data
        vm.getPageCount = function () {
            return Math.ceil(vm.totalRows / vm.pager.count)
        }

        // Method to get IDs of all selected objects
        vm.getSelectedIds = function () {
            var ids = []
            vm.selectedRows.forEach(function (row) {
                ids.push(row.id)
            })

            return ids
        }

        // Method to check if a given id is selected
        vm.getSelectionIndex = function (id) {
            for (var i = 0; i < vm.selectedRows.length; i++) {
                var row = vm.selectedRows[i]
                if (row.id == id) {
                    return i
                }
            }

            return -1
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
            vm.totalRows    = data.totalRows

            // Update column sizes
            refreshColSizes()
        }

        // Method to get filter to be sent to server
        function getFilter () {
            // Prepare column filters for server
            var colFilters = []
            vm.columns.forEach(function (column) {
                colFilters.push({
                    colId: column.id,
                    type: column.filter.type,
                    value: column.filter.value,
                    bNoBlanks: column.bNoBlanks
                })
            })

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
                sortList: sortList,
                colFilters: colFilters
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
                // Add column filter
                column.filter = getFilterForColumn(column)

                // Set properties
                column.sortType = Constants.Table.SORT_NONE
                column.bNoBlanks = false
                column.bShow = true
            })
        }

        // Method to init filter for a given column
        function getFilterForColumn(column) {
            var filter = {
                type: Constants.Filter.TYPE_NONE
            }

            switch (column.type) {
                case Constants.Template.FIELD_TYPE_TEXT:
                case Constants.Template.FIELD_TYPE_RADIOLIST:
                case Constants.Template.FIELD_TYPE_CHECKLIST:
                case Constants.Template.FIELD_TYPE_CHECKBOX: {
                    filter.type = Constants.Filter.TYPE_TEXT
                    filter.value = ""
                    break
                }
                case Constants.Template.FIELD_TYPE_LEAD:  {
                    filter.type = Constants.Filter.TYPE_OBJECT
                    filter.value = ""
                    break
                }
                case Constants.Template.FIELD_TYPE_NUMBER: {
                    filter.type = Constants.Filter.TYPE_NUMBER
                    filter.value = {from: null, to: null}
                    break
                }
                case Constants.Template.FIELD_TYPE_DATE: {
                    filter.type = Constants.Filter.TYPE_DATE
                    filter.value = {from: null, to: null}
                    break
                }
            }

            return filter
        }

        // Method to get export parameters
        function getExportParams () {
            var order = []

            vm.columns.forEach(function (column) {
                if (column.bShow) {
                    order.push(column.id)
                }
            })

            return {
                selection: vm.getSelectedIds(),
                order: order
            }
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
