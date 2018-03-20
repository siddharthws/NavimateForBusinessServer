/**
 * Created by Siddharth on 20-03-2018.
 */
app.controller('TeamViewerCtrl', function ($scope, $mdDialog,
                                           TeamService, id) {
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
    TeamService.sync([id]).then(
        // Success
        function () {
            vm.bLoading = false
            vm.obj = TeamService.cache[0]
        },
        // Error
        function () {
            vm.bLoading = false
            vm.bLoadError = true
        }
    )
})
