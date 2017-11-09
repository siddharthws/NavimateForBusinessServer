/**
 * Created by aroha on 15-09-2017.
 */

// Controller for Alert Dialog
app.controller('LiveTrackingCtrl', function ($scope, $rootScope, $mdDialog, $interval, $localStorage, ToastService, reps) {

    /* ------------------------------- Scope APIs -----------------------------------*/
    // Map Init Callback from ngMap
    $scope.mapInitialized = function (map) {
        // Set map object
        googleMap = map

        // Center map on added leads
        var bounds = new google.maps.LatLngBounds()
        $scope.team.forEach(function (rep) {
            bounds.extend(new google.maps.LatLng(rep.trackData.location.lat,rep.trackData.location.lng))
        })
        googleMap.fitBounds(bounds)
    // Trigger resize event (Hack since map is not loaded correctly second time)
        google.maps.event.trigger(googleMap, 'resize')
        // Run angular digest cycle since this is async callback
        $scope.$apply()
    }

        // API to get marker icon
    $scope.getMarkerIcon = function (rep) {
        // Blue marker for selected rep
            return {
                url: "/static/images/marker_selected.png",
                scaledSize: [40, 40]
            }
    }

    $scope.listItemClick = function (rep) {
        // Select this lead
        $scope.selectedRep = rep

        // Center map on this lead
        if (googleMap){
            googleMap.panTo(new google.maps.LatLng(rep.trackData.location.lat, rep.trackData.location.lng))
        }
    }

    $scope.cancel = function () {
        // Stop refresh callback
        $interval.cancel(refreshCb)

        // Stop live tracking on server
        $rootScope.showWaitingDialog("Stopping Live tracking...")
        $http({
            method:     'POST',
            url:        '/api/track/stop',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            }
        }).then(
            function (response) {
                $rootScope.hideWaitingDialog()
                $mdDialog.hide()
            },
            function (error) {
                $rootScope.hideWaitingDialog()
                $mdDialog.hide()
            }
        )
    }

    /* ------------------------------- Local APIs -----------------------------------*/
    function init() {
        // Init Track Data Structure
        reps.forEach(function (rep) {
            $scope.trackees.push({
                id:             rep.id,
                name:           rep.name,
                phone:          rep.phoneNumber,
                latitude:       0,
                longitude:      0,
                lastUpdated:    0,
                speed:          0,
                status:         Constants.Tracking.STATUS_WAITING
            })
        })
        $scope.selectedTrackee = $scope.trackees[0]

        // Http Request to start tracking
        $rootScope.showWaitingDialog("Starting Live Tracking...")
        $http ({
            method:     'POST',
            url:        '/api/track/start',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            },
            data:       {
                'reps':     reps
            }
        }).then(
            function (response) {
                $rootScope.hideWaitingDialog();

                // Start periodic Data Refresh
                refreshCb = $interval(refreshData, 1000)
            },
            function (error) {
                $rootScope.hideWaitingDialog();

                // Show Error Toast
                ToastService.toast("Unable to start live tracking...")
            }
        )
    }

    function refreshData() {
        $http({
            method:     'GET',
            url:        '/api/track/data',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            }
        }).then(
            function (response) {
                // Copy all server data from response object to tracking object
                response.data.forEach(function (serverData) {
                    $scope.trackees.forEach(function (currentData) {
                        if (currentData.id == serverData.id) {
                            currentData.latitude    = serverData.latitude
                            currentData.longitude   = serverData.longitude
                            currentData.lastUpdated = serverData.lastUpdated
                            currentData.speed       = serverData.speed
                            currentData.status      = serverData.status
                        }
                    })
                })
            },
            function (error) {
            }
        )
    }

    /* ------------------------------- INIT -----------------------------------*/
    // Init objects
    $scope.mapCenter = [21, 75]
    $scope.mapZoom   = 4
    var googleMap = null

    // Tracking related variables
    $scope.trackees = []
    $scope.selectedTrackee = {}
    var refreshCb

    // Init Live Tracking
    init()
})
