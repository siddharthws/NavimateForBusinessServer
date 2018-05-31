/**
 * Created by Chandel on 11-02-2018.
 */

app.controller("MovementReportCtrl", function ($rootScope, $scope, NavService, LocReportDS, DialogService, TableService, ObjMap, ObjMarker, ObjPolyline) {
    /*-------------------------------------- INIT ---------------------------------------------*/
    var vm = this

    // Set Active Tab and Menu
    NavService.setActive(NavService.reports, 1)

    // Init Variables
    vm.selectedRep = null
    vm.selectedDate = ""
    vm.report = []
    vm.forms = []
    vm.selection = null

    // Init Map
    vm.map = new ObjMap([])

    // Init Map parameters
    $scope.mapParams = {}

    //Set chart Properties
    vm.labels = ["Active", "Inactive"];
    vm.data = [50, 50];
    vm.colors = ['#4CAF50','#CFD8DC'];
    vm.options = {cutoutPercentage: 85}

    // Start and end time of report
    vm.startTime = null
    vm.endTime = null

    /*-------------------------------------- Public APIs ---------------------------------------*/
    vm.sync = function() {
        // Get report only if both date and rep are selected
        if(vm.selectedDate && vm.selectedRep) {
            // Reset report
            vm.report = []

            // Start Async Request to get data
            $rootScope.showWaitingDialog("Getting Report...")
            LocReportDS.sync(vm.selectedRep.id, vm.selectedDate).then(
                // Success Callback
                function () {
                    // Set report to cache
                    vm.report = LocReportDS.cache.report.points
                    vm.distance = LocReportDS.cache.report.distance
                    vm.forms = LocReportDS.cache.forms

                    // Update map markers and polyline
                    updateMap()

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

    vm.pickRep = function () {
        DialogService.tablePicker("Pick Representative", TableService.teamTable, function (id, name) {
            vm.selectedRep = {id: id, name: name}
            vm.sync()
        })
    }

    vm.viewRep = function () {
        DialogService.teamViewer(vm.selectedRep.id)
    }

    vm.onMarkerClick = function (idx) {
        // Get selected marker
        var selectedMarker = vm.map.markers[idx]

        // Trigger Form Viewer dialog if this is a form marker otherwise mark report selection
        if (selectedMarker.icon) {
            DialogService.formViewer(selectedMarker.id)
        } else {
            vm.selection = vm.report[selectedMarker.id]
        }
    }

    /*-------------------------------------- Private APIs ---------------------------------------*/

    // Function to create markers from report
    function updateMap () {
        // init polylines and markers from report
        var polylines = []
        var markers = []
        if(vm.report.length){
            // Get list of indexes where status is changing
            var changeIdxs = [0]
            for (var i = 0; i < vm.report.length - 1; i++) {
                if (vm.report[i].status != vm.report[i+1].status) {
                    changeIdxs.push(i+1)
                }
            }

            // Create polylines from list of indexes where status changes
            for (var i = 0; i < changeIdxs.length; i++) {
                // Get first and last index for this polyline
                var firstIndex = changeIdxs[i]
                var lastIndex = (i != changeIdxs.length - 1) ? changeIdxs[i+1] : vm.report.length - 1

                // Create a new polyline with new status color from this point
                var reportObj = vm.report[firstIndex]
                var polyColor = reportObj.status == Constants.Tracking.ERROR_NONE ? '#37bcf2' : '#ff0000'
                var polyline = new ObjPolyline([], polyColor)

                // Add points to polyline
                for (var j = firstIndex; j <= lastIndex; j++) {
                    polyline.path.push([vm.report[j].latitude, vm.report[j].longitude])
                }

                // Add to list of polylines
                polylines.push(polyline)
            }

            // Create marker for each element in report
            vm.report.forEach(function (reportObj, i) {
                // Add marker if time is valid
                if (reportObj.time) {
                    var marker = new ObjMarker(i, reportObj.time, new google.maps.LatLng(reportObj.latitude, reportObj.longitude))
                    markers.push(marker)
                }
            })

            // Add start and end text to first and last markers
            if (markers.length) {
                markers[0].bg = Constants.Map.MARKER_GREEN
                markers[0].bExcludeFromClustering = true
                vm.startTime = markers[0].name
                markers[markers.length - 1].bg = Constants.Map.MARKER_RED
                markers[markers.length - 1].bExcludeFromClustering = true
                vm.endTime = markers[markers.length - 1].name
            }
        }

        // Add markers for forms
        vm.forms.forEach(function (form) {
            var formMarker = new ObjMarker( form.id, null, new google.maps.LatLng(form.lat, form.lng),
                                            Constants.Map.MARKER_GREEN, "static/images/ic_report_white.png")
            formMarker.bExcludeFromClustering = true
            markers.push(formMarker)
        })

        // set map markers
        vm.map.markers = markers

        // Set map polyline
        vm.map.polylines = polylines

        // Re-center map
        vm.map.recenter()
    }

})