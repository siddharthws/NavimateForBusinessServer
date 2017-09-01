/**
 * Created by Siddharth on 01-09-2017.
 */

app.controller('LoginCtrl', function ($scope, $mdDialog, $location, $localStorage, AuthService) {

    $scope.login = function(){

        AuthService.login($scope.phoneNumber, $scope.password)
            .then(
                function (response) {
                    // Hide Dialog
                    $mdDialog.hide()

                    // Redirect to dashboard
                    $localStorage.accessToken = response.data.accessToken;
                    $location.path("/dashboard")
                },
                function (error) {
                    // Login Failure
                    console.log(error)
                }
            )
    }

    $scope.cancel = function () {
        $mdDialog.hide()
    }
})