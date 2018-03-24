/**
 * Created by Siddharth on 23-03-2018.
 */

// Controller for Location Picker Dialog
app.controller('LocationPickerCtrl', function ($scope, $mdDialog, GoogleApiService, ToastService,
                                               lat, lng, cb) {
    /* ------------------------------- INIT -----------------------------------*/
    var vm = this

    // Picked location details
    vm.address = ""
    vm.lat = lat
    vm.lng = lng

    // Init error / waiting flags
    vm.bConverting = false

    // Init Map Parameters
    $scope.mapParams = {}
    $scope.mapParams.lat = vm.lat
    $scope.mapParams.lng = vm.lng
    $scope.mapParams.markers = []

    /* ------------------------------- Public APIs -----------------------------------*/
    vm.addressUpdated = function (address) {
        // Update address
        vm.address = address

        // Get LatLng
        vm.bConverting = true
        GoogleApiService.addressToLatlng(address).then(
            function (latlng) {
                // Reset flag
                vm.bConverting = false

                // Update cache
                vm.lat = latlng.latitude
                vm.lng = latlng.longitude

                // Recenter map
                centerMap()
            },
            function () {
                // Reset flag
                vm.bConverting = false

                // Show error toast
                ToastService.toast("Could not get lat, lng from address")
            }
        )
    }

    vm.close = function () {
        $mdDialog.hide()
    }

    vm.pick = function () {
        // Get location at center of map
        var latlng = $scope.mapParams.getCenter()

        // Ignore if invalid location
        if (!latlng.latitude && !latlng.longitude) {
            ToastService.toast("Please select a valid place")
            return
        }

        // Check if cached location matches picked location
        if (vm.lat == latlng.latitude && vm.lng == latlng.longitude && vm.address) {
            // Hide dialog
            $mdDialog.hide()

            // Trigger callback directly
            cb(vm.address, vm.lat, vm.lng)
        } else {
            // Get address from latlng
            vm.bConverting = true
            GoogleApiService.latlngToAddress(latlng.latitude, latlng.longitude).then(
                function (address) {
                    // Hide dialog
                    vm.bConverting = false
                    $mdDialog.hide()

                    // Trigger callback
                    cb(address, latlng.latitude, latlng.longitude)
                },
                function () {
                    vm.bConverting = false
                    ToastService.toast("Unable to fetch address for this location...")
                }
            )
        }
    }

    /* ------------------------------- Private APIs -----------------------------------*/
    // Method to center map on cached lat,lng
    function centerMap() {
        $scope.$broadcast(Constants.Events.MAP_CENTER, {latitude: vm.lat, longitude: vm.lng})
    }
})
