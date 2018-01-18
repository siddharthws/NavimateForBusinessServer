/**
 * Created by Sandeep on 18-01-2018.
 */

app.controller("TaskCloseCtrl", function ($scope, $rootScope, $http, $localStorage, $state, DialogService, ToastService ,TaskDataService) {
    var vm = this

    /*-------------------------------- Scope APIs --------------------------------*/
    vm.add = function () {
        // Launch Task Creator dialog
        DialogService.taskCreator([{}], initTasks)
    }

    // Full List Selection Toggling
    vm.toggleAll = function () {
        for (var i = 0; i < vm.selection.length; i++) {
            vm.selection[i] = vm.bCheckAll
        }
    }

    // API to get selected items
    vm.getSelectedItems = function () {
        var selectedItems = []
        for (var i = 0; i < vm.selection.length; i++) {
            if (vm.selection[i]) {
                selectedItems.push(vm.tasks[i])
            }
        }
        return selectedItems
    }

    vm.remove = function () {
        //Launch confirm Dialog box
        DialogService.confirm("Are you sure you want to remove these " + vm.getSelectedItems().length + " tasks ?",
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
                        tasks : vm.getSelectedItems()
                    }
                })
                .then(
                    function (response) {
                        $rootScope.hideWaitingDialog()

                        //Re-sync Task data since Task renewal has been updated.
                        TaskDataService.sync()

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
        DialogService.confirm("Are you sure you want to stop renewal for " + vm.getSelectedItems().length + " tasks ?",
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
                        tasks : vm.getSelectedItems()
                    }
                })
                .then(
                    function (response) {
                        $rootScope.hideWaitingDialog()

                        //Re-sync Task data since Task renewal has been updated.
                        TaskDataService.sync()

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
    function initTasks () {
        // Get Task Data
        vm.tasks = TaskDataService.cache.data

        // Re-Init selection array with all unselected
        vm.selection = []

        if(vm.tasks){
            vm.tasks.forEach(function () {
                vm.selection.push(false)
            })
        }
    }

    /*-------------------------------- INIT --------------------------------*/
    // Set menu and option
    $scope.nav.item       = Constants.DashboardNav.Menu[Constants.DashboardNav.ITEM_TASKS]
    $scope.nav.option     = Constants.DashboardNav.Options[Constants.DashboardNav.OPTION_CLOSE]

    // Init Objects
    vm.tasks = []
    vm.selection = []
    vm.bCheckAll = false

    // Add event listeners
    // Listener for Task data ready event
    $scope.$on(Constants.Events.TASK_DATA_READY, function (event, data) {
        initTasks()
    })

    // Init View
    initTasks()
})
