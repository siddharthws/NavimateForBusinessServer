/**
 * Created by Siddharth on 21-11-2017.
 */

app.controller('ChangePasswordCtrl', function ($scope, $rootScope, $mdDialog, $localStorage, $http, ToastService) {

    /* ------------------------------- Scope APIs -----------------------------------*/
    // Button Click APIs
    $scope.change = function(){
        // Validate credentials
        if (validate()) {
            $rootScope.showWaitingDialog("Please wait while we update your password...")
            $http({
                method:     'POST',
                url:        '/api/users/changePassword',
                headers:    {
                    'X-Auth-Token':    $localStorage.accessToken
                },
                data:       {
                    oldPassword:    $scope.oldPassword,
                    newPassword:    $scope.newPassword
                }
            })
            .then(
                function (response) {
                    $rootScope.hideWaitingDialog()

                    // Hide dialog and show toast
                    $mdDialog.hide()
                    ToastService.toast("Your password has been updated...")
                },
                function (error) {
                    $rootScope.hideWaitingDialog()
                    if (error.status == 400) {
                        ToastService.toast("Incorrect current password entered...")
                    }
                })
        }
    }

    $scope.close = function () {
        $mdDialog.hide()
    }

    /* ------------------------------- Local APIs -----------------------------------*/
    function validate() {
        // Check for empty fields
        if (!$scope.oldPassword || !$scope.newPassword || !$scope.confirmPassword) {
            // Raise error flag
            $scope.bShowError = true

            // Show error toast
            ToastService.toast("Please fill all fields !!!")

            return false
        }

        // Check if password and confirm password matches
        if ($scope.newPassword != $scope.confirmPassword) {
            // Show error toast
            ToastService.toast("New and confirmed passwords do not match !!!")
            return false
        }

        return true
    }

    /* ------------------------------- INIT -----------------------------------*/
    $scope.bShowError = false;
})
