/**
 * Created by Siddharth on 25-02-2018.
 *
 * Service to cache table data
 */

app.service('TableService', function(ObjTable) {
    /* ----------------------------- INIT --------------------------------*/
    var vm  = this

    // Set currently active table to null on init
    vm.activeTable = null

    // Init tables for different types
    vm.taskTable = new ObjTable(Constants.Table.TYPE_TASK)
    vm.formTable = new ObjTable(Constants.Table.TYPE_FORM)
    vm.teamTable = new ObjTable(Constants.Table.TYPE_TEAM)

    /* ----------------------------- Public APIs --------------------------------*/
    // Method to reset Service
    vm.reset = function () {
        // Reset active table
        vm.activeTable = null

        // Reset cache
        vm.taskTable = new ObjTable(Constants.Table.TYPE_TASK)
        vm.formTable = new ObjTable(Constants.Table.TYPE_FORM)
        vm.teamTable = new ObjTable(Constants.Table.TYPE_TEAM)
    }
    /* ----------------------------- Private APIs --------------------------------*/
})
