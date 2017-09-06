/**
 * Created by Siddharth on 22-08-2017.
 */

app.controller("TeamManageCtrl", function ($scope, $http, $localStorage, $state) {

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
})
