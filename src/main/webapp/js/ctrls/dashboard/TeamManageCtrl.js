/**
 * Created by Siddharth on 22-08-2017.
 */

app.controller("TeamManageCtrl", function ( $scope, $rootScope, $http, $localStorage, $state,
                                            DialogService, ToastService, TeamService, TableService) {
    /* ------------------------------- INIT -----------------------------------*/
    var vm = this

    // Set menu and option
    $scope.nav.item       = Constants.DashboardNav.Menu[Constants.DashboardNav.ITEM_TEAM]
    $scope.nav.option     = Constants.DashboardNav.Options[Constants.DashboardNav.OPTION_MANAGE]

    // Set team table as active
    TableService.activeTable = TableService.teamTable
    vm.table = TableService.activeTable

    /* ------------------------------- APIs -----------------------------------*/

    // APIs for table based actions
    vm.export = function () {
        // Broadcast Toggle Columns Event
        $scope.$broadcast(Constants.Events.TABLE_EXPORT)
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
        DialogService.teamEditor(vm.table.selectedRows, vm.sync)
    }

    vm.track = function() {
        // Get all reps by IDs
        $rootScope.showWaitingDialog("Starting Live Tracking...")
        TeamService.sync(vm.table.selectedRows).then(
            // Success
            function () {
                $rootScope.hideWaitingDialog()
                DialogService.liveTracking(TeamService.cache)
            },
            // Error
            function () {
                $rootScope.hideWaitingDialog()
                ToastService.toast("Unable to get reps")
            }
        )
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
                    url:        '/api/manager/team/remove',
                    headers:    {
                        'X-Auth-Token':    $localStorage.accessToken
                    },
                    data: {
                        ids: vm.table.selectedRows
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

    vm.isAdmin = function () {
        return $localStorage.role == Constants.Role.ADMIN
    }

    /* ------------------------------- Local APIs -----------------------------------*/
})
