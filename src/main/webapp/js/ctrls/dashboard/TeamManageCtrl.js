/**
 * Created by Siddharth on 22-08-2017.
 */

app.controller("TeamManageCtrl", function ($scope, $rootScope, $http, $localStorage, $state, DialogService, ToastService) {
    var vm = this

    /* ------------------------------- APIs -----------------------------------*/
    vm.add = function () {
        DialogService.addRep(init)
    }

    vm.track = function() {
        DialogService.liveTracking(vm.selection)
    }

    // Single List Item Selection Toggle
    vm.toggleSelection = function (rep) {
        var idx = vm.selection.indexOf(rep)

        // Check if rep is present in selection
        if (idx != -1) {
            // Remove from selection
            vm.selection.splice(idx, 1)
        } else {
            // Add in selection
            vm.selection.push(rep)
        }
    }

    // Full List Selection Toggling
    vm.toggleAll = function () {
        // Check if all are selected
        if (vm.selection.length == vm.team.length) {
            // Remove All
            vm.selection.splice(0, vm.selection.length)
        } else {
            // Add All
            vm.team.forEach(function (rep) {
                if (vm.selection.indexOf(rep) == -1) {
                    vm.selection.push(rep)
                }
            })
        }
    }

    // List Actions
    vm.remove = function() {
        // Launch Confirm Dialog
        DialogService.confirm(
            "Are you sure you want to remove these " + vm.selection.length + " members from your team ?",
            function () {
                $rootScope.showWaitingDialog("Please wait while we are removing members...")
                // Make Http call to remove members
                $http({
                    method:     'POST',
                    url:        '/api/users/team/remove',
                    headers:    {
                        'X-Auth-Token':    $localStorage.accessToken
                    },
                    data: {
                        reps: JSON.stringify(vm.selection)
                    }
                })
                    .then(
                        function (response) {
                            $rootScope.hideWaitingDialog()
                            // Show Toast
                            ToastService.toast("Reps removed...")

                            // re-initialize team
                            init()
                        },
                        function (error) {
                            $rootScope.hideWaitingDialog()
                            ToastService.toast("Failed to remove reps!!!")

                            // re-initialize team
                            init()
                        }
                    )
            })
    }

    vm.createtasks = function () {
        var task = []
        vm.selection.forEach(function (rep)
        {
            task.push({
                rep: rep
            })
        })
        DialogService.taskCreator(task)
    }

    /* ------------------------------- Local APIs -----------------------------------*/
    function init() {
        // Re-initialize selection to empty
        vm.selection = []

        $rootScope.showWaitingDialog("Please wait while we are fetching team details...")
        // Get Team Data
        $http({
            method:     'GET',
            url:        '/api/users/team',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            }
        })
            .then(
                function (response) {
                    $rootScope.hideWaitingDialog()
                    vm.team = response.data
                },
                function (error) {
                    $rootScope.hideWaitingDialog()
                    console.log(error)
                }
            )
    }

    /* ------------------------------- INIT -----------------------------------*/
    // Set menu and option
    $scope.nav.item       = MENU_ITEMS[MENU_ITEM_TEAM]
    $scope.nav.option     = ITEM_OPTIONS[ITEM_OPTION_MANAGE]

    // Init Variables
    vm.team = []
    vm.selection = []

    // Init View
    init()
})
