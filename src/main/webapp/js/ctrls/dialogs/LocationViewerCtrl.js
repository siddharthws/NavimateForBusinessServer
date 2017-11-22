/**
 * Created by Siddharth on 22-11-2017.
 */

// Controller for Location Viewer Dialog
app.controller('LocationViewerCtrl', function ($scope, latitude, longitude) {

    $scope.markerLocation = {
        latitude: latitude,
        longitude: longitude
    }
    $scope.mapZoom = 14

    // Map Init Callback from ngMap
    $scope.mapInitialized = function (map) {
        // Trigger resize event (Hack since map is not loaded correctly second time)
        google.maps.event.trigger(map, 'resize')

        // Set center to given location
        map.setCenter(new google.maps.LatLng($scope.markerLocation.latitude, $scope.markerLocation.longitude))

        // Run angular digest cycle since this is async callback
        $scope.$apply()
    }
})
