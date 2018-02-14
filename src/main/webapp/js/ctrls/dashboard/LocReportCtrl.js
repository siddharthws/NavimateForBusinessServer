/**
 * Created by Chandel on 11-02-2018.
 */

app.controller("LocReportCtrl", function ($scope, TeamDataService) {
    var vm = this

    /*-------------------------------------- Scope APIs ---------------------------------------*/
    /*-------------------------------------- Local APIs ---------------------------------------*/
    /*-------------------------------------- INIT ---------------------------------------------*/
    // Set menu and option
    $scope.nav.item       = Constants.DashboardNav.Menu[Constants.DashboardNav.ITEM_REPORTS]
    $scope.nav.option     = Constants.DashboardNav.Options[Constants.DashboardNav.OPTION_LOCATION]

    // Init Variables
    vm.team = []
    $scope.mapParams = {}
    vm.selectedRep = {}

    // Get Team Data
    vm.team =  TeamDataService.cache.data
})