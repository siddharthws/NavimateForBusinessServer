/**
 * Created by Siddharth on 19-11-2017.
 */

// Controller for Toggle Columns Dialog
app.controller('ToggleColumnsCtrl', function ($scope, $mdDialog, columns) {
    /* ------------------------------- INIT -----------------------------------*/
    var vm = this

    // Columns & groups
    vm.columns = columns
    vm.groups = []

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
    vm.add = function(column) {
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

    vm.minus = function(column) {
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

    /* ------------------------------- Private APIs -----------------------------------*/
    function initGroups() {
        // Collect Objects IDs from columns
        var objIds = []
        columns.forEach(function (column) {
            if (!objIds.contains(column.objectId)) {
                objIds.push(column.objectId)
            }
        })

        // Divide columns into groups
        vm.groups = []
        objIds.forEach(function (objId) {
            // Create group
            var group = {
                name: Constants.Template.OBJ_NAMES[objId],
                columns: []
            }

            // Add columns to group
            columns.forEach(function (column) {
                if (column.objectId == objId) {
                    group.columns.push(column)
                }
            })

            // Add to groups
            vm.groups.push(group)
        })
    }

    /* ------------------------------- INIT -----------------------------------*/
    initGroups()
})
