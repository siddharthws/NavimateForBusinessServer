/**
 * Created by Siddharth on 22-08-2017.
 */

app.controller("TeamManageCtrl", function ($scope, $rootScope, $http, $localStorage, $state, DialogService, ToastService, TeamDataService) {
    var vm = this

    /* ------------------------------- APIs -----------------------------------*/
    vm.add = function () {
        DialogService.addRep(init)
    }

    vm.track = function() {
        DialogService.liveTracking(vm.getSelectedItems())
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
                selectedItems.push(vm.team[i])
            }
        }
        return selectedItems
    }

    // List Actions
    vm.remove = function() {
        // Launch Confirm Dialog
        DialogService.confirm(
            "Are you sure you want to remove these " + vm.getSelectedItems().length + " members from your team ?",
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
                        reps: JSON.stringify(vm.getSelectedItems())
                    }
                })
                    .then(
                        function (response) {
                            $rootScope.hideWaitingDialog()

                            //Re-sync team data since member has been removed
                            TeamDataService.sync()

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
        vm.selection.forEach(function (bSelected, i)
        {
            if (bSelected) {
                task.push({
                    repId: vm.team[i].id
                })
            }
        })
        DialogService.taskCreator(task)
    }

    /* ------------------------------- Local APIs -----------------------------------*/

     function init(){
        // Re-Init selection array with all unselected
        vm.selection = []

        // Get Team Data
        vm.team =  TeamDataService.cache.data

        if(vm.team) {
            vm.team.forEach(function () {
                vm.selection.push(false)
            })
        }
    }

    /* ------------------------------- INIT -----------------------------------*/
    // Set menu and option
    $scope.nav.item       = Constants.DashboardNav.Menu[Constants.DashboardNav.ITEM_TEAM]
    $scope.nav.option     = Constants.DashboardNav.Options[Constants.DashboardNav.OPTION_MANAGE]

    // Init Variables
    vm.team = []
    vm.selection = []
    vm.bCheckAll = false

    // Add event listeners
    // Listener for Team data ready event
    $scope.$on(Constants.Events.TEAM_DATA_READY, function (event, data) {
        init()
    })

    // Init View
    init()

})
