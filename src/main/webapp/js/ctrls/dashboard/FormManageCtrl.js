/**
 * Created by Siddharth on 22-08-2017.
 */

app.controller("FormManageCtrl", function ($scope, $rootScope, $http, $localStorage, $state, DialogService, ToastService) {

    /*------------------------------- INIT -------------------------------*/
    // Set menu and option
    $scope.nav.item       = MENU_ITEMS[MENU_ITEM_FORMS]
    $scope.nav.option     = ITEM_OPTIONS[ITEM_OPTION_MANAGE]

    // Init Object
    $scope.selection = []

    // Get Forms for this user
    init()

    /*------------------------------- Scope APIs -------------------------------*/
    $scope.edit = function () {
        if ($scope.selection.length != 1) {
            ToastService.toast("Please select a single form to edit...")
        } else {
            DialogService.formEditor($scope.selection[0], init)
        }
    }

    $scope.create = function () {
        DialogService.formEditor(null, init)
    }

    // Single List Item Selection Toggle
    $scope.toggleSelection = function (form) {
        var idx = $scope.selection.indexOf(form)

        // Check if lead is present in selection
        if (idx != -1) {
            // Remove from selection
            $scope.selection.splice(idx, 1)
        } else {
            // Add in selection
            $scope.selection.push(form)
        }
    }

    // Full List Selection Toggling
    $scope.toggleAll = function () {
        // Check if all are selected
        if ($scope.selection.length == $scope.forms.length) {
            // Remove All
            $scope.selection.splice(0, $scope.selection.length)
        } else {
            // Add All
            $scope.forms.forEach(function (form) {
                if ($scope.selection.indexOf(form) == -1) {
                    $scope.selection.push(form)
                }
            })
        }
    }

    /*------------------------------- Other APIs -------------------------------*/

    function init() {
        // Re-initialize selection array
        $scope.selection = []

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
                $scope.forms = response.data
            },
            function (error) {
                $rootScope.hideWaitingDialog()
                console.log(error)
            }
        )
    }
})
