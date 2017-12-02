/**
 * Created by Siddharth on 19-11-2017.
 */

// Controller for Toggle Columns Dialog
app.controller('ToggleColumnsCtrl', function ($scope, $mdDialog, columns, resultCb) {
    var vm = this

    /* ------------------------------- Html APIs -----------------------------------*/
    vm.done = function () {
        resultCb(vm.columns)
        $mdDialog.hide()
    }

    // Full List Selection Toggling
    vm.toggleAll = function () {
        for (var i = 0; i < vm.columns.length; i++) {
            vm.columns[i].show = vm.bCheckAll
        }
    }

    // Column Re-ordering APIs
    vm.moveUp = function(index) {
        if (index > 0) {
            vm.columns.move(index, index - 1)
        }
    }

    vm.moveDown = function(index) {
        if (index < (vm.columns.length - 1)) {
            vm.columns.move(index, index + 1)
        }
    }

    /* ------------------------------- Local APIs -----------------------------------*/
    /* ------------------------------- INIT -----------------------------------*/
    // Init objects
    vm.columns = columns
})
