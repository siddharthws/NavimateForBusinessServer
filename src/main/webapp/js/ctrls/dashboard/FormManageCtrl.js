/**
 * Created by Siddharth on 22-08-2017.
 */

app.controller("FormManageCtrl", function ($scope, $rootScope, $http, $localStorage, $state, DialogService, ToastService) {
    var vm = this

    /*------------------------------- Scope APIs -------------------------------*/
    vm.edit = function () {
        if (vm.selection.length != 1) {
            ToastService.toast("Please select a single form to edit...")
        } else {
            DialogService.formEditor(vm.selection[0], init)
        }
    }

    vm.create = function () {
        DialogService.formEditor(null, init)
    }

    // Single List Item Selection Toggle
    vm.toggleSelection = function (form) {
        var idx = vm.selection.indexOf(form)

        // Check if lead is present in selection
        if (idx != -1) {
            // Remove from selection
            vm.selection.splice(idx, 1)
        } else {
            // Add in selection
            vm.selection.push(form)
        }
    }

    // Full List Selection Toggling
    vm.toggleAll = function () {
        // Check if all are selected
        if (vm.selection.length == vm.forms.length) {
            // Remove All
            vm.selection.splice(0, vm.selection.length)
        } else {
            // Add All
            vm.forms.forEach(function (form) {
                if (vm.selection.indexOf(form) == -1) {
                    vm.selection.push(form)
                }
            })
        }
    }

    /*------------------------------- Local APIs -------------------------------*/

    function init() {
        // Re-initialize selection array
        vm.selection = []

        $rootScope.showWaitingDialog("Please wait while we are fetching forms...")
        $http({
            method:     'GET',
            url:        '/api/users/form',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            }
        })
        .then(
            function (response) {
                $rootScope.hideWaitingDialog()
                vm.forms = response.data
            },
            function (error) {
                $rootScope.hideWaitingDialog()
                console.log(error)
            }
        )
    }

    /*------------------------------- INIT -------------------------------*/
    // Set menu and option
    $scope.nav.item       = MENU_ITEMS[MENU_ITEM_FORMS]
    $scope.nav.option     = ITEM_OPTIONS[ITEM_OPTION_MANAGE]

    // Init Object
    vm.selection = []
    vm.forms = []

    // Get Forms for this user
    init()
})
