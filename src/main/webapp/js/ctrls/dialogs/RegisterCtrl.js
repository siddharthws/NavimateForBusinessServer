/**
 * Created by Siddharth on 01-09-2017.
 */

app.controller('RegisterCtrl', function ($scope, $rootScope, $mdDialog, AuthService, DialogService, ToastService, regInfo) {


    $scope.bShowError = false

    // Array of dropdown items to select roles
    $scope.roles = [{id: Constants.Role.ADMIN, name: "Admin"},
                    {id: Constants.Role.CC, name: "Customer Care"},
                    {id: Constants.Role.MANAGER, name: "Manager"}]

    $scope.regInfo = regInfo
    if (!$scope.regInfo) {
        $scope.regInfo = {name: "", email: "", password: "", role: $scope.roles[0], companyName: ""}
        $scope.confirmPassword = $scope.regInfo.password
    }
    /* ------------------------------- Scope APIs -----------------------------------*/
    // Button Click APIs
    $scope.register = function(){

        // Validate credentials
        if (validate()) {
            $rootScope.showWaitingDialog("Validating info...")
            AuthService.validateRegistration($scope.regInfo).then(
                function (otp) {
                    $rootScope.hideWaitingDialog()
                    DialogService.emailVerify(  $scope.regInfo,
                                                otp)
                },
                function (error) {
                    $rootScope.hideWaitingDialog()
                    ToastService.toast("Registration Error : " + error)
                }
            )
        }
    }

    $scope.cancel = function () {
        $mdDialog.hide()
    }

    /* ------------------------------- Local APIs -----------------------------------*/
    function validate() {
        if (!$scope.regInfo.email ||
            !$scope.regInfo.password ||
            !$scope.regInfo.name ||
            !$scope.confirmPassword ||
            !$scope.regInfo.role ||
            !$scope.regInfo.companyName) {
            // Raise error flag
            $scope.bShowError = true

            // Show error toast
            ToastService.toast("Please fill all fields !!!")

            return false
        }

        // Valid passwords
        if ($scope.regInfo.password != $scope.confirmPassword) {
            // Show error toast
            ToastService.toast("Passwords do not match !!!")
            return false
        }

        // Validate Email
        if (($scope.regInfo.email.indexOf('@') == -1) ||
            ($scope.regInfo.email.indexOf('.') == -1)) {
            ToastService.toast("Invalid Email")
            return false
        }

        return true
    }

    /* ------------------------------- INIT -----------------------------------*/
})