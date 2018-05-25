/**
 * Created by Siddharth on 23-05-2018.
 */
app.factory('ObjPolyline', function() {

    // Class definition for polyline object
    ObjPolyline = function (path, color) {
        // ----------------------------------- Init ------------------------------------//
        var vm = this

        // Set properties from params
        vm.path = path ? path : []
        vm.color = color ? color : '#37bcf2'

        // ----------------------------------- Public APIs ------------------------------------//
        // ----------------------------------- Private APIs ------------------------------------//
        // ----------------------------------- Post Init ------------------------------------//

    }

    // ----------------------------------- Static APIs ------------------------------------//

    return ObjPolyline

})
