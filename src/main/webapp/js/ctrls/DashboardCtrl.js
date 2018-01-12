/**
 * Created by Siddharth on 23-08-2017.
 */

app.controller('DashboardCtrl', function ($scope, $rootScope, $state, $window, $localStorage, AuthService, DialogService, TeamDataService) {

    /*------------------------------------ INIT --------------------------------*/
    // Menu Selection Parameters
    $scope.nav = {}
    $scope.name = $localStorage.name
    $scope.role = $localStorage.role

    // Sync all data on Initialization
    TeamDataService.sync()

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
    
    $scope.openHelp = function () {
        $window.open($state.href('help'), "_blank")
    }
    
    $scope.optionClicked = function (state) {
        $state.go(state)
    }
})
