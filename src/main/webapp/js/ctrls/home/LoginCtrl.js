/**
 * Created by Siddharth on 01-09-2017.
 */

app.controller('LoginCtrl', function ($scope, $state, $localStorage, $cookies,
                                      AuthService, ToastService, DialogService) {
    /*------------------------------------ INIT --------------------------------*/
    var vm = this

    // Error Flag
    vm.bInputError = false

    // Email / Password to be entered by the user
    vm.email            = ""
    vm.password         = ""

    /*------------------------------------ Public Methods --------------------------------*/
    // Method to login
    vm.login = function () {
        if (validate()) {
            // Set Waiting Flag
            vm.bWaiting = true

            AuthService.login(vm.email, vm.password).then(
                function () {
                    // Remember user info
                    rememberMe()

                    // Reset Waiting Flag
                    vm.bWaiting = false

                    // Redirect to dashboard
                    $state.go("dashboard-loading")
                },
                function (error) {
                    // Reset Waiting Flag
                    vm.bWaiting = false

                    // Show error toast
                    DialogService.alert("Login error : " + error)
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

    vm.passwordErr = function () {
        if (!vm.password) {
            return "Password is required"
        }

        return null
    }

    /*------------------------------------ Private Methods --------------------------------*/
    // Method to validate input
    function validate() {
        // Reset error flag
        vm.bInputError = false

        // Check for input errors
        if (vm.emailErr() || vm.passwordErr()) {
            vm.bInputError = true
        }

        return !vm.bInputError
    }

    // Method to save login details when login has been done
    function rememberMe() {
        // Check if user wants to be remembered
        if (vm.bRemember) {
            // Expiration date of cookies
            var expireDate = new Date()
            expireDate.setDate(expireDate.getDate() + 7)

            // Add username and password to cookies
            $cookies.put(Constants.Cookie.KEY_EMAIL, vm.email, {expires: expireDate})
            $cookies.put(Constants.Cookie.KEY_PASSWORD, vm.password, {expires: expireDate})
        } else {
            // Remove username and password cookies
            $cookies.remove(Constants.Cookie.KEY_EMAIL)
            $cookies.remove(Constants.Cookie.KEY_PASSWORD)
        }
    }

    /*------------------------------------ Post Init --------------------------------*/
    // Retrieve saved username and password
    var savedEmail = $cookies.get(Constants.Cookie.KEY_EMAIL)
    var savedPassword = $cookies.get(Constants.Cookie.KEY_PASSWORD)
    if (savedEmail || savedPassword) {
        vm.bRemember = true
        vm.email = savedEmail
        vm.password = savedPassword
    }
})