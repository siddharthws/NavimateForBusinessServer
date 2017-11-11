/**
 * Created by Siddharth on 22-08-2017.
 */

app.controller("TaskManageCtrl", function ($scope, $rootScope, $http, $localStorage, $state, DialogService, ToastService) {

    /*-------------------------------- INIT --------------------------------*/
        // Set menu and option
    $scope.nav.item       = MENU_ITEMS[MENU_ITEM_TASKS]
    $scope.nav.option     = ITEM_OPTIONS[ITEM_OPTION_MANAGE]

    function initTasks () {
        //Re-initialize selection to empty
        $scope.selection = []

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
                $scope.tasks = response.data
            },
            function (error) {
                $rootScope.hideWaitingDialog()
                console.log(error)
            }
        )
    }

    initTasks()
    /*-------------------------------- APIs --------------------------------*/
    $scope.add = function () {
        // Launch Task Creator dialog
        DialogService.taskCreator([{}], initTasks)
    }

    // Single List Item Selection Toggle
    $scope.toggleSelection = function (task) {
        var idx = $scope.selection.indexOf(task)

        // Check if task is present in selection
        if (idx != -1) {
            // Remove from selection
            $scope.selection.splice(idx, 1)
        } else {
            // Add in selection
            $scope.selection.push(task)
        }
    }

    // Full List Selection Toggling
    $scope.toggleAll = function () {
        // Check if all are selected
        if ($scope.selection.length == $scope.tasks.length) {
            // Remove All
            $scope.selection.splice(0, $scope.selection.length)
        } else {
            // Add All
            $scope.tasks.forEach(function (task) {
                if ($scope.selection.indexOf(task) == -1) {
                    $scope.selection.push(task)
                }
            })
        }
    }

    $scope.close = function () {
        //Launch confirm Dialog box
        DialogService.confirm("Are you sure you want to close these " + $scope.selection.length + " tasks ?",
            closeSelected)
    }

    function closeSelected() {
        //http call to close tasks
        $rootScope.showWaitingDialog("Closing Tasks...")
        $http({
            method: 'POST',
            url:    '/api/users/task/close',
            headers: {
                'X-Auth-Token': $localStorage.accessToken
            },
            data: {
                tasks : $scope.selection
            }
        })
        .then(
            function (response) {
                $rootScope.hideWaitingDialog()
                // Show Toast
                ToastService.toast("tasks closed...")

                // re-initialize tasks
                initTasks()

            },
            function (error) {
                $rootScope.hideWaitingDialog()
                ToastService.toast("Failed to close tasks!!!")

                // re-initialize tasks
                initTasks()
            })

    }
    
    $scope.stopRenewal = function () {
        //Launch confirm Dialog box
        DialogService.confirm("Are you sure you want to stop renewal for " + $scope.selection.length + " tasks ?",
            stopRenewalCb)
    }

    function stopRenewalCb() {
        //http call to stop task renewal
        $rootScope.showWaitingDialog("Stopping renewal period...")
        $http({
            method: 'POST',
            url:    '/api/users/task/stoprenew',
            headers: {
                'X-Auth-Token': $localStorage.accessToken
            },
            data: {
                tasks : $scope.selection
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
    }
})
