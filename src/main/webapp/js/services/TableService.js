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
    vm.leadTable = new ObjTable(Constants.Table.TYPE_LEAD)
    vm.taskTable = new ObjTable(Constants.Table.TYPE_TASK)
    vm.formTable = new ObjTable(Constants.Table.TYPE_FORM)

    /* ----------------------------- Public APIs --------------------------------*/
    /* ----------------------------- Private APIs --------------------------------*/
})
