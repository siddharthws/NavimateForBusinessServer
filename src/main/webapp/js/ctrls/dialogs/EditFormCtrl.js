/**
 * Created by Siddharth on 18-09-2017.
 */

// Controller for Alert Dialog
app.controller('EditFormCtrl', function ($scope, $http, $localStorage, $mdDialog, ToastService, form, updateCb) {

    $scope.form = form;

    $scope.save = function () {
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
                // Hide dialog and show toast
                $mdDialog.hide()
                ToastService.toast("Form updated successfully...")

                // Trigger Callback
                updateCb()
            },
            function (error) {
                console.log(error)
                ToastService.toast("Could not update form...")
            }
        )
    }

    $scope.cancel = function () {
        $mdDialog.hide()
    }
})
