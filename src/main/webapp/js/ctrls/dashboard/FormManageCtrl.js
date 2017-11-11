/**
 * Created by Siddharth on 22-08-2017.
 */

app.controller("FormManageCtrl", function ($scope, $rootScope, $http, $localStorage, $state, DialogService) {

    /*------------------------------- INIT -------------------------------*/
    // Set menu and option
    $scope.nav.item       = MENU_ITEMS[MENU_ITEM_FORMS]
    $scope.nav.option     = ITEM_OPTIONS[ITEM_OPTION_MANAGE]

    // Get Forms for this user
    getForms()

    /*------------------------------- Scope APIs -------------------------------*/
    $scope.edit = function (form) {
        DialogService.formEditor(form, getForms)
    }

    $scope.create = function () {
        DialogService.formEditor(null, getForms)
    }

    /*------------------------------- Other APIs -------------------------------*/

    function getForms() {
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
