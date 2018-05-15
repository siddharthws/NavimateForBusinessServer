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
        if (vm.markers.length) {
            vm.selectedMarker = vm.markers[0]
        }

        // ----------------------------------- Public methods ------------------------------------//
        // Method to initialize google map
        vm.initGoogleMap = function (googleMap) {
            // Set map
            vm.gMap = googleMap

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

            // ry centering using marker bounds
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

            vm.markers.forEach(function (marker) {
                bounds.extend(marker.position)
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

        // ----------------------------------- Post Init ------------------------------------//
    }

    // ----------------------------------- Static APIs ------------------------------------//

    return ObjMap
})
