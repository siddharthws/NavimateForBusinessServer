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

    // Single List Item Selection Toggle
    vm.toggleSelection = function (task) {
        var idx = vm.selection.indexOf(task)

        // Check if task is present in selection
        if (idx != -1) {
            // Remove from selection
            vm.selection.splice(idx, 1)
        } else {
            // Add in selection
            vm.selection.push(task)
        }
    }

    // Full List Selection Toggling
    vm.toggleAll = function () {
        // Check if all are selected
        if (vm.selection.length == vm.tasks.length) {
            // Remove All
            vm.selection.splice(0, vm.selection.length)
        } else {
            // Add All
            vm.tasks.forEach(function (task) {
                if (vm.selection.indexOf(task) == -1) {
                    vm.selection.push(task)
                }
            })
        }
    }

    vm.close = function () {
        //Launch confirm Dialog box
        DialogService.confirm("Are you sure you want to close these " + vm.selection.length + " tasks ?",
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
                        tasks : vm.selection
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
        DialogService.confirm("Are you sure you want to remove these " + vm.selection.length + " tasks ?",
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
                        tasks : vm.selection
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
        DialogService.confirm("Are you sure you want to stop renewal for " + vm.selection.length + " tasks ?",
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
                        tasks : vm.selection
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
        //Re-initialize selection to empty
        vm.selection = []

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
                },
                function (error) {
                    $rootScope.hideWaitingDialog()
                    console.log(error)
                }
            )
    }

    /*-------------------------------- INIT --------------------------------*/
    // Set menu and option
    $scope.nav.item       = MENU_ITEMS[MENU_ITEM_TASKS]
    $scope.nav.option     = ITEM_OPTIONS[ITEM_OPTION_MANAGE]

    // Init Objects
    vm.tasks = []
    vm.selection = []

    initTasks()
})
