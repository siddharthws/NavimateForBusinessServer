/**
 * Created by Siddharth on 23-08-2017.
 */

app.controller('DashboardCtrl', function ($scope, $rootScope, $state, $window, $localStorage, AuthService, DialogService, TeamDataService, LeadDataService, TemplateDataService, TaskDataService) {

    /*------------------------------------ INIT --------------------------------*/
    // Menu Selection Parameters
    $scope.nav = {}
    $scope.name = $localStorage.name
    $scope.role = $localStorage.role

    // Sync all data on Initialization
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
})
