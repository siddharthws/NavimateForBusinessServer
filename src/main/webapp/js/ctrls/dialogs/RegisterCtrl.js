/**
 * Created by Siddharth on 01-09-2017.
 */

app.controller('RegisterCtrl', function ($scope, $mdDialog, AuthService, DialogService) {

    $scope.register = function(){
        AuthService.register($scope.name, $scope.phoneNumber, $scope.password)
            .then(
                function (response) {
                    // Close this dialog
                    $mdDialog.hide()

                    // Open Login Dialog
                    DialogService.login()
                },
                function (error) {
                    console.log(error)
                }
            )
    }

    $scope.cancel = function () {
        $mdDialog.hide()
    }
})