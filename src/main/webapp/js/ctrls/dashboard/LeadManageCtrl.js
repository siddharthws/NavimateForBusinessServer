/**
 * Created by Siddharth on 04-09-2017.
 */

app.controller("LeadManageCtrl", function ($scope, $http, $localStorage, $state, ExcelService, DialogService, ToastService) {

    /* ------------------------------- INIT -----------------------------------*/
    // Set menu and option
    $scope.selection.item       = MENU_ITEMS[MENU_ITEM_LEADS]
    $scope.selection.option     = ITEM_OPTIONS[ITEM_OPTION_MANAGE]

    // Init Variables
    $scope.leads = []

    // Send request to get list of leads
    function init() {
        // Re-initialize selection to empty
        $scope.selection = []

        //Get Leads Data
        $http({
            method:     'GET',
            url:        '/api/users/lead',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            }
        })
        .then(
            function (response) {
                $scope.leads = response.data
            },
            function (error) {
                console.log(error)
                $state.go('home')
            }
        )
    }

    init()
    /* ------------------------------- Scope APIs -----------------------------------*/
    $scope.add = function() {
        DialogService.leadEditor(null, init)
    }

    // Single List Item Selection Toggle
    $scope.toggleSelection = function (lead) {
        var idx = $scope.selection.indexOf(lead)

        // Check if rep is present in selection
        if (idx != -1) {
            // Remove from selection
            $scope.selection.splice(idx, 1)
        } else {
            // Add in selection
            $scope.selection.push(lead)
        }
    }

    // Full List Selection Toggling
    $scope.toggleAll = function () {
        // Check if all are selected
        if ($scope.selection.length == $scope.leads.length) {
            // Remove All
            $scope.selection.splice(0, $scope.selection.length)
        } else {
            // Add All
            $scope.leads.forEach(function (lead) {
                if ($scope.selection.indexOf(lead) == -1) {
                    $scope.selection.push(lead)
                }
            })
        }
    }

    // List Actions
    $scope.remove = function() {
        // Launch Confirm Dialog
        DialogService.confirm(
            "Are you sure you want to remove these " + $scope.selection.length + " leads ?",
            removeSelected)
    }

    // API to remove selected leads from list
    function removeSelected () {
        // Make Http call to remove leads
        $http({
            method: 'POST',
            url: '/api/users/lead/remove',
            headers: {
                'X-Auth-Token': $localStorage.accessToken
            },
            data: {
                leads: $scope.selection
            }
        }).then(
            function (response) {
                // Show Toast
                ToastService.toast("leads removed...")

                // re-initialize leads
                init()
            },
            function (error) {
                ToastService.toast("Failed to remove leads!!!")

                // re-initialize leads
                init()
            })
    }
    
    $scope.edit = function () {
        //Launch Leads-Editor dialog
        DialogService.leadEditor($scope.selection)
    }
})
