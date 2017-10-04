/**
 * Created by Siddharth on 01-09-2017.
 */

app.controller('HomeCtrl', function ($scope, $window, DialogService) {

    $scope.login = function () {
        DialogService.login()
    }

    $scope.register = function () {
        DialogService.register()
    }

    $scope.explore=function(){
        if ($("#div_feature").is(':hidden')) {
            $("#div_feature").show(2000);
            $("#btn_explore").text("Hide Features");
        } else {
            $("#div_feature").slideUp(3000);
            $("#btn_explore").text("Explore Features");
        }
    }
    
    $scope.downloadApp = function () {
        $window.location.href = 'https://play.google.com/store/apps/details?id=com.biz.navimate'
    }

    $scope.facebook = function () {
        $window.location.href = 'https://www.facebook.com/navimateapp/'
    }
})