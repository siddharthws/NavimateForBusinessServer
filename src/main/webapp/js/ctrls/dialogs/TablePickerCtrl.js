/**
 * Created by Siddharth on 19-03-2018.
 */

// Controller for picking objects using table
app.controller('TablePickerCtrl', function ($scope, $mdDialog,
                                            TableService, ToastService,
                                            title, table, cb) {
    /* ------------------------------- INIT -----------------------------------*/
    var vm = this

    // Set active table
    table.selectedRows = []
    TableService.activeTable = table
    vm.table = table

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
        if (!vm.table.selectedRows.length) {
            ToastService.toast("Select at least 1 row !!!")
            return
        }

        // Get selected object id
        var row = vm.table.selectedRows[0]

        // Trigger callback
        cb(row)

        // Close this dialog
        vm.close()
    }

    // Method to close this dialog
    vm.close = function () {
        // Reset selected rows
        vm.table.selectedRows = []

        // Hide dialog
        $mdDialog.hide()
    }

    /* ------------------------------- Private APIs -----------------------------------*/
    /* ------------------------------- Post INIT -----------------------------------*/
})
