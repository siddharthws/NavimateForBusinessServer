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
    vm.add = function(index) {
        // Get column
        var column = vm.columns[index]

        // Find column with next position
        var nextColumn = null
        for (var i = 0; i < vm.columns.length; i++) {
            if (vm.columns[i].position == column.position + 1) {
                nextColumn = vm.columns[i]
                break
            }
        }

        // Update position
        column.position = column.position + 1
        nextColumn.position = nextColumn.position - 1
    }

    vm.minus = function(index) {
        // Get column
        var column = vm.columns[index]

        // Find column with next position
        var prevColumn = null
        for (var i = 0; i < vm.columns.length; i++) {
            if (vm.columns[i].position == column.position - 1) {
                prevColumn = vm.columns[i]
                break
            }
        }

        // Update position
        column.position = column.position - 1
        prevColumn.position = prevColumn.position + 1
    }

    /* ------------------------------- Local APIs -----------------------------------*/
})
