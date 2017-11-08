/**
 * Created by Siddharth on 04-10-2017.
 */

app.controller('IndexCtrl', function ($scope, $rootScope) {
    // Attach Constants
    $rootScope.Constants = Constants

    // Data object for waiting dialog
    $scope.waiting = {
        bShow: false,
        message: ""
    }

    // APIs to show / hide dialogs
    $rootScope.showWaitingDialog = function (message) {
        $scope.waiting.message = message
        $scope.waiting.bShow = true
    }

    $rootScope.hideWaitingDialog = function () {
        $scope.waiting.bShow = false
    }
})
