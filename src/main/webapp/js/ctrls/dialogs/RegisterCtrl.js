/**
 * Created by Siddharth on 01-09-2017.
 */

app.controller('RegisterCtrl', function ($scope, $mdDialog, AuthService, DialogService, ToastService) {

    /* ------------------------------- Scope APIs -----------------------------------*/
    // Button Click APIs
    $scope.register = function(){

        // Validate credentials
        if (validate()) {
            AuthService.register($scope.name, $scope.phoneNumber, $scope.password)
                .then(
                    function (response) {
                        // Close this dialog
                        $mdDialog.hide()

                        // Open Login Dialog
                        DialogService.login()
                    },
                    function (error) {
                        // Show error toast
                        ToastService.toast("Unable to register...")
                    }
                )
        }
    }

    $scope.cancel = function () {
        $mdDialog.hide()
    }

    /* ------------------------------- Local APIs -----------------------------------*/
    function validate() {
        if (!$scope.phoneNumber || !$scope.password || !$scope.name) {
            // Raise error flag
            $scope.bShowError = true

            // Show error toast
            ToastService.toast("Please fill all fields !!!")

            return false
        }

        return true
    }

    /* ------------------------------- INIT -----------------------------------*/
    $scope.bShowError = false;

})