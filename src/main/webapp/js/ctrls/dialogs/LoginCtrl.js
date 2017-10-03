/**
 * Created by Siddharth on 01-09-2017.
 */

app.controller('LoginCtrl', function ($scope, $mdDialog, $state, $localStorage, AuthService, DialogService, ToastService) {

    /* ------------------------------- Scope APIs -----------------------------------*/
    // Button Click APIs
    $scope.login = function(){

        // Validate credentials
        if (validate()) {
            AuthService.login($scope.phoneNumber, $scope.password)
                .then(
                    function (response) {
                        // Hide Dialog
                        $mdDialog.hide()

                        // Redirect to dashboard
                        $localStorage.accessToken = response.data.accessToken;
                        $localStorage.name = response.data.name;
                        $state.go("dashboard.team-manage")
                    },
                    function (error) {
                        // Show error toast
                        ToastService.toast("Invalid Credentials...")
                    }
                )
        }
    }

    $scope.register = function () {
        // Launch register dialog
        DialogService.register()
    }

    $scope.cancel = function () {
        $mdDialog.hide()
    }

    /* ------------------------------- Local APIs -----------------------------------*/
    function validate() {
        if (!$scope.phoneNumber || !$scope.password) {
            // Raise error flag
            $scope.bShowError = true

            // Show error toast
            ToastService.toast("Please fill all fields !!!")

            return false
        }

        return true
    }

    /* ------------------------------- INIT -----------------------------------*/
    $scope.bShowError = false;

})