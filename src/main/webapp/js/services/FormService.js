/**
 * Created by Siddharth on 31-05-2018.
 */

app.service('FormService', function($q, $http, $localStorage, ObjForm, TemplateService, ObjTable2, ObjColumn) {
    /* ----------------------------- INIT --------------------------------*/
    var vm = this

    vm.table = null

    // Sync Serialization related variables
    var canceller = null
    var bOngoing = false

    /* ----------------------------- APIs --------------------------------*/
    // Method to reset Service
    vm.reset = function () {
        // Init Lead Table
        vm.table = new ObjTable2(Constants.Table.TYPE_FORM, vm.getTableColumns, vm.parseTableResponse)
    }

    // API to get task data
    vm.sync = function (ids){
        if (bOngoing) {
            canceller.resolve()
            bOngoing = false
        }
        canceller = $q.defer()

        // Create deferred object
        var deferred = $q.defer()

        // Trigger request
        bOngoing = true
        $http({
            method:     'POST',
            url:        '/api/manager/forms/getByIds',
            timeout: canceller.promise,
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            },
            data : {
                ids: ids
            }
        }).then(
            // Success
            function (response) {
                // Reset ongoing flag
                bOngoing = false

                // Parse response into Form Object
                var forms = []
                response.data.forEach(function (formJson) {
                    forms.push(ObjForm.fromJson(formJson))
                })

                // Resolve promise
                deferred.resolve(forms)
            },
            // Error
            function (error) {
                // Ignore if request was cancelled
                if (error.status == -1) {
                    return
                }

                // Reset ongoing flag
                bOngoing = false

                // Resolve promise
                deferred.reject()
            })

        return deferred.promise
    }

    // Methods to get columns for different table types
    vm.getTableColumns = function () {
        var columns = []

        // Get constant reference for usign locally
        var Table_C = Constants.Table
        var Template_C = Constants.Template

        // Add mandatory columns for forms
        columns.push(new ObjColumn(Table_C.ID_FORM_REP,         "Representative", Template_C.FIELD_TYPE_REP,           null, "rep",         Constants.Template.TYPE_FORM, 1, true))
        columns.push(new ObjColumn(Table_C.ID_FORM_TEMPLATE,    "Template"      , Template_C.FIELD_TYPE_TEMPLATE,      null, "template",    Constants.Template.TYPE_FORM, 2, false))
        columns.push(new ObjColumn(Table_C.ID_FORM_DATE,        "Submit Date"   , Template_C.FIELD_TYPE_DATE,          null, "dateCreated", Constants.Template.TYPE_FORM, 3, true))
        columns.push(new ObjColumn(Table_C.ID_FORM_LOCATION,    "Location"      , Template_C.FIELD_TYPE_LOCATION,      null, "location",    Constants.Template.TYPE_FORM, 4, true))
        columns.push(new ObjColumn(Table_C.ID_FORM_DISTANCE,    "Distance"      , Template_C.FIELD_TYPE_NUMBER,        null, "distanceKm",  Constants.Template.TYPE_FORM, 5, true))
        columns.push(new ObjColumn(Table_C.ID_FORM_LEAD,        "Lead"          , Template_C.FIELD_TYPE_LEAD,          null, "lead",        Constants.Template.TYPE_FORM, 6, true))
        columns.push(new ObjColumn(Table_C.ID_FORM_TASK,        "Task"          , Template_C.FIELD_TYPE_TASK,          null, "task",        Constants.Template.TYPE_FORM, 7, true))
        columns.push(new ObjColumn(Table_C.ID_FORM_TASK_STATUS, "Task Status"   , Template_C.FIELD_TYPE_TEXT,          null, "taskStatus",  Constants.Template.TYPE_FORM, 8, true))

        // Iterate through each lead template
        TemplateService.getByType(Constants.Template.TYPE_FORM).forEach(function (template) {
            // Iterate through each field
            template.fields.forEach(function (field, i) {
                var bShow = i < 2

                // Add new column to array
                columns.push(new ObjColumn(field.id, field.title, field.type, field.id, String(field.id), Constants.Template.TYPE_FORM, columns.length + 1, bShow))
            })
        })

        return columns
    }

    // Method to parse lead sync response to tabular format
    vm.parseTableResponse = function (response) {
        // Parse response into rows for table
        var rows = []
        response.forms.forEach(function (json) {
            // Parse to Form Object
            var form = ObjForm.fromJson(json)

            // Add to rows
            rows.push(form.toRow(vm.table))
        })

        return rows
    }

    // Init Lead Table
    vm.reset()
})
