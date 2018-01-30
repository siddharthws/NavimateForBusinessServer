/**
 * Created by Siddharth on 01-09-2017.
 */

app.controller('EmailVerifyCtrl', function ($scope, $rootScope, $mdDialog, AuthService, DialogService, ToastService, name, email, password, role, companyName) {

    /* ------------------------------- Scope APIs -----------------------------------*/
    // Button Click APIs
    $scope.emailVerify = function(){

        // Validate OTP
        if (validateOtp()) {
            //Register the account as OTP is validated
            $rootScope.showWaitingDialog("Please wait while you are being registered...")

            AuthService.register(name, email, password, role, companyName)
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
                        ToastService.toast(error.data.error)

                        // Re-open Register dialog
                        DialogService.register(name, email, password, role, companyName)
                    }
                )
            }
    }

        $scope.cancel = function () {
            // Re-open Register dialog
            DialogService.register(name, email, password, role, companyName)
    }

    /* ------------------------------- Local APIs -----------------------------------*/
    function generateOtp(){
        otp_gen= Math.floor(100000 + Math.random() * 900000)
        $rootScope.showWaitingDialog("Please wait while OTP is sent to mail ID")

        AuthService.emailOtp(otp_gen, email)
            .then(
                function (response) {
                    //Email Successfully sent
                    ToastService.toast("Email Sent Successfully.")

                    //Hide waiting dialog
                    $rootScope.hideWaitingDialog()
                },
                function (error) {
                    // Show error toast
                    ToastService.toast("Error While Sending OTP to Email.")

                    //Hide waiting dialog
                    $rootScope.hideWaitingDialog()

                    // Re-open Register dialog
                    DialogService.register(name, email, password)
                }
            )
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