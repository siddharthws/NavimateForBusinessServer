/**
 * Created by Siddharth on 15-12-2017.
 */

app.controller("LeadTemplatesCtrl", function ($scope, $rootScope, $http, $localStorage, $state, DialogService, ToastService) {
    var vm = this

    /*------------------------------- Scope APIs -------------------------------*/
    vm.edit = function () {
        var selectedItems = vm.getSelectedItems()
        if (selectedItems.length != 1) {
            ToastService.toast("Please select a single form to edit...")
        } else {
            DialogService.leadTemplateEditor(selectedItems[0], init)
        }
    }

    vm.create = function () {
        DialogService.leadTemplateEditor(null, init)
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
        $rootScope.showWaitingDialog("Please wait while we are fetching forms...")
        $http({
            method:     'GET',
            url:        '/api/users/template',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken,
                'templateType':     Constants.Template.TYPE_LEAD
            }
        }).then(
            function (response) {
                $rootScope.hideWaitingDialog()
                vm.templates = response.data.templates

                // Re-Init selection array with all unselected
                vm.selection = []
                vm.templates.forEach(function () {
                    vm.selection.push(false)
                })
            },
            function (error) {
                $rootScope.hideWaitingDialog()

                ToastService.toast("Unable to load forms templates !!!")
            }
        )
    }

    /*------------------------------- INIT -------------------------------*/
    // Set menu and option
    $scope.nav.item       = Constants.DashboardNav.Menu[Constants.DashboardNav.ITEM_TEMPLATES]
    $scope.nav.option     = Constants.DashboardNav.Options[Constants.DashboardNav.OPTION_LEAD]

    // Init Object
    vm.selection = []
    vm.templates = []
    vm.bCheckAll = false

    // Get Forms for this user
    init()
})