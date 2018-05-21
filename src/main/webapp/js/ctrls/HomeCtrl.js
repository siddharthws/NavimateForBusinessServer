/**
 * Created by Siddharth on 01-09-2017.
 */

app.controller('HomeCtrl', function ($scope, $state, $window) {
    /*------------------------------------ INIT --------------------------------*/
    var vm = this

    // Registration Info
    // variable is attached to scope since ti is required
    // by both OTP verification screen and registration screen
    $scope.regInfo = {  name: "",
                        email: "",
                        password: "",
                        role: Constants.Role.REGISTRATION[0],
                        companyName: "",
                        confirmPassword: ""}

    /*------------------------------------ Public Methods --------------------------------*/
    // Method to handle action button
    vm.getBtnText = function () {
        // Check state and return appropriate text
        if ($state.current.name == 'home.login') {
            return "Register"
        } else if ($state.current.name == 'home.register') {
            return "Login"
        }
    }

    vm.isBtnVisible = function () {
        // Button should be visible on login / register states only
        return ($state.current.name == 'home.login') || ($state.current.name == 'home.register')
    }

    vm.btnClick = function () {
        if ($state.current.name == 'home.login') {
            // Go to register page
            $state.go("home.register")
        } else if ($state.current.name == 'home.register') {
            // Go to login page
            $state.go("home.login")
        }
    }

    // Method to redirect to navimateapp.com
    vm.knowMore = function () {
        $window.open("https://www.navimateapp.com", "_blank")
    }

    /*------------------------------------ Private Methods --------------------------------*/
    /*------------------------------------ Post Init --------------------------------*/
})