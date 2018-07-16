/**
 * Created by Aroha on 16-07-2018.
 */
app.controller('ProductViewerCtrl', function ($scope, $mdDialog,
                                           ProductService, id) {
    /* ------------------------------- Init -----------------------------------*/
    var vm = this

    // Init Object
    vm.obj = null

    // Waiting / Error Flag
    vm.bLoading = false
    vm.bLoadError = false

    /* ------------------------------- Public APIs -----------------------------------*/
    vm.close = function () {
        $mdDialog.hide()
    }

    /* ------------------------------- Private APIs -----------------------------------*/
    /* ------------------------------- Post Init -----------------------------------*/
    // Get object by ID
    vm.bLoading = true
    ProductService.sync([id]).then(
        // Success
        function () {
            vm.bLoading = false
            vm.obj = ProductService.cache[0]
        },
        // Error
        function () {
            vm.bLoading = false
            vm.bLoadError = true
        }
    )
})
