/**
 * Created by Siddharth on 01-09-2017.
 */

app.controller('EmailVerifyCtrl', function ($scope, $rootScope, $mdDialog, AuthService, DialogService, ToastService) {

    /* ------------------------------- Scope APIs -----------------------------------*/
    // Button Click APIs
    $scope.emailVerify = function(){

        // Validate OTP
        if (validateOtp()) {
            ToastService.toast("OTP Received...Checking")
            /*
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
                    }
                )*/

        }
    }

        $scope.cancel = function () {
        $mdDialog.hide()
    }

    /* ------------------------------- Local APIs -----------------------------------*/
    function generateOtp(){
        otp_gen= Math.floor(100000 + Math.random() * 900000)
    }

    function validateOtp() {
        if (!$scope.otp) {
            // Raise error flag
            $scope.bShowError = true

            // Show error toast
            ToastService.toast("Please enter the OTP sent to Email ID provided")

            return false
        }

        //Check if OTP matched or not
        if ($scope.otp!=otp_gen) {
            // Raise error flag
            $scope.bShowError = true

            // Show error toast
            ToastService.toast("Invalid OTP Entered")

            return false
        }

        return true
    }

    /* ------------------------------- INIT -----------------------------------*/
    $scope.bShowError = false;
    var otp_gen
    generateOtp()
})