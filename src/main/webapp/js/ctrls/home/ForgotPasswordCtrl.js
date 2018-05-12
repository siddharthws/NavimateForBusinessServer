/**
 * Created by Siddharth on 11-05-2018.
 */
app.controller('ForgotPasswordCtrl', function ( $scope, $state, $localStorage, $cookies,
                                                AuthService, ToastService, DialogService) {
    /*------------------------------------ INIT --------------------------------*/
    var vm = this

    // Error Flag
    vm.bInputError = false

    // Email to be entered by the user
    vm.email            = ""

    /*------------------------------------ Public Methods --------------------------------*/
    // Method to login
    vm.forgotPassword = function () {
        if (validate()) {
            // Set Waiting Flag
            vm.bWaiting = true

            AuthService.forgotPassword(vm.email).then(
                function () {
                    // Show success toast
                    ToastService.toast("Email sent succesfully...")

                    // Redirect to login screen
                    $state.go("home.login")
                },
                function (error) {
                    // Reset Waiting Flag
                    vm.bWaiting = false

                    // Show error toast
                    DialogService.alert("Error while sending email : " + error)
                }
            )
        }
    }

    // methods to report input errors
    vm.emailErr = function () {
        if (!vm.email) {
            return "Email is required"
        } else if ((vm.email.indexOf('@') == -1) || (vm.email.indexOf('.') == -1)) {
            return "Invalid Email"
        }

        return null
    }

    /*------------------------------------ Private Methods --------------------------------*/
    // Method to validate input
    function validate() {
        // Reset error flag
        vm.bInputError = false

        // Check for input errors
        if (vm.emailErr()) {
            vm.bInputError = true
        }

        return !vm.bInputError
    }

    /*------------------------------------ Post Init --------------------------------*/
})
