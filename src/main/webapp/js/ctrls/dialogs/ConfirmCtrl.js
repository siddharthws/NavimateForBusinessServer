/**
 * Created by Siddharth on 31-08-2017.
 */

// Controller for Confirm Dialog
app.controller('ConfirmCtrl', function ($scope, $mdDialog, message, yesCb) {

    $scope.message = message

    $scope.yes = function () {
        $mdDialog.hide()

        // Trigger Callback
        yesCb()
    }

    $scope.no = function () {
        $mdDialog.hide()
    }
})