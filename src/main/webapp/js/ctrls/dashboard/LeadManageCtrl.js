/**
 * Created by Siddharth on 04-09-2017.
 */

app.controller("LeadManageCtrl", function ( $scope, $rootScope, $http, $localStorage, $state,
                                            DialogService, ToastService, TableService) {
    /* ------------------------------- INIT -----------------------------------*/
    var vm = this

    // Set menu and option
    $scope.nav.item       = Constants.DashboardNav.Menu[Constants.DashboardNav.ITEM_LEADS]
    $scope.nav.option     = Constants.DashboardNav.Options[Constants.DashboardNav.OPTION_MANAGE]

    // Set lead table as active
    TableService.activeTable = TableService.leadTable
    vm.table = TableService.activeTable

    /* ------------------------------- Scope APIs -----------------------------------*/
    vm.add = function() {
        DialogService.leadEditor(null, vm.sync)
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
        // Get owned leads from selection
        var ownedLeads = getOwnedLeadsFromSelection()

        if (!ownedLeads.length) {
            ToastService.toast("You do not have permission to remove any of the selected leads.")
            return
        } else if (vm.table.selectedRows.length > ownedLeads.length) {
            ToastService.toast("You do not have permission to remove some of the selected leads. Removing from selection.")
        }

        // Launch Confirm Dialog
        DialogService.confirm(
            "Are you sure you want to remove " + ownedLeads.length + " leads ?",
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
                        leads: ownedLeads
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

    vm.edit = function () {
        // Get owned leads from selection
        var ownedLeads = getOwnedLeadsFromSelection()

        if (!ownedLeads.length) {
            ToastService.toast("You do not have permission to edit any of the selected leads.")
            return
        } else if (vm.table.selectedRows.length > ownedLeads.length) {
            ToastService.toast("You do not have permission to edit some of the selected leads. Removing from selection.")
        }

        //Launch Leads-Editor dialog
        DialogService.leadEditor(ownedLeads, vm.sync)
    }

    vm.createtasks = function () {
        // Create empty tasks for all seelcted leads
        var task = []
        vm.table.selectedRows.forEach(function (selectedId) {
            task.push({
                leadId: selectedId
            })
        })

        // Trigger task creation dialog
        DialogService.taskCreator(task)
    }

    /* ------------------------------- Local APIs -----------------------------------*/
    function getOwnedLeadsFromSelection() {
        var ownedLeads = []
        vm.table.selectedRows.forEach(function (selectedId) {
            var lead = $rootScope.getLeadById(selectedId)
            if ((lead.ownerId == $localStorage.id) ||
                ($localStorage.role == Constants.Role.ADMIN)) {
                ownedLeads.push(lead)
            }
        })

        return ownedLeads
    }
})
