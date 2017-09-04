/**
 * Created by Siddharth on 04-09-2017.
 */

app.controller("TeamCtrl", function ($scope, $http, $localStorage, $location) {

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
                    console.log("Content View Info received")
                },
                function (error) {
                    console.log(error)
                    $location.path('')
                }
            )
    }

    // Init View
    $scope.init()
})
