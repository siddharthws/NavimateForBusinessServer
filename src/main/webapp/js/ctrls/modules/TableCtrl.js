/**
 * Created by Siddharth on 24-02-2018.
 */

app.controller('TableCtrl', function (  $scope, $window, $state,
                                        TableService, DialogService) {
    /* ----------------------------- INIT --------------------------------*/
    var vm  = this

    // Add Constants
    vm.TableConst = Constants.Table
    vm.FilterConst = Constants.Filter
    vm.TemplateConst = Constants.Template

    // Error / Waiting flags
    vm.bError = false
    vm.bWaiting = false

    // Get table object from service
    vm.table = TableService.activeTable

    // Sync table data if required
    if (!vm.table.columns.length) {
        sync(true)
    }

    /* ----------------------------- Public APIs --------------------------------*/

    // API to show photo on new page
    vm.showImage = function (filename) {
        $window.open($state.href('photos', {name: filename}), "_blank")
    }

    // API to show location on map
    vm.showLocation = function (latlng) {
        // Split location into lat & lng
        var latLngArr = latlng.split(',')

        // Prepare Location array to send to location viewer
        var locations = []
        locations.push({
            title:      "Picked Location",
            latitude:   latLngArr[0],
            longitude:  latLngArr[1]
        })

        // Open Location Viewer dialog
        DialogService.locationViewer(locations)
    }

    /* ----------------------------- Private APIs --------------------------------*/
    // Method to re-initialize data
    function sync(bColumns) {
        // Set flags
        vm.bError = false
        vm.bWaiting = true

        // Start Async Request to get data
        vm.table.sync(bColumns).then(
            // Success Callback
            function (success) {
                // Reset Waiting flag
                vm.bWaiting = false
            },
            // Error Callback
            function (error) {
                // Reset Waiting flag
                vm.bWaiting = false

                // Set Error flag
                vm.bError = true
            }
        )
    }

    /* ----------------------------- Event Listeners --------------------------------*/
    // Re sync data when Sync Event is triggered
    $scope.$on(Constants.Events.TABLE_SYNC, function (event, args) {
        // Sync data
        sync(false)
    })
})
