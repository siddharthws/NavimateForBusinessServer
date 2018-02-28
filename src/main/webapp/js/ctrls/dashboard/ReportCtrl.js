/**
 * Created by Siddharth on 11-12-2017.
 */

app.controller("ReportCtrl", function ($scope, $rootScope, $http, $localStorage, ToastService, TableService) {
    /* ------------------------------- INIT -----------------------------------*/
    var vm = this

    // Set menu and option
    $scope.nav.item       = Constants.DashboardNav.Menu[Constants.DashboardNav.ITEM_REPORTS]
    $scope.nav.option     = Constants.DashboardNav.Options[Constants.DashboardNav.OPTION_REPORT]

    // Set form table as active
    TableService.activeTable = TableService.formTable
    vm.table = TableService.activeTable

    /*-------------------------------------- Scope APIs ---------------------------------------*/
    // APIs for table based actions
    vm.sync = function () {
        // Broadcast Sync Table Event
        $scope.$broadcast(Constants.Events.TABLE_SYNC)
    }

    vm.export = function () {
        // Broadcast Export Table Event
        $scope.$broadcast(Constants.Events.TABLE_EXPORT)
    }

    vm.clearFilters = function () {
        // Broadcast Clear Filter Event
        $scope.$broadcast(Constants.Events.TABLE_CLEAR_FILTERS)
    }

    vm.toggleColumns = function () {
        // Broadcast Toggle Columns Event
        $scope.$broadcast(Constants.Events.TABLE_TOGGLE_COLUMNS)
    }
})
