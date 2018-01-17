/**
 * Created by Siddharth on 15-12-2017.
 */

app.controller("LeadTemplatesCtrl", function ($scope, $rootScope, $http, $localStorage, $state, DialogService, ToastService, TemplateDataService) {
    var vm = this

    /*------------------------------- Scope APIs -------------------------------*/
    vm.edit = function () {
        var selectedItems = vm.getSelectedItems()
        if (selectedItems.length != 1) {
            ToastService.toast("Please select a single template to edit...")
        } else {
            DialogService.leadTemplateEditor(selectedItems[0])
        }
    }

    vm.create = function () {
        DialogService.leadTemplateEditor(null)
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
                selectedItems.push(vm.templates[i])
            }
        }
        return selectedItems
    }

    /*------------------------------- Local APIs -------------------------------*/

    function init() {
        // Get Template Data
        vm.templates = TemplateDataService.cache.data.leads

        // Re-Init selection array with all unselected
        vm.selection = []
        if(vm.templates) {
            vm.templates.forEach(function () {
                vm.selection.push(false)
            })
        }
    }

    /*------------------------------- INIT -------------------------------*/
    // Set menu and option
    $scope.nav.item       = Constants.DashboardNav.Menu[Constants.DashboardNav.ITEM_TEMPLATES]
    $scope.nav.option     = Constants.DashboardNav.Options[Constants.DashboardNav.OPTION_LEAD]

    // Init Object
    vm.selection = []
    vm.templates = []
    vm.bCheckAll = false

    // Add event listeners
    // Listener for Template data ready event
    $scope.$on(Constants.Events.LEAD_TEMPLATE_DATA_READY, function (event, data) {
        init()
    })

    // Run init sequence if data is already updated
    init()
})
