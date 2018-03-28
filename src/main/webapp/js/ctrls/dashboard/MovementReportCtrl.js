/**
 * Created by Chandel on 11-02-2018.
 */

app.controller("MovementReportCtrl", function ($rootScope, $scope, $filter, LocReportDS) {
    /*-------------------------------------- INIT ---------------------------------------------*/
    var vm = this

    // Set menu and option
    $scope.nav.item       = Constants.DashboardNav.Menu[Constants.DashboardNav.ITEM_REPORTS]
    $scope.nav.option     = Constants.DashboardNav.Options[Constants.DashboardNav.OPTION_LOCATION]

    // Init Variables
    vm.team = []
    vm.selectedRep = null
    vm.selectedDate = ""
    vm.report = []

    // Init Map parameters
    $scope.mapParams = {}

    //Set chart Properties
    vm.labels = ["Active", "Inactive"];
    vm.data = [50, 50];
    vm.colors = ['#4CAF50','#CFD8DC'];
    vm.options = {cutoutPercentage: 85}

    /*-------------------------------------- Public APIs ---------------------------------------*/
    vm.sync = function() {
        // Get report only if both date and rep are selected
        if(vm.selectedDate && vm.selectedRep) {
            // Reset report
            vm.report = []

            // Format the selected the date to 'yyyy-MM-dd' format
            vm.formattedDateString = $filter('date')(vm.selectedDate, 'yyyy-MM-dd')

            // Start Async Request to get data
            $rootScope.showWaitingDialog("Getting Report...")
            LocReportDS.sync(vm.selectedRep.id, vm.formattedDateString).then(
                // Success Callback
                function () {
                    // Set report to cache
                    vm.report = LocReportDS.cache

                    // Update map markers and polyline
                    updatePolylines()
                    updateMarkers()

                    // Hide dialog
                    $rootScope.hideWaitingDialog()
                },
                // Error Callback
                function () {
                    // Hide dialog
                    $rootScope.hideWaitingDialog()
                }
            )
        }
    }

    /*-------------------------------------- Private APIs ---------------------------------------*/
    // Function to create polyline from report
    function updatePolylines() {
        // Reset map polyline
        $scope.mapParams.polylines = []

        var path =[]
        if(vm.report.length){
            // Create path array
            vm.report.forEach(function (reportObj, i) {
                // Check if object has no error
                if (reportObj.status == Constants.Tracking.ERROR_NONE) {
                    // Add lat lng to path
                    path.push([reportObj.latitude, reportObj.longitude])
                }
            })

            //push path in polyline
            $scope.mapParams.polylines.push({
                color : "#37bcf2",
                path : path
            })
        }
    }

    // Function to create markers from report
    function updateMarkers () {
        // Reste map markers
        $scope.mapParams.markers = []

        if(vm.syncData.length != 0){
            // Create marker for each element in report
            vm.report.forEach(function (reportObj, i) {
                // Check if object has no error
                if (reportObj.status == Constants.Tracking.ERROR_NONE) {
                    // Add marker
                    $scope.mapParams.markers.push({
                        bshow: true,
                        title: reportObj.time,
                        latitude: reportObj.latitude,
                        longitude: reportObj.longitude
                    })
                }
            })
        }
    }

})