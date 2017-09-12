/**
 * Created by Siddharth on 22-08-2017.
 */

app.controller("TeamManageCtrl", function ($scope, $http, $localStorage, $state, DialogService) {

    // Set menu and option
    $scope.selection.item       = MENU_ITEMS[MENU_ITEM_TEAM]
    $scope.selection.option     = ITEM_OPTIONS[ITEM_OPTION_MANAGE]

    /* ------------------------------- INIT -----------------------------------*/
    $scope.init = function ()
    {
        $http({
            method:     'GET',
            url:        '/api/users/team',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            }
        })
            .then(
                function (response) {
                    $scope.team = response.data
                },
                function (error) {
                    console.log(error)
                    $state.go('home')
                }
            )
    }

    // Init View
    $scope.init()

    /* ------------------------------- APIs -----------------------------------*/
    $scope.add = function () {
        DialogService.addRep($scope.init)
    }
})
