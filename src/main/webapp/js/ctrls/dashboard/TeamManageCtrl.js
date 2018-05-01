/**
 * Created by Siddharth on 22-08-2017.
 */

app.controller("TeamManageCtrl",
                function (  $scope, $rootScope, $http, $localStorage, $state,
                            DialogService, ToastService, TeamService, TableService, NavService, ImportService) {
    /* ------------------------------- INIT -----------------------------------*/
    var vm = this

    // Set Active Tab and Menu
    NavService.setActive(NavService.team, 0)

    // Set team table as active
    TableService.activeTable = TableService.teamTable
    vm.table = TableService.activeTable

    /* ------------------------------- APIs -----------------------------------*/

    // APIs for table based actions
    vm.export = function () {
        // Broadcast Toggle Columns Event
        $scope.$broadcast(Constants.Events.TABLE_EXPORT)
    }

    vm.import = function (file) {
        // Show waiting dialog
        $rootScope.showWaitingDialog("Importing team. This may take some time...")

        // Perform import
        ImportService.import("/api/admin/team/import", file).then(
            // Success callback
            function () {
                // Sync data again
                vm.sync()

                // Notify user about success
                $rootScope.hideWaitingDialog()
                ToastService.toast("Team imported successfully...")
            },
            // Error callback
            function (message) {
                // Notify user about error
                $rootScope.hideWaitingDialog()
                DialogService.alert("Upload Error : " + message)
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

    vm.add = function () {
        DialogService.teamEditor(null, vm.sync)
    }

    vm.edit = function () {
        DialogService.teamEditor(vm.table.getSelectedIds(), vm.sync)
    }

    vm.track = function() {
        // Get all reps by IDs
        DialogService.liveTracking(vm.table.selectedRows)
    }

    // List Actions
    vm.remove = function() {
        // Launch Confirm Dialog
        DialogService.confirm(
            "Are you sure you want to remove these " + vm.table.selectedRows.length + " members from your team ?",
            function () {
                $rootScope.showWaitingDialog("Please wait while we are removing members...")
                // Make Http call to remove members
                $http({
                    method:     'POST',
                    url:        '/api/admin/team/remove',
                    headers:    {
                        'X-Auth-Token':    $localStorage.accessToken
                    },
                    data: {
                        ids: vm.table.getSelectedIds()
                    }
                })
                    .then(
                        function (response) {
                            $rootScope.hideWaitingDialog()

                            //Re-sync team data since member has been removed
                            vm.sync()

                            // Show Toast
                            ToastService.toast("Reps removed...")
                        },
                        function (error) {
                            $rootScope.hideWaitingDialog()
                            ToastService.toast("Failed to remove reps!!!")
                        }
                    )
            })
    }

    /* ------------------------------- Local APIs -----------------------------------*/
})
