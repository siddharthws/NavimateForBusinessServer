/**
 * Created by Siddharth on 04-09-2017.
 */

app.controller("LeadManageCtrl", function ($scope, $rootScope, $http, $localStorage, $state, $window,  ExcelService, DialogService, ToastService, LeadDataService) {
    var vm = this

    /* ------------------------------- Scope APIs -----------------------------------*/
    vm.add = function() {
        DialogService.leadEditor(null, init)
    }

    vm.showOnMaps = function (lead) {
        $window.open("https://www.google.com/maps/search/?api=1&query=" + lead.latitude + "," + lead.longitude, "_blank")
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
                selectedItems.push(vm.leads[i])
            }
        }
        return selectedItems
    }

    // List Actions
    vm.remove = function() {
        // Launch Confirm Dialog
        DialogService.confirm(
            "Are you sure you want to remove these " + vm.getSelectedItems().length + " leads ?",
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
                        leads: vm.getSelectedItems()
                    }
                }).then(
                    function (response) {
                        $rootScope.hideWaitingDialog()

                        //Re-sync Lead data since lead has been deleted
                        LeadDataService.sync()

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
        DialogService.leadEditor(vm.getSelectedItems())
    }

    vm.createtasks = function () {
        var task = []
        vm.selection.forEach(function (bSelected, i)
        {
            if (bSelected) {
                task.push({
                    lead: vm.leads[i]
                })
            }
        })
        DialogService.taskCreator(task)
    }

    /* ------------------------------- Local APIs -----------------------------------*/
    // Send request to get list of leads
    function init() {
        // Re-Init selection array with all unselected
        vm.selection = []
        // Get Lead Data
        vm.leads =  LeadDataService.cache.data
        if(vm.leads) {
            vm.leads.forEach(function () {
                vm.selection.push(false)
            })
        }
    }

    /* ------------------------------- INIT -----------------------------------*/
    // Set menu and option
    $scope.nav.item       = Constants.DashboardNav.Menu[Constants.DashboardNav.ITEM_LEADS]
    $scope.nav.option     = Constants.DashboardNav.Options[Constants.DashboardNav.OPTION_MANAGE]

    // Init Variables
    vm.leads = []
    vm.selection = []
    vm.bCheckAll = false

    // Add event listeners
    // Listener for Lead data ready event
    $scope.$on(Constants.Events.LEAD_DATA_READY, function (event, data) {
        init()
    })
    // Init View
    init()
})
