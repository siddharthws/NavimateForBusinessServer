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
    vm.markerClick = function (event, idx) {
        // update Selected marker
        selectedMarker = $scope.mapParams.markers[idx]

        // Emit marker click event to be handled by parent
        $scope.$emit(Constants.Events.MAP_MARKER_CLICK, {idx: idx})
    }

    // Marker drag events
    vm.markerDragend = function(event, idx) {
        //Get selected Marker
        var marker = $scope.mapParams.markers[idx]

        // Update marker's latitude and longitude
        marker.latitude   = event.latLng.lat()
        marker.longitude  = event.latLng.lng()

        // Emit marker Dragged event to be handled by parent
        $scope.$emit(Constants.Events.MAP_MARKER_DRAGEND, {idx:idx})
    }

    //Map Marker Icon Update
    vm.getMarkerIcon = function (idx) {
        if(idx ==$scope.mapParams.markers.indexOf(selectedMarker)) {
            return {
                url: "/static/images/marker_selected.png",
                scaledSize: [40, 40]
            }
        }
        else {
            return {
                url: "/static/images/marker_default.png",
                scaledSize: [40, 40]
            }
        }
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
                if (vm.markers.length) {
                    // Center using markers
                    if (vm.markers.length == 1) {
                        // Center on marker if only 1 is present
                        centerMap(new google.maps.LatLng(   vm.markers[0].latitude,
                                                            vm.markers[0].longitude))
                    } else {
                        // Center using bounds
                        var bounds = new google.maps.LatLngBounds()
                        for (var i = 0; i < vm.markers.length; i++) {
                            bounds.extend(new google.maps.LatLng(   vm.markers[i].latitude,
                                                                    vm.markers[i].longitude))
                        }
                        googleMap.fitBounds(bounds)
                    }
                } else if (currentLatLng) {
                    // Center map on current latlng
                    centerMap(currentLatLng)
                }
            }
        }
    }

    /*------------------------------------ Init --------------------------------*/
    var googleMap = null
    var currentLatLng = null
    var selectedMarker = null

    // Add map params
    $scope.mapParams.zoom = 14

    if($scope.mapParams.markers.length){
        selectedMarker = $scope.mapParams.markers[0]
    }

    // Add properties to vm
    vm.markers = $scope.mapParams.markers

    // Add event listeners
    // Listener for map center event
    $scope.$on(Constants.Events.MAP_CENTER, function (event, param) {
        centerMap(new google.maps.LatLng(param.latitude, param.longitude))
    })

    // Listener to update Selected marker
    $scope.$on(Constants.Events.MAP_MARKER_SELECTED, function (event, param) {
      selectedMarker = $scope.mapParams.markers[param.idx]
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