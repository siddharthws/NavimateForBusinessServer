/**
 * Created by Siddharth on 29-04-2018.
 */

// Controller for picking objects using table
app.controller('Table2PickerCtrl', function ($scope, $mdDialog,
                                            TableService, ToastService,
                                            title, table, cb) {
    /* ------------------------------- INIT -----------------------------------*/
    var vm = this

    TableService.activeTable = table
    vm.table = table

    // Reset Active rows
    table.selection = []

    // Attach Table properties to scope
    $scope.tableProps = {
        bSingleSelect: true
    }

    // Set dialog title
    vm.title = title

    /* ------------------------------- Public APIs -----------------------------------*/
    // Method to pick an object
    vm.pick = function () {
        // Show error if no object is selected
        if (!vm.table.selection.length) {
            ToastService.toast("Select at least 1 row !!!")
            return
        }

        // Get selected object id
        var row = vm.table.selection[0]

        // Trigger callback
        cb(row.id, row.name)

        // Close this dialog
        vm.close()
    }

    // Method to close this dialog
    vm.close = function () {
        // Hide dialog
        $mdDialog.hide()
    }

    /* ------------------------------- Private APIs -----------------------------------*/
    /* ------------------------------- Post INIT -----------------------------------*/
})
