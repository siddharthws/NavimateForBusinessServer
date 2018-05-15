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

        // Show marker by default
        vm.bShow = true

        // Set possible icons
        vm.icon = {
            url: "/static/images/marker_selected.png",
            scaledSize: [40, 40]
        }
        vm.iconActive = {
            url: "/static/images/marker_selected.png",
            scaledSize: [40, 40]
        }

        // ----------------------------------- Public APIs ------------------------------------//
        // Method to determine if the marker should be shown
        vm.isShowing = function () {
            // Either latitude or longitude should be valid
            return Statics.isPositionValid(vm.position)
        }

        // ----------------------------------- Private APIs ------------------------------------//
        // ----------------------------------- Post Init ------------------------------------//

    }

    // ----------------------------------- Static APIs ------------------------------------//

    return ObjMarker
})
