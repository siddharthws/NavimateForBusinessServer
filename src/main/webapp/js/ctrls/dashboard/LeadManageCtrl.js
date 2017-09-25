/**
 * Created by Siddharth on 04-09-2017.
 */

app.controller("LeadManageCtrl", function ($scope, $http, $localStorage, $state, ExcelService, DialogService) {

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
})
