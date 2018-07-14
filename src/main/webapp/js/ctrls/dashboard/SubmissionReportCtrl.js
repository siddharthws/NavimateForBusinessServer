/**
 * Created by Siddharth on 11-12-2017.
 */

app.controller("SubmissionReportCtrl", function (   $scope, $rootScope, $http, $localStorage,
                                                    ToastService, TableService, NavService, DialogService, FormService) {
    /* ------------------------------- INIT -----------------------------------*/
    var vm = this

    // Set Active Tab and Menu
    NavService.setActive(NavService.reports, 0)

    // Set form table as active
    TableService.activeTable = FormService.table
    vm.table = TableService.activeTable

    /*-------------------------------------- Scope APIs ---------------------------------------*/
    // APIs for table based actions
    vm.sync = function () {
        // Reset table Selection
        vm.table.selection = []

        // Broadcast Sync Table Event
        $scope.$broadcast(Constants.Events.TABLE_SYNC)
    }

    vm.export = function () {
        // Broadcast Export Table Event
        $scope.$broadcast(Constants.Events.TABLE_EXPORT)
    }

    vm.reset = function () {
        // Broadcast Clear Filter Event
        $scope.$broadcast(Constants.Events.TABLE_RESET)
    }

    vm.toggleColumns = function () {
        // Broadcast Toggle Columns Event
        $scope.$broadcast(Constants.Events.TABLE_TOGGLE_COLUMNS)
    }

    // APIs for actions in dropdown
    vm.remove = function() {
        // Launch Confirm Dialog
        DialogService.confirm(
            "Are you sure you want to remove " + vm.table.selection.length + " forms ?",
            function () {
                $rootScope.showWaitingDialog("Please wait while we are removing forms...")
                // Make Http call to remove leads
                $http({
                    method: 'POST',
                    url: '/api/manager/forms/remove',
                    headers: {
                        'X-Auth-Token': $localStorage.accessToken
                    },
                    data: {
                        ids: vm.table.getSelectedIds()
                    }
                }).then(
                    function (response) {
                        $rootScope.hideWaitingDialog()

                        // Reset table Selection
                        vm.table.selection = []

                        // Sync data again
                        vm.sync()

                        // Show Toast
                        ToastService.toast("forms removed...")
                    },
                    function (error) {
                        $rootScope.hideWaitingDialog()
                        ToastService.toast("Failed to remove forms!!!")
                    })
            })
    }
})
