/**
 * Created by Siddharth on 04-09-2017.
 */

app.controller("LeadManageCtrl",
                function ( $scope, $rootScope, $http, $localStorage, $state,
                            DialogService, ToastService, TableService, ImportService, NavService) {
    /* ------------------------------- INIT -----------------------------------*/
    var vm = this

    // Set menu and option
    NavService.activeMenu = NavService.leads
    NavService.activeMenu.activeTab = NavService.leads.tabs[0]

    // Set lead table as active
    TableService.activeTable = TableService.leadTable
    vm.table = TableService.activeTable

    /* ------------------------------- Scope APIs -----------------------------------*/
    vm.add = function() {
        DialogService.leadEditor(null, vm.sync)
    }

    vm.edit = function () {
        //Launch Leads-Editor dialog
        DialogService.leadEditor(vm.table.getSelectedIds(), vm.sync)
    }

    // API to import leads
    vm.import = function (file) {
        // Show waiting dialog
        $rootScope.showWaitingDialog("Importing leads. This may take some time...")

        // Perform import
        ImportService.import("/api/manager/leads/import", file).then(
            // Success callback
            function () {
                // Sync data again
                vm.sync()

                // Notify user about success
                $rootScope.hideWaitingDialog()
                ToastService.toast("Leads imported successfully...")
            },
            // Error callback
            function (message) {
                // Notify user about error
                $rootScope.hideWaitingDialog()
                ToastService.toast("Upload Error : " + message)
            }
        )
    }

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

    // APIs for actions in dropdown
    vm.remove = function() {
        // Launch Confirm Dialog
        DialogService.confirm(
            "Are you sure you want to remove " + vm.table.selectedRows.length + " leads ?",
            function () {
                $rootScope.showWaitingDialog("Please wait while we are removing leads...")
                // Make Http call to remove leads
                $http({
                    method: 'POST',
                    url: '/api/users/lead/remove',
                    headers: {
                        'X-Auth-Token': $localStorage.accessToken
                    },
                    data: {
                        ids: vm.table.getSelectedIds()
                    }
                }).then(
                    function (response) {
                        $rootScope.hideWaitingDialog()

                        // Sync data again
                        vm.sync()

                        // Show Toast
                        ToastService.toast("leads removed...")
                    },
                    function (error) {
                        $rootScope.hideWaitingDialog()
                        ToastService.toast("Failed to remove leads!!!")
                    })
            })
    }

    /* ------------------------------- Local APIs -----------------------------------*/
})
