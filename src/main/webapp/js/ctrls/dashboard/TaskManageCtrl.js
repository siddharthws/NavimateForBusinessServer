/**
 * Created by Siddharth on 22-08-2017.
 */

app.controller("TaskManageCtrl", function ($scope, $http, $localStorage, $state, DialogService, ToastService) {

    /*-------------------------------- INIT --------------------------------*/
        // Set menu and option
    $scope.selection.item       = MENU_ITEMS[MENU_ITEM_TASKS]
    $scope.selection.option     = ITEM_OPTIONS[ITEM_OPTION_MANAGE]

    //Re-initialize selection to empty
    $scope.selection = []

    function initTasks () {
        $http({
            method:     'GET',
            url:        '/api/users/task',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            }
        })
        .then(
            function (response) {
                $scope.tasks = response.data
            },
            function (error) {
                console.log(error)
                $state.go('home')
            }
        )
    }

    initTasks()
    /*-------------------------------- APIs --------------------------------*/
    $scope.add = function () {
        // Launch Task Creator dialog
        DialogService.taskCreator(initTasks)
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
        $http({
            method: 'POST',
            url:    '/api/users/task/close',
            headers: {
                'X-Auth-Token': $localStorage.accessToken
            },
            data: {
                tasks : $scope.selection
            }
        }).then(
            function (response) {
                // Show Toast
                ToastService.toast("tasks closed...")

                // re-initialize tasks
                initTasks()
            },
            function (error) {
                ToastService.toast("Failed to close tasks!!!")

                // re-initialize tasks
                initTasks()
            })

    }
})
