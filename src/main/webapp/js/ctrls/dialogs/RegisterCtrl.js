/**
 * Created by Siddharth on 01-09-2017.
 */

app.controller('RegisterCtrl', function ($scope, $rootScope, $mdDialog, AuthService, DialogService, ToastService, name, email, password, role, companyName) {

    /* ------------------------------- Scope APIs -----------------------------------*/
    // Button Click APIs
    $scope.register = function(){

        // Validate credentials
        if (validate()) {
            DialogService.emailVerify(  $scope.name,
                                        $scope.email,
                                        $scope.password,
                                        $scope.role,
                                        $scope.companyName)
        }
    }

    $scope.cancel = function () {
        $mdDialog.hide()
    }

    /* ------------------------------- Local APIs -----------------------------------*/
    function validate() {
        if (!$scope.email || !$scope.password || !$scope.name || !$scope.confirmPassword || !$scope.role || !$scope.companyName) {
            // Raise error flag
            $scope.bShowError = true

            // Show error toast
            ToastService.toast("Please fill all fields !!!")

            return false
        }

        // Valid passwords
        if ($scope.password != $scope.confirmPassword) {
            // Show error toast
            ToastService.toast("Passwords do not match !!!")
            return false
        }

        // Validate Email
        if (($scope.email.indexOf('@') == -1) ||
            ($scope.email.indexOf('.') == -1)) {
            ToastService.toast("Invalid Email")
            return false
        }

        return true
    }

    /* ------------------------------- INIT -----------------------------------*/
    $scope.bShowError = false;
    $scope.name = name
    $scope.email = email
    $scope.password = password
    $scope.confirmPassword = password
    $scope.roles=["Manager","Admin"]
    $scope.companyName=companyName
    $scope.role=role

})