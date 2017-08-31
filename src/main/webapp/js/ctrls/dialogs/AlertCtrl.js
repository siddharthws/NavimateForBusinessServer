/**
 * Created by Siddharth on 29-08-2017.
 */

// Controller for Alert Dialog
app.controller('AlertCtrl', function ($scope, $mdDialog, message) {

    $scope.message = message

    $scope.ok = function () {
        $mdDialog.hide()
    }
})