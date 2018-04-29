/**
 * Created by Siddharth on 19-11-2017.
 */

// Controller for Toggle Columns Dialog
app.controller('ToggleColumnsCtrl', function ($scope, $mdDialog, columns) {
    /* ------------------------------- INIT -----------------------------------*/
    var vm = this

    // Init columns using arguments
    vm.columns = columns

    /* ------------------------------- Html APIs -----------------------------------*/
    vm.done = function () {
        $mdDialog.hide()
    }

    // Full List Selection Toggling
    vm.toggleAll = function () {
        for (var i = 0; i < vm.columns.length; i++) {
            vm.columns[i].filter.bShow = vm.bCheckAll
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
})
