/**
 * Created by Siddharth on 11-12-2017.
 */

app.controller("ReportCtrl", function ($scope, $rootScope, $http, $localStorage) {
    /*-------------------------------------- Scope APIs ---------------------------------------*/
    /*-------------------------------------- Local APIs ---------------------------------------*/
    this.syncTable = function () {
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

                    // Trigger Init data callback for table view
                    $scope.tableParams.functions.initTable()
                },
                function (error) {
                    $rootScope.hideWaitingDialog()

                    // Show Error Toast
                    ToastService.toast("Unable to load report !!!")
                }
            )
    }

    /*-------------------------------------- INIT ---------------------------------------------*/
    // Set menu and option
    $scope.nav.item       = Constants.DashboardNav.Menu[Constants.DashboardNav.ITEM_REPORTS]
    $scope.nav.option     = Constants.DashboardNav.Options[Constants.DashboardNav.OPTION_REPORT]

    // Init Table Parametes
    $scope.tableParams = {}
    $scope.tableParams.style = {
        bBordered: true
    }
    $scope.tableParams.exportName = 'Navimate-Report'
    $scope.tableParams.columns = []
    $scope.tableParams.values = []

    // Add sync table function to table params
    $scope.tableParams.functions = {}
    $scope.tableParams.functions.syncTable = this.syncTable

    // Sync Table now
    this.syncTable()
})
