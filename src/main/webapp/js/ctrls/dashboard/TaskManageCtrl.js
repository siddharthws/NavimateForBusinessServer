/**
 * Created by Siddharth on 22-08-2017.
 */

app.controller("TaskManageCtrl", function ($scope, $rootScope, $http, $localStorage, $state, DialogService, ToastService) {
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

    vm.close = function () {
        //Launch confirm Dialog box
        DialogService.confirm("Are you sure you want to close these " + vm.getSelectedItems().length + " tasks ?",
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
                        tasks : vm.getSelectedItems()
                    }
                })
                .then(
                    function (response) {
                        $rootScope.hideWaitingDialog()
                        // Show Toast
                        ToastService.toast("Tasks closed...")

                        // re-initialize tasks
                        initTasks()

                    },
                    function (error) {
                        $rootScope.hideWaitingDialog()
                        ToastService.toast("Failed to close tasks!!!")

                        // re-initialize tasks
                        initTasks()
                    })
            })
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
                        // Show Toast
                        ToastService.toast("Tasks removed successfully...")

                        // re-initialize tasks
                        initTasks()

                    },
                    function (error) {
                        $rootScope.hideWaitingDialog()
                        ToastService.toast("Failed to remove tasks!!!")

                        // re-initialize tasks
                        initTasks()
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
                        // Show Toast
                        ToastService.toast("renewal period stopped...")

                        // re-initialize tasks
                        initTasks()
                    },
                    function (error) {
                        $rootScope.hideWaitingDialog()
                        ToastService.toast("Failed to stop renewal!!!")

                        // re-initialize tasks
                        initTasks()
                    })
            })
    }

    /*-------------------------------- Local APIs --------------------------------*/
    function initTasks () {
        $rootScope.showWaitingDialog("Please wait while we are fetching tasks...")
        $http({
            method:     'GET',
            url:        '/api/users/task',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            }
        })
            .then(
                function (response) {
                    $rootScope.hideWaitingDialog()
                    vm.tasks = response.data

                    // Re-Init selection array with all unselected
                    vm.selection = []
                    vm.tasks.forEach(function () {
                        vm.selection.push(false)
                    })
                },
                function (error) {
                    $rootScope.hideWaitingDialog()

                    ToastService.toast("Unable to load tasks !!!")
                }
            )
    }

    /*-------------------------------- INIT --------------------------------*/
    // Set menu and option
    $scope.nav.item       = Constants.DashboardNav.Menu[Constants.DashboardNav.ITEM_TASKS]
    $scope.nav.option     = Constants.DashboardNav.Options[Constants.DashboardNav.OPTION_MANAGE]

    // Init Objects
    vm.tasks = []
    vm.selection = []
    vm.bCheckAll = false

    initTasks()
})
