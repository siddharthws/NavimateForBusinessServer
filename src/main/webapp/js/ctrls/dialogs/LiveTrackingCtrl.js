/**
 * Created by aroha on 15-09-2017.
 */

// Controller for Alert Dialog
app.controller('LiveTrackingCtrl', function ($scope, $rootScope, $http, $mdDialog, $interval, $localStorage, ToastService, reps) {

    /* ------------------------------- Scope APIs -----------------------------------*/
    // API to refresh Rep Status
    $scope.refreshReps = function () {
        // Get list of unavailable reps
        var unAvReps = []
        $scope.trackees.forEach(function (trackee) {
            if (trackee.status == Constants.Tracking.STATUS_UNAVAILABLE) {
                unAvReps.push(trackee.id)
            }
        })

        if (unAvReps.length > 0) {
            // Send Http request to refresh Unavailable reps
            $http({
                method:     'POST',
                url:        '/api/track/refresh',
                headers:    {
                    'X-Auth-Token':    $localStorage.accessToken
                },
                data:       {
                    'reps':     unAvReps
                }
            }).then(
                function (response) {
                },
                function (error) {
                }
            )
        }
    }

    // Map Init Callback from ngMap
    $scope.mapInitialized = function (map) {
        // Set map object
        googleMap = map

        // Trigger resize event (Hack since map is not loaded correctly second time)
        google.maps.event.trigger(googleMap, 'resize')

        // Run angular digest cycle since this is async callback
        $scope.$apply()
    }

    $scope.listItemClick = function (trackee) {
        $scope.selectedTrackee = trackee
    }

    $scope.markerClick = function (event, trackee) {
        $scope.listItemClick(trackee)
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
    // Tracking related variables
    $scope.trackees = []
    $scope.selectedTrackee = {}
    var refreshCb

    // Map related vars
    $scope.mapZoom = 14
    var googleMap = null

    // Init Live Tracking
    init()
})
