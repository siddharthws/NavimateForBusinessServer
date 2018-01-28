/**
 * Created by Siddharth on 22-11-2017.
 */

// Controller for Location Viewer Dialog
app.controller('LocationViewerCtrl', function ($scope, latitude, longitude) {
    var vm = this

    /* ------------------------------- Html APIs -----------------------------------*/
    /* ------------------------------- Local APIs -----------------------------------*/
    /* ------------------------------- INIT -----------------------------------*/

    // Init Map Parameters
    $scope.mapParams = {}
    $scope.mapParams.markers = []

    // Add markers using parameters
    $scope.mapParams.markers.push({
        title: "Temporary Title",
        latitude: latitude,
        longitude: longitude
    })
})
