/**
 * Created by Siddharth on 18-09-2017.
 */

// Controller for Alert Dialog
app.controller('EditFormCtrl', function ($scope, $rootScope, $http, $localStorage, $mdDialog, ToastService, form, updateCb) {

    $scope.form = form;

    $scope.save = function () {
        $rootScope.showWaitingDialog("Please wait while form is being updated...")
        $http({
            method:     'POST',
            url:        '/api/users/form',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            },
            data: {
                'form': $scope.form
            }
        })
        .then(
            function (response) {
                $rootScope.hideWaitingDialog()
                // Hide dialog and show toast
                $mdDialog.hide()
                ToastService.toast("Form updated successfully...")

                // Trigger Callback
                updateCb()
            },
            function (error) {
                $rootScope.hideWaitingDialog()
                console.log(error)
                ToastService.toast("Could not update form...")
            }
        )
    }

    $scope.cancel = function () {
        $mdDialog.hide()
    }
})
