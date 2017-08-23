/**
 * Created by Siddharth on 23-08-2017.
 */

app.controller('LoginCtrl', function ($scope, $location, $localStorage, AuthService) {

    $scope.btnLogin = function(){
        AuthService.login($scope.phoneNumber, $scope.password)
        .then(
            function (response) {
                $localStorage.accessToken = response.data.accessToken;
                $location.path("/dashboard")
            },
            function (error) {
                console.log(error)
            }
        )
    }
})
