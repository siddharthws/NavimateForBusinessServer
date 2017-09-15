/**
 * Created by Siddharth on 22-08-2017.
 */

app.controller("TeamManageCtrl", function ($scope, $http, $localStorage, $state, DialogService, ToastService) {

    /* ------------------------------- INIT -----------------------------------*/
    // Set menu and option
    $scope.selection.item       = MENU_ITEMS[MENU_ITEM_TEAM]
    $scope.selection.option     = ITEM_OPTIONS[ITEM_OPTION_MANAGE]

    $scope.init = function ()
    {
        // Re-initialize selection to empty
        $scope.selection = []

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
                $scope.team = response.data
            },
            function (error) {
                console.log(error)
                $state.go('home')
            }
        )
    }

    // Init View
    $scope.init()

    /* ------------------------------- APIs -----------------------------------*/
    $scope.add = function () {
        DialogService.addRep($scope.init)
    }

    // Single List Item Selection Toggle
    $scope.toggleSelection = function (rep) {
        var idx = $scope.selection.indexOf(rep)

        // Check if rep is present in selection
        if (idx != -1) {
            // Remove from selection
            $scope.selection.splice(idx, 1)
        } else {
            // Add in selection
            $scope.selection.push(rep)
        }
    }

    // Full List Selection Toggling
    $scope.toggleAll = function () {
        // Check if all are selected
        if ($scope.selection.length == $scope.team.length) {
            // Remove All
            $scope.selection.splice(0, $scope.selection.length)
        } else {
            // Add All
            $scope.team.forEach(function (rep) {
                if ($scope.selection.indexOf(rep) == -1) {
                    $scope.selection.push(rep)
                }
            })
        }
    }

    // List Actions
    $scope.remove = function() {
        // Launch Confirm Dialog
        DialogService.confirm(
            "Are you sure you want to remove these " + $scope.selection.length + " members from your team ?",
            removeSelected)
    }

    // API to remove selected team members from list
    function removeSelected () {
        // Make Http call to remove members
        $http({
            method:     'POST',
            url:        '/api/users/team/remove',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            },
            data: {
                reps: JSON.stringify($scope.selection)
            }
        })
        .then(
            function (response) {
                // Show Toast
                ToastService.toast("Reps removed...")

                // re-initialize team
                $scope.init()
            },
            function (error) {
                ToastService.toast("Failed to remove reps!!!")

                // re-initialize team
                $scope.init()
            }
        )
    }
})
