/**
 * Created by Siddharth on 07-05-2018.
 */

app.factory('ObjMarker', function() {

    // Class definition for marker object
    ObjMarker = function (id, name, position) {
        // ----------------------------------- Init ------------------------------------//
        var vm = this

        // Set properties from params
        vm.id = id
        vm.name = name
        vm.position = position
        vm.bExcludeFromClustering = false

        // Show marker by default
        vm.bShow = true

        // ----------------------------------- Public APIs ------------------------------------//
        // Method to determine if the marker should be shown
        vm.isShowing = function () {
            // Either latitude or longitude should be valid
            return Statics.isPositionValid(vm.position) && vm.bShow
        }

        // Method to get extended bounds of a marker as per current map view
        vm.getExtendedBounds = function (projection) {
            // Convert the points to pixels and the extend out by the grid size.
            var trPix = projection.fromLatLngToDivPixel(vm.position);
            var blPix = projection.fromLatLngToDivPixel(vm.position);
            trPix.x += 40;
            trPix.y -= 40;
            blPix.x -= 40;
            blPix.y += 40;

            // Convert the pixel points back to LatLng
            var ne = projection.fromDivPixelToLatLng(trPix);
            var sw = projection.fromDivPixelToLatLng(blPix);

            // Create bounds object using new position
            var bounds = new google.maps.LatLngBounds()
            bounds.extend(ne);
            bounds.extend(sw);
            return bounds;
        }

        // ----------------------------------- Private APIs ------------------------------------//
        // ----------------------------------- Post Init ------------------------------------//

    }

    // ----------------------------------- Static APIs ------------------------------------//

    return ObjMarker
})
