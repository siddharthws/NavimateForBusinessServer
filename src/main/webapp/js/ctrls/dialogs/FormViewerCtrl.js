/**
 * Created by Siddharth on 31-05-2018.
 */
app.controller('FormViewerCtrl', function ($scope, $mdDialog, FormService, DialogService,
                                           id) {
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
            title: vm.obj.rep.name
        }])
    }

    vm.close = function () {
        $mdDialog.hide()
    }

    /* ------------------------------- Private APIs -----------------------------------*/
    /* ------------------------------- Post Init -----------------------------------*/
    // Get object by ID
    vm.bLoading = true
    FormService.sync([id]).then(
        // Success
        function (forms) {
            vm.bLoading = false
            vm.obj = forms[0]
        },
        // Error
        function () {
            vm.bLoading = false
            vm.bLoadError = true
        }
    )
})
