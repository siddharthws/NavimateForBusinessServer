/**
 * Created by Siddharth on 01-09-2017.
 */

app.controller('HomeCtrl', function ($scope, DialogService) {

    $scope.login = function () {
        // Placeholder
    }

    $scope.register = function () {
        DialogService.register()
    }

})