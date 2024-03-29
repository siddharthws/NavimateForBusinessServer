/**
 * Created by Siddharth on 07-05-2018.
 */

app.factory('ObjMap', function($timeout, LocationService) {

    //
    // Class definition for Map Object
    //
    ObjMap = function (markers) {
        // ----------------------------------- Init ------------------------------------//
        var vm = this

        vm.gMap = null

        // Marker related info
        vm.markers = markers

        // Selected marker
        vm.selectedMarker = null
        if (vm.markers && vm.markers.length) {
            vm.selectedMarker = vm.markers[0]
        }

        // Polyline related info
        vm.polylines = []

        // ----------------------------------- Public methods ------------------------------------//
        // Method to initialize google map
        vm.initGoogleMap = function (googleMap) {
            // Set map
            vm.gMap = googleMap

            // Set event listeners
            google.maps.event.addListener(googleMap, 'idle', idleListener);

            // Center Map on init
            vm.recenter()

            // Trigger resize event (Hack since map is not loaded correctly second time)
            google.maps.event.trigger(vm.gMap, 'resize')
        }

        // methods to set / get map center
        vm.getCenter = function () {
            var center = new google.maps.LatLng(0, 0)

            if (vm.gMap != null) {
                center = vm.gMap.getCenter()
            }

            return center
        }

        vm.setCenter = function (position) {
            // Ignore if map is not loaded
            if (vm.gMap == null) {
                return
            }

            // Ignore if position is invalid
            if (!Statics.isPositionValid(position)) {
                return
            }

            // Set default zoom if centering for first time
            if (!Statics.isPositionValid(vm.getCenter())) {
                vm.gMap.setZoom(Constants.Map.DEFAULT_ZOOM)
            }

            // Set map center
            vm.gMap.panTo(position)
        }

        // Method to recenter map using appropriate method as per map data
        vm.recenter = function () {
            // Ignore if map is not initialized
            if (vm.gMap == null) {
                return
            }

            // try centering using marker & polyline bounds
            var bounds = getMarkerBounds()
            if (!bounds.isEmpty() && Statics.isPositionValid(bounds.getCenter())) {
                vm.gMap.fitBounds(bounds)
                return
            }

            // Try centering using current location
            if (Statics.isPositionValid(LocationService.curLocation)) {
                vm.setCenter(LocationService.curLocation)
                return
            }
        }

        // ----------------------------------- Private methods ------------------------------------//
        // Method to get LatLngBounds from markers
        function getMarkerBounds() {
            var bounds = new google.maps.LatLngBounds()

            // Get marker bounds
            vm.markers.forEach(function (marker) {
                bounds.extend(marker.position)
            })

            // Get polyline bounds
            vm.polylines.forEach(function (polyline) {
                polyline.path.forEach(function (point) {
                    bounds.extend(new google.maps.LatLng(point[0], point[1]))
                })
            })

            // Don't zoom in too far on only one marker
            var ne = bounds.getNorthEast(), sw = bounds.getSouthWest()
            if (ne.equals(sw)) {
                var extendPoint1 = new google.maps.LatLng(ne.lat() + 0.01, sw.lng() + 0.01);
                var extendPoint2 = new google.maps.LatLng(ne.lat() - 0.01, sw.lng() - 0.01);
                bounds.extend(extendPoint1);
                bounds.extend(extendPoint2);
            }

            return bounds
        }

        //update cluster event
        function clusterMarkers (){
            // Ignore if map is not initialized
            if (vm.gMap == null) {
                return
            }

            // hide all markers as default in idle state
            vm.markers.forEach(function (marker) {
                marker.bShow = false
            })

            // Get map projection
            var overlay = new google.maps.OverlayView()
            overlay.setMap(vm.gMap)
            var projection = overlay.getProjection()

            // Set marker visibility as per clustering
            var visibleMarkers = []

            // Add all markers that need to be excluded from cluster into visible markers
            vm.markers.forEach(function (marker) {
                if (marker.bExcludeFromClustering) {
                    marker.bShow = true
                    visibleMarkers.push(marker)
                }
            })

            // Add other markers to clusters
            for(var i = 0; i < vm.markers.length; i++) {
                var marker = vm.markers[i]

                // Skip if non clusterable marker
                if (marker.bExcludeFromClustering) {
                    continue
                }

                // Skip the markers that are not inside the current map-view
                if(!vm.gMap.getBounds().contains(marker.position)) {
                    continue
                }

                // Check if this marker ishould be shown based on other markers which are already showing
                var bShow = true
                for (var j = 0; j < visibleMarkers.length; j++) {
                    var visMarker = visibleMarkers[j]
                    var extBounds = visMarker.getExtendedBounds(projection)
                    if (extBounds.contains(marker.position)) {
                        bShow = false
                        break
                    }
                }

                // Show the marker if required
                marker.bShow = bShow

                // Add to visible markers if showing
                if (bShow) {
                    visibleMarkers.push(marker)
                }
            }
        }


        // ----------------------------------- Event listeners ------------------------------------//
        // Listener for map idle event
        function idleListener() {
            // Re cluster the markers when mad is idle
            $timeout(clusterMarkers, 0)
        }

        // ----------------------------------- Post Init ------------------------------------//
    }

    // ----------------------------------- Static APIs ------------------------------------//

    return ObjMap
})
