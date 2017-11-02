/**
 * Created by Siddharth on 01-09-2017.
 */

app.controller('RegisterCtrl', function ($scope, $rootScope, $mdDialog, AuthService, DialogService, ToastService, name, email, password) {

    /* ------------------------------- Scope APIs -----------------------------------*/
    // Button Click APIs
    $scope.register = function(){

        // Validate credentials
        if (validate()) {
            //Register the account as OTP is validated
            $rootScope.showWaitingDialog("Please wait while you are being registered...")

            AuthService.register($scope.name, $scope.email, $scope.password)
                .then(
                    function (response) {
                        $rootScope.hideWaitingDialog()

                        // Close this dialog
                        $mdDialog.hide()
                        ToastService.toast("Registered successfully...")

                        // Open Login Dialog
                        DialogService.login()
                    },
                    function (error) {
                        $rootScope.hideWaitingDialog()

                        // Show error toast
                        ToastService.toast("Unable to register...")

                        // Re-open Register dialog
                        //DialogService.register(name, email, password)
                    }
                )
        }
    }

    $scope.cancel = function () {
        $mdDialog.hide()
    }

    /* ------------------------------- Local APIs -----------------------------------*/
    function validate() {
        if (!$scope.email || !$scope.password || !$scope.name) {
            // Raise error flag
            $scope.bShowError = true

            // Show error toast
            ToastService.toast("Please fill all fields !!!")

            return false
        }

        // Validate Email
        if (($scope.email.indexOf('@') == -1) ||
            ($scope.email.indexOf('.') == -1)) {
            ToastService.toast("Invalid Email")
            return false
        }

        return true
    }

    /* ------------------------------- INIT -----------------------------------*/
    $scope.bShowError = false;
    $scope.name = name
    $scope.email = email
    $scope.password = password

})