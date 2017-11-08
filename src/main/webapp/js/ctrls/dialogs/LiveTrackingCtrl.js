/**
 * Created by aroha on 15-09-2017.
 */

// Controller for Alert Dialog
app.controller('LiveTrackingCtrl', function ($scope, $mdDialog, $interval, team) {

    $scope.cancel = function () {
        $mdDialog.hide()
        $interval.cancel(liveTrack)
    }

    $scope.temp = function () {

                 initTrackData()

                fakeData = function(){
                    $scope.team[0].trackData.location.lat  +=0.002
                    $scope.team[0].trackData.location.lng  +=0.002
                    $scope.team[1].trackData.location.lat  +=0.002
                    $scope.team[1].trackData.location.lng  +=0.002

                    console.log($scope.team);
            };
            liveTrack =  $interval(fakeData, 1000);
            fakeData();
    }

    function initTrackData(){
        for(var i = 0; i<$scope.team.length ;i++){
            var trackData= {
            status: Constants.Tracking.STATUS_WAITING,
            location: {
                lat: 20.0,
                lng: 75.0
            },
            speed: 0,
            lastUpdated : 0
        }
            $scope.team[i].trackData = trackData
        }
    }

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
    /* ------------------------------- INIT -----------------------------------*/
    // Init objects
    var liveTrack
    $scope.selectedRep = {}
    $scope.team =  team
    $scope.mapCenter = [21, 75]
    $scope.mapZoom   = 4
    var googleMap = null

    // start intializing fakedata
    $scope.temp()
})
