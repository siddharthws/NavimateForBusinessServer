/**
 * Created by Siddharth on 01-09-2017.
 */

app.controller('LoginCtrl', function ($scope, $mdDialog, $state, $localStorage, AuthService) {

    $scope.login = function(){

        AuthService.login($scope.phoneNumber, $scope.password)
            .then(
                function (response) {
                    // Hide Dialog
                    $mdDialog.hide()

                    // Redirect to dashboard
                    $localStorage.accessToken = response.data.accessToken;
                    $state.go("dashboard.team.report")
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