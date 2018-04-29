/**
 * Created by Siddharth on 22-08-2017.
 */

app.controller("TaskManageCtrl",
                function (  $scope, $rootScope, $http, $localStorage, $state,
                            DialogService, ToastService, TableService, NavService, ImportService) {
    var vm = this

    // Set Active Tab and Menu
    NavService.setActive(NavService.tasks, 0)

    // Set task table as active
    TableService.activeTable = TableService.taskTable
    vm.table = TableService.activeTable

    /*-------------------------------- Scope APIs --------------------------------*/
    vm.add = function () {
        // Launch Task Creator dialog
        DialogService.taskEditor(null, vm.sync)
    }

    // APIs for table based actions
    vm.export = function () {
        // Broadcast Toggle Columns Event
        $scope.$broadcast(Constants.Events.TABLE_EXPORT)
    }

    // API to import tasks
    vm.import = function (file) {
        // Show waiting dialog
        $rootScope.showWaitingDialog("Importing tasks. This may take some time...")

        // Perform import
        ImportService.import("/api/manager/tasks/import", file).then(
            // Success callback
            function () {
                // Sync data again
                vm.sync()

                // Notify user about success
                $rootScope.hideWaitingDialog()
                ToastService.toast("Tasks imported successfully...")
            },
            // Error callback
            function (message) {
                // Notify user about error
                $rootScope.hideWaitingDialog()
                ToastService.toast("Upload Error : " + message)
            }
        )
    }

    vm.clearFilters = function () {
        // Broadcast Toggle Columns Event
        $scope.$broadcast(Constants.Events.TABLE_CLEAR_FILTERS)
    }

    vm.toggleColumns = function () {
        // Broadcast Toggle Columns Event
        $scope.$broadcast(Constants.Events.TABLE_TOGGLE_COLUMNS)
    }

    vm.sync = function () {
        // Broadcast Toggle Columns Event
        $scope.$broadcast(Constants.Events.TABLE_SYNC)
    }

    vm.edit = function () {
        DialogService.taskEditor(vm.table.getSelectedIds(), vm.sync)
    }

    vm.close = function () {
        //Launch confirm Dialog box
        DialogService.confirm("Are you sure you want to close these " + vm.table.selectedRows.length + " tasks ?",
            function () {
                //http call to close tasks
                $rootScope.showWaitingDialog("Closing Tasks...")
                $http({
                    method: 'POST',
                    url:    '/api/users/task/close',
                    headers: {
                        'X-Auth-Token': $localStorage.accessToken
                    },
                    data: {
                        ids : vm.table.getSelectedIds()
                    }
                })
                .then(
                    function (response) {
                        $rootScope.hideWaitingDialog()

                        //Re-sync Task data since Task has been closed
                        vm.sync()

                        // Show Toast
                        ToastService.toast("Tasks closed...")
                    },
                    function (error) {
                        $rootScope.hideWaitingDialog()
                        ToastService.toast("Failed to close tasks!!!")
                    })
            })
    }

    vm.remove = function () {
        //Launch confirm Dialog box
        DialogService.confirm("Are you sure you want to remove these " + vm.table.selectedRows.length + " tasks ?",
            function () {
                //http call to close tasks
                $rootScope.showWaitingDialog("Removing Tasks...")
                $http({
                    method: 'POST',
                    url:    '/api/users/task/remove',
                    headers: {
                        'X-Auth-Token': $localStorage.accessToken
                    },
                    data: {
                        ids : vm.table.getSelectedIds()
                    }
                })
                .then(
                    function (response) {
                        $rootScope.hideWaitingDialog()

                        //Re-sync Task data since Task has been Removed.
                        vm.sync()

                        // Show Toast
                        ToastService.toast("Tasks removed successfully...")
                    },
                    function (error) {
                        $rootScope.hideWaitingDialog()
                        ToastService.toast("Failed to remove tasks!!!")
                    })
            })
    }

    vm.stopRenewal = function () {
        //Launch confirm Dialog box
        DialogService.confirm("Are you sure you want to stop renewal for " + vm.table.selectedRows.length + " tasks ?",
            function () {
                //http call to stop task renewal
                $rootScope.showWaitingDialog("Stopping renewal period...")
                $http({
                    method: 'POST',
                    url:    '/api/users/task/stoprenew',
                    headers: {
                        'X-Auth-Token': $localStorage.accessToken
                    },
                    data: {
                        ids : vm.table.getSelectedIds()
                    }
                })
                .then(
                    function (response) {
                        $rootScope.hideWaitingDialog()

                        //Re-sync Task data since Task renewal has been updated.
                        vm.sync()

                        // Show Toast
                        ToastService.toast("renewal period stopped...")
                    },
                    function (error) {
                        $rootScope.hideWaitingDialog()
                        ToastService.toast("Failed to stop renewal!!!")
                    })
            })
    }

    /*-------------------------------- Local APIs --------------------------------*/
})
