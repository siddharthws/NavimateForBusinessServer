/**
 * Created by Siddharth on 20-03-2018.
 */
app.controller('LeadViewerCtrl', function ($scope, $mdDialog, DialogService,
                                           LeadService, id) {
    /* ------------------------------- Init -----------------------------------*/
    var vm = this

    // Init Object
    vm.obj = null

    // Waiting / Error Flag
    vm.bLoading = false
    vm.bLoadError = false

    /* ------------------------------- Public APIs -----------------------------------*/
    vm.viewLocation = function () {
        DialogService.locationViewer([{
            latitude: vm.obj.lat,
            longitude: vm.obj.lng,
            title: vm.obj.name
        }])
    }

    vm.close = function () {
        $mdDialog.hide()
    }

    /* ------------------------------- Private APIs -----------------------------------*/
    /* ------------------------------- Post Init -----------------------------------*/
    // Get object by ID
    vm.bLoading = true
    LeadService.sync([id]).then(
        // Success
        function () {
            vm.bLoading = false
            vm.obj = LeadService.cache[0]
        },
        // Error
        function () {
            vm.bLoading = false
            vm.bLoadError = true
        }
    )
})
