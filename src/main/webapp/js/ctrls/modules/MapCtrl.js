/**
 * Created by Siddharth on 19-12-2017.
 */
app.controller('MapCtrl', function ($scope) {
    var vm = this

    /*------------------------------------ HTML APIs --------------------------------*/
    // Map Init Callback from ngMap
    vm.mapInitialized = function (map) {
        // Set map object
        googleMap = map

        // Initialize Map Center to current location if required
        initMapCenter()

        // Trigger resize event (Hack since map is not loaded correctly second time)
        google.maps.event.trigger(googleMap, 'resize')

        // Run angular digest cycle since this is async callback
        $scope.$apply()
    }

    // Marker click event
    vm.markerClick = function (idx) {
        // Emit marker click event to be handled by parent
        $scope.$emit(Constants.Events.MAP_MARKER_CLICK, {idx: idx})
    }

    /*------------------------------------ Local APIs --------------------------------*/
    // API to re-center the map on added markers
    function centerMap(latLng) {
        // Ignore if map not loaded
        if (!googleMap) {
            return
        }

        // Ignore invalid latlng
        if (!latLng || (!latLng.lat() && !latLng.lng())) {
            return
        }

        // Pan map to this latlng
        googleMap.panTo(latLng)
    }
    
    // APi to Initialize map center
    function initMapCenter() {
        // Check if map is loaded
        if (googleMap) {
            // Check if map is centered on 0,0
            var mapCenter = googleMap.getCenter()
            if (!mapCenter.lat() && !mapCenter.lng()) {
                // Check if current location is available
                if (currentLatLng) {
                    // Center map on current latlng
                    centerMap(currentLatLng)
                }
            }
        }
    }

    /*------------------------------------ Init --------------------------------*/
    var googleMap = null
    var currentLatLng = null

    // Add map params
    $scope.mapParams.zoom = 14

    // Add properties to vm
    vm.markers = $scope.mapParams.markers

    // Add event listeners
    // Listener for map center event
    $scope.$on(Constants.Events.MAP_CENTER, function (event, param) {
        centerMap(new google.maps.LatLng(param.latitude, param.longitude))
    })

    // Get current location to center map
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(function (position) {
            // Set current Latlng cache
            currentLatLng = new google.maps.LatLng(position.coords.latitude, position.coords.longitude)

            // Init Map Center
            initMapCenter()
        })
    }
})