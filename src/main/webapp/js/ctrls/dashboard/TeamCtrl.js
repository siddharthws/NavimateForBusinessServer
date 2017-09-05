/**
 * Created by Siddharth on 04-09-2017.
 */

app.controller("TeamCtrl", function ($scope, $http, $localStorage, $state) {

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
                    console.log(response.data)
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
