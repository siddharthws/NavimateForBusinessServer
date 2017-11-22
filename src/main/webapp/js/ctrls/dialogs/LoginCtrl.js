/**
 * Created by Siddharth on 01-09-2017.
 */

app.controller('LoginCtrl', function ($scope, $rootScope, $mdDialog, $state, $http, $localStorage, $cookies, AuthService, DialogService, ToastService) {

    var COOKIE_EMAIL = "Navm8Email"
    var COOKIE_PASSWORD = "Navm8Password"

    /* ------------------------------- Scope APIs -----------------------------------*/
    // Button Click APIs
    $scope.login = function(){

        // Validate credentials
        if (validate()) {
            $rootScope.showWaitingDialog("Please wait while we log you in...")

            AuthService.login($scope.email, $scope.password)
                .then(
                    function (response) {
                        // Save access token and user info
                        $localStorage.accessToken = response.data.accessToken;
                        $localStorage.name = response.data.name;

                        // Check if user wants to be remembered
                        if ($scope.bRemember) {
                            // Add username and password to cookies
                            $cookies.put(COOKIE_EMAIL, $scope.email)
                            $cookies.put(COOKIE_PASSWORD, $scope.password)
                        } else {
                            // Remove username and password cookies
                            $cookies.remove(COOKIE_EMAIL)
                            $cookies.remove(COOKIE_PASSWORD)
                        }

                        // Hide waiting dialog
                        $rootScope.hideWaitingDialog()

                        // Hide Login Dialog
                        $mdDialog.hide()

                        // Redirect to dashboard
                        $state.go("dashboard.team-manage")
                    },
                    function (error) {
                        $rootScope.hideWaitingDialog()

                        // Show error toast
                        ToastService.toast("Invalid Credentials...")
                    }
                )
        }
    }
    
    $scope.forgotPassword = function () {
        // Check if email has been entered
        if (!$scope.email) {
            ToastService.toast("Please enter a valid Email Id...")
            return
        }

        $rootScope.showWaitingDialog("Please wait while we email your password..")
        $http({
            method:     'POST',
            url:        '/api/auth/forgotPassword',
            data:       {
                email:    $scope.email
            }
        })
        .then(
            function (response) {
                $rootScope.hideWaitingDialog()
                ToastService.toast("Your password has been mailed to your email ID...")
            },
            function (error) {
                $rootScope.hideWaitingDialog()
                if (error.status == 400) {
                    ToastService.toast("Unknown email provided...")
                } else {
                    ToastService.toast("Unable to email your password...")
                }
            })
    }

    $scope.register = function () {
        // Launch register dialog
        DialogService.register("", "", "")
    }

    $scope.cancel = function () {
        $mdDialog.hide()
    }

    /* ------------------------------- Local APIs -----------------------------------*/
    function validate() {
        if (!$scope.email || !$scope.password) {
            // Raise error flag
            $scope.bShowError = true

            // Show error toast
            ToastService.toast("Please fill all fields !!!")

            return false
        }

        return true
    }

    /* ------------------------------- INIT -----------------------------------*/
    $scope.bShowError = false
    $scope.bRemember = false
    $scope.email = ""
    $scope.password = ""

    // Retrieve saved username and password
    var savedEmail = $cookies.get(COOKIE_EMAIL)
    var savedPassword = $cookies.get(COOKIE_PASSWORD)
    if (savedEmail || savedPassword) {
        $scope.bRemember = true
        $scope.email = savedEmail
        $scope.password = savedPassword
    }
})