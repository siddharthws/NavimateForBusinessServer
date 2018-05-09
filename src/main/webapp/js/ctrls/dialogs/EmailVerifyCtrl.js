/**
 * Created by Siddharth on 01-09-2017.
 */

app.controller('EmailVerifyCtrl', function ($scope, $rootScope, $mdDialog, AuthService, DialogService, ToastService, regInfo, otp) {

    /* ------------------------------- Scope APIs -----------------------------------*/
    // Button Click APIs
    $scope.emailVerify = function(){

        // Validate OTP
        if (validateOtp()) {
            //Register the account as OTP is validated
            $rootScope.showWaitingDialog("Please wait while you are being registered...")

            AuthService.register(regInfo)
                .then(
                    function () {
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
                        ToastService.toast(error)

                        // Re-open Register dialog
                        DialogService.register(regInfo)
                    }
                )
            }
    }

    $scope.cancel = function () {
        // Re-open Register dialog
        DialogService.register(regInfo)
    }

    /* ------------------------------- Local APIs -----------------------------------*/

    function validateOtp() {
        if (!$scope.otp) {
            // Raise error flag
            $scope.bShowError = true

            // Show error toast
            ToastService.toast("Please enter the OTP sent to Email ID provided")

            return false
        }

        //Check if OTP matched or not
        if (String($scope.otp) != otp) {
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
})