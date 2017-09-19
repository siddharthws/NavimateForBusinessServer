/**
 * Created by Siddharth on 18-09-2017.
 */

// Controller for Alert Dialog
app.controller('EditFormCtrl', function ($scope, $http, $localStorage, $mdDialog, ToastService, form, updateCb) {

    $scope.form = form;

    $scope.save = function () {
        // Placeholder
    }

    $scope.cancel = function () {
        $mdDialog.hide()
    }
})
