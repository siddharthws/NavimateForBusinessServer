/**
 * Created by Siddharth on 22-08-2017.
 */

app.controller("FormManageCtrl", function ($scope, $http, $localStorage, $state) {

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
