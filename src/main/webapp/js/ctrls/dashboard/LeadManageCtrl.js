/**
 * Created by Siddharth on 04-09-2017.
 */

app.controller("LeadManageCtrl", function ($scope, $rootScope, $http, $localStorage, $state, $window,  ExcelService, DialogService, ToastService) {

    /* ------------------------------- INIT -----------------------------------*/
    // Set menu and option
    $scope.nav.item       = MENU_ITEMS[MENU_ITEM_LEADS]
    $scope.nav.option     = ITEM_OPTIONS[ITEM_OPTION_MANAGE]

    // Init Variables
    $scope.leads = []

    // Send request to get list of leads
    function init() {
        // Re-initialize selection to empty
        $scope.selection = []

        $rootScope.showWaitingDialog("Please wait while we are fetching leads data...")
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
                $rootScope.hideWaitingDialog()
                $scope.leads = response.data
            },
            function (error) {
                $rootScope.hideWaitingDialog()
                console.log(error)
            }
        )
    }

    init()
    /* ------------------------------- Scope APIs -----------------------------------*/
    $scope.add = function() {
        DialogService.leadEditor(null, init)
    }

    $scope.showOnMaps = function (lead) {
        $window.open("https://www.google.com/maps/search/?api=1&query=" + lead.latitude + "," + lead.longitude, "_blank")
    }

    // Single List Item Selection Toggle
    $scope.toggleSelection = function (lead) {
        var idx = $scope.selection.indexOf(lead)

        // Check if lead is present in selection
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
        $rootScope.showWaitingDialog("Please wait while we are removing leads...")
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
                $rootScope.hideWaitingDialog()
                // Show Toast
                ToastService.toast("leads removed...")

                // re-initialize leads
                init()
            },
            function (error) {
                $rootScope.hideWaitingDialog()
                ToastService.toast("Failed to remove leads!!!")

                // re-initialize leads
                init()
            })
    }
    
    $scope.edit = function () {
        //Launch Leads-Editor dialog
        DialogService.leadEditor($scope.selection)
    }

    $scope.createtasks = function () {
        var task = []
        $scope.selection.forEach(function (lead)
        {
            task.push({
                lead: lead
            })
        })
        DialogService.taskCreator(task)
    }
})
