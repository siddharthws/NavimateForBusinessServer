/**
 * Created by Siddharth on 01-09-2017.
 */

app.controller('HomeCtrl', function ($scope, DialogService) {

    $scope.login = function () {
        DialogService.login()
    }

    $scope.register = function () {
        DialogService.register()
    }
    
    $scope.explore=function () {
        if ($("#div_feature").is(':hidden')) {
            $("#div_feature").show(2000);
            $("#explore_btn").text("Hide Features");
        } else {
            $("#div_feature").slideUp(2000);
            $("#explore_btn").text("Explore Features");
        }
    }
})