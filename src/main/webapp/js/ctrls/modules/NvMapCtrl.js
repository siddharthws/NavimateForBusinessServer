/**
 * Created by Siddharth on 07-05-2018.
 */

app.controller('NvMapCtrl', function ($scope) {
    /*------------------------------------ Init --------------------------------*/
    var vm = this

    /*------------------------------------ Public APIs --------------------------------*/
    // Map Init Callback from ngMap
    vm.mapInitialized = function (googleMap) {
        // init google map in local map object
        $scope.objMap.initGoogleMap(googleMap)

        // Run angular digest cycle since this is async callback
        $scope.$apply()
    }

    // Marker click event
    vm.markerClick = function (event, idx) {
        // update Selected marker
        $scope.objMap.selectedMarker = $scope.objMap.markers[idx]

        // Center map on this marker
        $scope.objMap.setCenter($scope.objMap.selectedMarker.position)

        // Trigger callback
        $scope.onMarkerClick({idx: idx})
    }

    /*------------------------------------ Private APIs --------------------------------*/
    /*------------------------------------ Post Init --------------------------------*/
})
