/**
 * Created by Siddharth on 23-08-2017.
 */

app.controller('DashboardCtrl',
                function (  $scope, $rootScope, $state, $window, $localStorage, $timeout,
                            AuthService, DialogService, ToastService, NavService, LocationService) {
    /*------------------------------------ INIT --------------------------------*/
    var vm = this

    // Go to loading page if app is not loaded
    if (!$rootScope.bAppLoaded) {
        $state.go('dashboard-loading')
    }

    // Set Nav controls
    vm.nav = NavService

    // Prepare 2D array of menu items so that HRs can be displayed
    vm.menus = [
        [vm.nav.team, vm.nav.leads, vm.nav.tasks],
        [vm.nav.reports, vm.nav.templates],
        [vm.nav.company]
    ]

    // Menu Selection Parameters
    $scope.nav = {}
    $scope.name = $localStorage.name
    $scope.role = $localStorage.role

    // Init Location Service
    LocationService.init()

    /*------------------------------------ Root APIs --------------------------------*/
    // Access Checker API
    $rootScope.isAdmin = function () {
        return $localStorage.role == Constants.Role.ADMIN
    }

    $rootScope.isCC = function () {
        return $localStorage.role >= Constants.Role.CC
    }

    /*------------------------------------ APIs --------------------------------*/
    // Button Click APIs
    $scope.logout = function(){
        $rootScope.showWaitingDialog("Please wait while you are being logged out...")
        AuthService.logout()
            .then(
                function (response) {
                    $rootScope.hideWaitingDialog()
                    $localStorage.accessToken = ""
                    $state.go("home.login")
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

    /*------------------------------------EVENT LISTENERS --------------------------------*/
    /*------------------------------------Post Init --------------------------------*/
    // remove cover after 100ms. This is done so that when user is on dashboard and he refreshes
    // refreshes the webpage, the page should not flicker before redirecting to loading screen
    $timeout(function () {
        vm.bNoCover = true
    }, 100)

})
