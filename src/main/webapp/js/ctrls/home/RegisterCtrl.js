/**
 * Created by Siddharth on 23-08-2017.
 */

app.controller('RegisterCtrl', function ($scope, $location, $localStorage, AuthService) {

    $scope.btnRegister = function(){
        AuthService.register($scope.name, $scope.phoneNumber, $scope.password)
            .then(
                function (response) {
                    $location.path("/login")
                },
                function (error) {
                    console.log(error)
                }
            )
    }
})
