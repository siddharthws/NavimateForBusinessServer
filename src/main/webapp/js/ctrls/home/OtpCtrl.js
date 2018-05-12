/**
 * Created by Siddharth on 11-05-2018.
 *//**
 * Created by Siddharth on 11-05-2018.
 */
app.controller('OtpCtrl', function ( $scope, $state, $localStorage, $cookies,
                                     AuthService, ToastService, DialogService) {
    /*------------------------------------ INIT --------------------------------*/
    var vm = this

    // Error Flag
    vm.bInputError = false

    // OTP to be entered by the user
    vm.otp            = ""

    /*------------------------------------ Public Methods --------------------------------*/
    // Method to login
    vm.register = function () {
        // Validate OTP
        if (validate()) {
            // Set waiting flag
            vm.bWaiting = true

            AuthService.register($scope.regInfo).then(
                function () {
                    // Reset Waiting Flag
                    vm.bWaiting = false

                    // Show success toast
                    ToastService.toast("Registered successfully...")

                    // Redirect to login
                    $state.go("home.login")
                },
                function (error) {
                    // Reset Waiting Flag
                    vm.bWaiting = false

                    // Show error dialog
                    DialogService.alert("Registration error : " + error)

                    // Redirect to login
                    $state.go("home.register")
                }
            )
        }
    }

    // methods to report input errors
    vm.otpErr = function () {
        if (!vm.otp) {
            return "Enter OTP"
        }

        return null
    }

    /*------------------------------------ Private Methods --------------------------------*/
    // Method to validate input
    function validate() {
        // Reset error flag
        vm.bInputError = false

        // Validate Entered OTP
        if (vm.otpErr()) {
            vm.bInputError = true
            return false
        }

        // Compare entered OTP
        if (String(vm.otp) != $scope.regInfo.otp) {
            // Show error dialog
            DialogService.alert("Invalid OTP Entered")
            return false
        }

        return true
    }

    /*------------------------------------ Post Init --------------------------------*/
    // Redirect to registration if OTP is not set
    if (!$scope.regInfo || !$scope.regInfo.otp) {
        $state.go("home.register")
    }
})
