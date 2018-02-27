/**
 * Created by Siddharth on 22-08-2017.
 */

app.controller("TaskManageCtrl", function ($scope, $rootScope, $http, $localStorage, $state, DialogService, ToastService, TableService) {
    var vm = this

    // Set menu and option
    $scope.nav.item       = Constants.DashboardNav.Menu[Constants.DashboardNav.ITEM_TASKS]
    $scope.nav.option     = Constants.DashboardNav.Options[Constants.DashboardNav.OPTION_MANAGE]

    // Set task table as active
    TableService.activeTable = TableService.taskTable
    vm.table = TableService.activeTable

    /*-------------------------------- Scope APIs --------------------------------*/
    vm.add = function () {
        // Launch Task Creator dialog
        DialogService.taskCreator(null, vm.sync())
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

    vm.edit = function () {
        // Get selected tasks from selection
        var selectedTasks = getTaskFromSelection()

        DialogService.taskCreator(selectedTasks)
    }

    vm.close = function () {
        // Get selected tasks from selection
        var selectedTasks = getTaskFromSelection()

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
                        tasks : selectedTasks
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
        // Get selected tasks from selection
        var selectedTasks = getTaskFromSelection()

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
                        tasks : selectedTasks
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
        // Get selected tasks from selection
        var selectedTasks = getTaskFromSelection()

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
                        tasks : selectedTasks
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

    vm.showMap = function () {
        // Prepare location array for each task
        var locations = []
        vm.table.selectedRows.forEach(function (selectedId) {
            // Get task of this selectedId
            var task = $rootScope.getTaskById(selectedId)
            // Get lead of this task
            var lead = $rootScope.getLeadById(task.leadId)
            // Add lead title, lat and lng
            locations.push({
                title:      lead.title,
                latitude:   lead.latitude,
                longitude:  lead.longitude
            })
        })

        // Open Location Viewer dialog
        DialogService.locationViewer(locations)
    }

    /*-------------------------------- Local APIs --------------------------------*/

    function getTaskFromSelection() {
        var selectedTasks = []
        // get task object for the selected tasks
        vm.table.selectedRows.forEach(function (selectedId) {
            // Get task of this Id
            var task = $rootScope.getTaskById(selectedId)
            selectedTasks.push(task)
        })

        return selectedTasks
    }


})
