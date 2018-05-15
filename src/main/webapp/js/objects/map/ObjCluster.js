/**
 * Created by Chandel on 27-02-2018.
 */

app.factory('ObjCluster', function() {
    ObjCluster = function (map) {
        // ----------------------------------- INIT ------------------------------------//
        var vm = this
        vm.map_ = map;
        vm.gridSize_ = 60;
        vm.markers_ = [];
        var overlay = new google.maps.OverlayView();
        overlay.draw = function() {};
        overlay.setMap(map);

        /*----------------------------------------Functions--------------------------------------------*/
        //Extends a bounds object by the grid size.
        vm.getExtendedBounds = function(bounds) {
            var projection = overlay.getProjection();
            if(projection == null)
                return bounds

            // Turn the bounds into latlng.
            var tr = new google.maps.LatLng(bounds.getNorthEast().lat(),
            bounds.getNorthEast().lng());
            var bl = new google.maps.LatLng(bounds.getSouthWest().lat(),
            bounds.getSouthWest().lng());

            // Convert the points to pixels and the extend out by the grid size.
            var trPix = projection.fromLatLngToDivPixel(tr);
            trPix.x += vm.gridSize_;
            trPix.y -= vm.gridSize_;

            var blPix = projection.fromLatLngToDivPixel(bl);
            blPix.x -= vm.gridSize_;
            blPix.y += vm.gridSize_;

            // Convert the pixel points back to LatLng
            var ne = projection.fromDivPixelToLatLng(trPix);
            var sw = projection.fromDivPixelToLatLng(blPix);

            // Extend the bounds to contain the new bounds.
            bounds.extend(ne);
            bounds.extend(sw);
            return bounds;
        };

        // Determines if a marker lies in the clusters bounds.
        vm.isMarkerInClusterBounds = function(latlng) {
            return vm.getExtendedBounds(vm.getBounds()).contains(latlng);
        };

        //Returns the bounds of the cluster.
        vm.getBounds = function() {
            var bounds = new google.maps.LatLngBounds();
            for (var i = 0, marker; marker = vm.markers_[i]; i++) {
                bounds.extend(new google.maps.LatLng(marker.latitude, marker.longitude));
            }
            return bounds;
        };

        //Add a marker the cluster.
        vm.addMarker = function(marker) {
            vm.markers_.push(marker)
        };
    }
    return ObjCluster
});