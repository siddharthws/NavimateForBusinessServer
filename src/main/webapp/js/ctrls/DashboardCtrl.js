/**
 * Created by Siddharth on 23-08-2017.
 */

app.controller('DashboardCtrl',
                function (  $scope, $rootScope, $state, $window, $localStorage,
                            AuthService, DialogService, ToastService, NavService,
                            TemplateService, TeamService) {
    /*------------------------------------ INIT --------------------------------*/
    var vm = this

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
    var bError = false
    var bTemplateSync = false

    // Sync all data on Initialization
    $rootScope.showWaitingDialog("Please wait while we are fetching data...")

    // Sync Templates
    TemplateService.sync().then(
        // Success
        function () {
            bTemplateSync = true
            checkSync()
        },
        // Error
        function () {
            handleError()
        }
    )

    // Sync managers for admin
    if ($localStorage.role >= Constants.Role.CC) {
        TeamService.syncManagers()
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
                    $state.go("home")
                },
                function (error) {
                    $rootScope.hideWaitingDialog()
                    console.log(error)
                }
            )
    }

    // Attach Admin Checker API
    $rootScope.isAdmin = function () {
        return $localStorage.role == Constants.Role.ADMIN
    }

    $rootScope.isCC = function () {
        return $localStorage.role >= Constants.Role.CC
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

    function checkSync(){
        if(bTemplateSync){
            $rootScope.hideWaitingDialog()
        }
    }

    function handleError() {
        // Ignore if error flag is already marker
        if (bError) {
            return
        }
        bError = true

        // Hide Waiting dialog
        $rootScope.hideWaitingDialog()

        // Show Error Toast
        ToastService.toast("Unable to load data !!!")
    }
    /*------------------------------------EVENT LISTENERS --------------------------------*/

})
