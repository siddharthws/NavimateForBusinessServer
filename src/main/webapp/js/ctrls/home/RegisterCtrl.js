/**
 * Created by Siddharth on 01-09-2017.
 */

app.controller('RegisterCtrl', function (   $scope, $state,
                                            AuthService, DialogService, ToastService) {
    /*------------------------------------ Init --------------------------------*/
    var vm = this

    // Error Flag
    vm.bInputError = false

    /*------------------------------------ Public Methods --------------------------------*/
    // Method to validate registration
    vm.validateRegistration = function () {
        if (validate()) {
            // Set Waiting Flag
            vm.bWaiting = true

            // Trigger registration validation request
            AuthService.validateRegistration($scope.regInfo).then(
                function (serverOtp) {
                    // Reset Waiting Flag
                    vm.bWaiting = false

                    // Set OTP
                    $scope.regInfo.otp = serverOtp

                    // Prompt User to verify OTP
                    $state.go('home.otp')
                },
                function (error) {
                    // Reset Waiting Flag
                    vm.bWaiting = false

                    // Show error toast
                    DialogService.alert("Registration error : " + error)
                }
            )
        }
    }

    // Error Reporting Methods for different input fields
    vm.nameErr = function () {
        if (!$scope.regInfo.name) {
            return "Name is required"
        }
    }

    vm.emailErr = function () {
        if (!$scope.regInfo.email) {
            return "Email is required"
        } else if (($scope.regInfo.email.indexOf('@') == -1) || ($scope.regInfo.email.indexOf('.') == -1)) {
            return "Invalid Email"
        }

        return null
    }

    vm.companyErr = function () {
        if (!$scope.regInfo.companyName) {
            return "Company name is required"
        }

        return null
    }

    vm.passwordErr = function () {
        if (!$scope.regInfo.password) {
            return "Password is required"
        }

        return null
    }

    vm.confirmPasswordErr = function () {
        if (!$scope.regInfo.confirmPassword) {
            return "Password is required"
        } else if ($scope.regInfo.confirmPassword != $scope.regInfo.password) {
            return "Password does not match"
        }

        return null
    }

    /*------------------------------------ Private Methods --------------------------------*/
    // Method to validate registration info
    function validate() {
        // Reset error flag
        vm.bInputError = false

        // validate info
        if (vm.nameErr() ||
            vm.emailErr() ||
            vm.companyErr() ||
            vm.passwordErr() ||
            vm.confirmPasswordErr()) {
            vm.bInputError = true
        }

        return !vm.bInputError
    }

    /*------------------------------------ Post Init --------------------------------*/
})