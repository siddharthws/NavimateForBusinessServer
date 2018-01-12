/**
 * Created by Chandel on 05-01-2018.
 */

app.controller("CompanyProfileCtrl", function ($scope, $rootScope, $http, $localStorage) {
    var vm = this

    /*------------------------------------ INIT --------------------------------*/
    // Set menu and option
    $scope.nav.item     = Constants.DashboardNav.Menu[Constants.DashboardNav.ITEM_COMPANY]
    $scope.nav.option   = Constants.DashboardNav.Options[Constants.DashboardNav.OPTION_PROFILE]
    vm.companyName      = $localStorage.companyName
    vm.apiKey           = $localStorage.apiKey
})