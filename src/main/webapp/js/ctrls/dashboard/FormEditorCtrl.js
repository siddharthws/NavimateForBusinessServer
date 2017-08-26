/**
 * Created by Siddharth on 22-08-2017.
 */

app.controller("FormEditorCtrl", function ($scope, $http, $location, $localStorage) {

    $scope.init = function ()
    {
        $http({
            method:     'GET',
            url:        '/api/users/form',
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
                    $location.path('/login')
                }
            )
    }

    // Init View
    $scope.init()
})
