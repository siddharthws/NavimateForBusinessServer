/**
 * Created by Siddharth on 23-08-2017.
 */

app.controller('DashboardCtrl', function (  $scope, $rootScope, $state, $window, $localStorage,
                                            AuthService, DialogService, ToastService,
                                            TeamDataService, LeadDataService, TemplateService, TaskDataService) {

    /*------------------------------------ INIT --------------------------------*/
    // Menu Selection Parameters
    $scope.nav = {}
    $scope.name = $localStorage.name
    $scope.role = $localStorage.role
    var bError = false
    var bLeadSync, bTeamSync, bTaskSync, bTemplateSync = false

    // Sync all data on Initialization
    $rootScope.showWaitingDialog("Please wait while we are fetching data...")
    TeamDataService.sync()
    LeadDataService.sync()
    TaskDataService.sync()

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

    // Attach Data service methods
    $rootScope.getRepById = TeamDataService.getById
    $rootScope.getLeadById = LeadDataService.getById
    $rootScope.getTaskById = TaskDataService.getById
    $rootScope.getTemplateById = TemplateService.getTemplateById
    $rootScope.getFieldById = TemplateService.getFieldById

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

    function checkSync(){
        if(bLeadSync && bTeamSync && bTaskSync && bTemplateSync){
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

    $scope.$on(Constants.Events.LEAD_DATA_READY, function (event, params) {
        bLeadSync = true
        checkSync()
    })
    $scope.$on(Constants.Events.TASK_DATA_READY, function (event, params) {
        bTaskSync = true
        checkSync()
    })
    $scope.$on(Constants.Events.TEAM_DATA_READY, function (event, params) {
        bTeamSync = true
        checkSync()
    })

    // Listener for data load error
    $scope.$on(Constants.Events.DATA_LOAD_ERROR, function (event, params) {
        handleError()
    })

})
