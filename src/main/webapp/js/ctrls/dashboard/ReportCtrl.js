/**
 * Created by Siddharth on 11-12-2017.
 */

app.controller("ReportCtrl", function ($scope, $rootScope, $http, $localStorage, ToastService) {
    var vm = this

    /*-------------------------------------- Scope APIs ---------------------------------------*/
    // APi to sync table data with server and re-initialize table
    vm.syncTable = function () {
        $rootScope.showWaitingDialog("Please wait while we are fetching the report...")
        $http({
            method:     'GET',
            url:        '/api/users/report',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            }
        })
            .then(
                function (response) {
                    $rootScope.hideWaitingDialog()
                    $scope.tableParams.columns = response.data.columns
                    $scope.tableParams.values = response.data.values

                    // Broadcast Table Init Event
                    $scope.$broadcast(Constants.Events.TABLE_INIT)
                },
                function (error) {
                    $rootScope.hideWaitingDialog()

                    // Show Error Toast
                    ToastService.toast("Unable to load report !!!")
                }
            )
    }

    // APIs for table based actions
    vm.export = function () {
        // Broadcast Toggle Columns Event
        $scope.$broadcast(Constants.Events.TABLE_EXPORT, {filename: 'Navimate-Report'})
    }

    vm.clearFilters = function () {
        // Broadcast Toggle Columns Event
        $scope.$broadcast(Constants.Events.TABLE_CLEAR_FILTERS)
    }

    vm.toggleColumns = function () {
        // Broadcast Toggle Columns Event
        $scope.$broadcast(Constants.Events.TABLE_TOGGLE_COLUMNS)
    }

    /*-------------------------------------- Local APIs ---------------------------------------*/
    /*-------------------------------------- INIT ---------------------------------------------*/
    // Set menu and option
    $scope.nav.item       = Constants.DashboardNav.Menu[Constants.DashboardNav.ITEM_REPORTS]
    $scope.nav.option     = Constants.DashboardNav.Options[Constants.DashboardNav.OPTION_REPORT]

    // Init Table Parametes
    $scope.tableParams = {}
    $scope.tableParams.style = {
        bBordered: true
    }
    $scope.tableParams.columns = []
    $scope.tableParams.values = []

    // Sync Table once on init
    vm.syncTable()
})
