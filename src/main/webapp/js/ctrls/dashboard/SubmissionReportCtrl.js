/**
 * Created by Siddharth on 11-12-2017.
 */

app.controller("SubmissionReportCtrl",
                function ($scope, $rootScope, $http, $localStorage,
                         ToastService, TableService, NavService) {
    /* ------------------------------- INIT -----------------------------------*/
    var vm = this

    // Set Active Tab and Menu
    NavService.setActive(NavService.reports, 0)

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
