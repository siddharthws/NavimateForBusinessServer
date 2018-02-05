/**
 * Created by Siddharth on 23-08-2017.
 */

app.controller('DashboardCtrl', function ($scope, $rootScope, $state, $window, $localStorage, AuthService, DialogService, TeamDataService, LeadDataService, TemplateDataService, TaskDataService, ToastService) {

    /*------------------------------------ INIT --------------------------------*/
    // Menu Selection Parameters
    $scope.nav = {}
    $scope.name = $localStorage.name
    $scope.role = $localStorage.role
    var bLeadSync, bTeamSync, bTaskSync, bFormTemplateSync, bLeadTemplateSync, bTaskTemplateSync = false

    // Sync all data on Initialization
    $rootScope.showWaitingDialog("Please wait while we are fetching data...")
    TeamDataService.sync()
    LeadDataService.sync()
    TemplateDataService.syncForms()
    TemplateDataService.syncLeads()
    TemplateDataService.syncTasks()
    TaskDataService.sync()

    // Attach Data service methods
    $rootScope.getRepById = TeamDataService.getById
    $rootScope.getLeadById = LeadDataService.getById
    $rootScope.getTaskById = TaskDataService.getById
    $rootScope.getTemplateById = TemplateDataService.getTemplateById
    $rootScope.getFieldById = TemplateDataService.getFieldById

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
        if(!bLeadSync && !bTeamSync && !bTaskSync && !bFormTemplateSync && !bLeadTemplateSync && !bTaskTemplateSync ){
            $rootScope.hideWaitingDialog()
        }
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
    $scope.$on(Constants.Events.FORM_TEMPLATE_DATA_READY, function (event, params) {
        bFormTemplateSync = true
        checkSync()
    })
    $scope.$on(Constants.Events.LEAD_TEMPLATE_DATA_READY, function (event, params) {
        bLeadTemplateSync = true
        checkSync()
    })
    $scope.$on(Constants.Events.TASK_TEMPLATE_DATA_READY, function (event, params) {
        bTaskTemplateSync = true
        checkSync()
    })

    // Listener for data load error
    $scope.$on(Constants.Events.DATA_LOAD_ERROR, function (event, params) {
       DialogService.alert("Unable to load data !!!")
    })

})
