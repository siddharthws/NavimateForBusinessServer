/**
 * Created by Siddharth on 23-08-2017.
 */

app.controller('DashboardCtrl', function ($scope, $rootScope, $state, $window, $localStorage, AuthService, DialogService) {

    /*------------------------------------ INIT --------------------------------*/
    // Menu Selection Parameters
    $scope.nav = {}
    $scope.name = $localStorage.name

    /*------------------------------------ APIs --------------------------------*/
    // Button Click APIs
    $scope.logout = function(){
        $rootScope.showWaitingDialog("Please wait while you are being logged out...")
        AuthService.logout()
            .then(
                function (response) {
                    $rootScope.hideWaitingDialog()
                    $localStorage.accessToken = ""
                    $state.go("home")
                },
                function (error) {
                    $rootScope.hideWaitingDialog()
                    console.log(error)
                }
            )
    }
    
    $scope.changePassword = function () {
        DialogService.changePassword()
    }

    $scope.onOptionClick = function (option) {
        // Prepare State URL
        var state = "dashboard." + $scope.nav.item.name + "-" + option.name

        // Update State to this option
        $state.go(state)
    }
    
    $scope.openHelp = function () {
        $window.open($state.href('help'), "_blank")
    }
})
