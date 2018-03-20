/**
 * Created by Siddharth on 20-03-2018.
 */
app.controller('TaskViewerCtrl', function ($scope, $mdDialog,
                                           TaskService, id) {
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
    TaskService.sync([id]).then(
        // Success
        function () {
            vm.bLoading = false
            vm.obj = TaskService.cache[0]
        },
        // Error
        function () {
            vm.bLoading = false
            vm.bLoadError = true
        }
    )
})
