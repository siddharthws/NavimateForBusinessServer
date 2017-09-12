/**
 * Created by Siddharth on 22-08-2017.
 */

app.controller("FormManageCtrl", function ($scope, $http, $localStorage, $state) {

    // Set menu and option
    $scope.selection.item       = MENU_ITEMS[MENU_ITEM_FORMS]
    $scope.selection.option     = ITEM_OPTIONS[ITEM_OPTION_MANAGE]

    // Get Forms for this user
    $http({
        method:     'GET',
        url:        '/api/users/form',
        headers:    {
            'X-Auth-Token':    $localStorage.accessToken
        }
    })
    .then(
        function (response) {
            $scope.forms = response.data
        },
        function (error) {
            console.log(error)
            $state.go('home')
        }
    )
})
