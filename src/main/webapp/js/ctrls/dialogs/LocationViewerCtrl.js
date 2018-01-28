/**
 * Created by Siddharth on 22-11-2017.
 */

// Controller for Location Viewer Dialog
app.controller('LocationViewerCtrl', function ($scope, locations) {
    var vm = this

    /* ------------------------------- Html APIs -----------------------------------*/
    /* ------------------------------- Local APIs -----------------------------------*/
    /* ------------------------------- INIT -----------------------------------*/

    // Init Map Parameters
    $scope.mapParams = {}
    $scope.mapParams.markers = []

    // Add markers using parameters
    locations.forEach(function (location) {
        $scope.mapParams.markers.push({
            title: location.title,
            latitude: location.latitude,
            longitude: location.longitude
        })
    })
})
