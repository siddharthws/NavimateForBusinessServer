/**
 * Created by Siddharth on 19-11-2017.
 */

// Controller for Toggle Columns Dialog
app.controller('ToggleColumnsCtrl', function ($scope, $mdDialog, columns, resultCb) {

    $scope.columns = columns

    $scope.done = function () {
        resultCb($scope.columns)
        $mdDialog.hide()
    }

    $scope.close = function () {
        $mdDialog.hide()
    }
})
