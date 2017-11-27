/**
 * Created by Siddharth on 04-09-2017.
 */

app.controller("LeadManageCtrl", function ($scope, $rootScope, $http, $localStorage, $state, $window,  ExcelService, DialogService, ToastService) {
    var vm = this

    /* ------------------------------- Scope APIs -----------------------------------*/
    vm.add = function() {
        DialogService.leadEditor(null, init)
    }

    vm.showOnMaps = function (lead) {
        $window.open("https://www.google.com/maps/search/?api=1&query=" + lead.latitude + "," + lead.longitude, "_blank")
    }

    // Single List Item Selection Toggle
    vm.toggleSelection = function (lead) {
        var idx = vm.selection.indexOf(lead)

        // Check if lead is present in selection
        if (idx != -1) {
            // Remove from selection
            vm.selection.splice(idx, 1)
        } else {
            // Add in selection
            vm.selection.push(lead)
        }
    }

    // Full List Selection Toggling
    vm.toggleAll = function () {
        // Check if all are selected
        if (vm.selection.length == vm.leads.length) {
            // Remove All
            vm.selection.splice(0, vm.selection.length)
        } else {
            // Add All
            vm.leads.forEach(function (lead) {
                if (vm.selection.indexOf(lead) == -1) {
                    vm.selection.push(lead)
                }
            })
        }
    }

    // List Actions
    vm.remove = function() {
        // Launch Confirm Dialog
        DialogService.confirm(
            "Are you sure you want to remove these " + vm.selection.length + " leads ?",
            function () {
                $rootScope.showWaitingDialog("Please wait while we are removing leads...")
                // Make Http call to remove leads
                $http({
                    method: 'POST',
                    url: '/api/users/lead/remove',
                    headers: {
                        'X-Auth-Token': $localStorage.accessToken
                    },
                    data: {
                        leads: vm.selection
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
            })
    }
    
    vm.edit = function () {
        //Launch Leads-Editor dialog
        DialogService.leadEditor(vm.selection)
    }

    vm.createtasks = function () {
        var task = []
        vm.selection.forEach(function (lead)
        {
            task.push({
                lead: lead
            })
        })
        DialogService.taskCreator(task)
    }

    /* ------------------------------- Local APIs -----------------------------------*/
    // Send request to get list of leads
    function init() {
        // Re-initialize selection to empty
        vm.selection = []

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
                    vm.leads = response.data
                },
                function (error) {
                    $rootScope.hideWaitingDialog()
                    console.log(error)
                }
            )
    }

    /* ------------------------------- INIT -----------------------------------*/
    // Set menu and option
    $scope.nav.item       = MENU_ITEMS[MENU_ITEM_LEADS]
    $scope.nav.option     = ITEM_OPTIONS[ITEM_OPTION_MANAGE]

    // Init Variables
    vm.leads = []
    vm.selection = []

    init()
})
