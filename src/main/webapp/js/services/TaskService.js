/**
 * Created by aroha on 18-01-2018.
 */
app.service('TaskService', function($q, $http, $localStorage, ObjTask, TemplateService, ObjTable2, ObjColumn) {
    /* ----------------------------- INIT --------------------------------*/
    var vm = this

    vm.cache = null
    vm.table = null

    // Sync Serialization related variables
    var canceller = null
    var bOngoing = false

    /* ----------------------------- APIs --------------------------------*/
    // Method to reset Service
    vm.reset = function () {
        // Reset cache
        vm.cache = []

        // Init Lead Table
        vm.table = new ObjTable2(Constants.Table.TYPE_TASK, vm.getTableColumns, vm.parseTableResponse)
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
            url:        '/api/manager/tasks/getByIds',
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

                // Update cache
                vm.cache = []
                response.data.forEach(function (taskJson) {
                    vm.cache.push(ObjTask.fromJson(taskJson))
                })

                // Resolve promise
                deferred.resolve()
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

    // API to edit task data
    vm.edit = function (tasks){
        // Create deferred object
        var deferred = $q.defer()

        // Convert tasks to JSON
        var tasksJson = []
        tasks.forEach(function (task) {
            tasksJson.push(ObjTask.toJson(task))
        })

        // Trigger request
        $http({
            method:     'POST',
            url:        '/api/manager/tasks/edit',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            },
            data : {
                tasks: tasksJson
            }
        }).then(
            // Success
            function (response) {
                // Resolve promise
                deferred.resolve()
            },
            // Error
            function (error) {
                // Resolve promise
                deferred.reject(error.data.error)
            })

        return deferred.promise
    }

    // API to get task by ID
    vm.getById = function (id) {
        for (var i = 0; i < vm.cache.length; i++) {
            var task = vm.cache[i]
            if (task.id == id) {
                return task
            }
        }

        return null
    }

    // Methods to get columns for different table types
    vm.getTableColumns = function () {
        var columns = []

        // Get constant reference for usign locally
        var Table_C = Constants.Table
        var Template_C = Constants.Template

        // Add mandatory columns
        columns.push(new ObjColumn(Table_C.ID_TASK_ID,             "ID",                    Template_C.FIELD_TYPE_TASK,     null, "publicId",           Constants.Template.TYPE_TASK))
        columns.push(new ObjColumn(Table_C.ID_TASK_LEAD,           "Lead",                  Template_C.FIELD_TYPE_LEAD,     null, "lead",               Constants.Template.TYPE_TASK))
        columns.push(new ObjColumn(Table_C.ID_TASK_LOCATION,       "Location",              Template_C.FIELD_TYPE_LOCATION, null, "location",           Constants.Template.TYPE_TASK))
        columns.push(new ObjColumn(Table_C.ID_TASK_MANAGER,        "Manager",               Template_C.FIELD_TYPE_NON_REP,  null, "manager",            Constants.Template.TYPE_TASK))
        columns.push(new ObjColumn(Table_C.ID_TASK_REP,            "Rep",                   Template_C.FIELD_TYPE_REP,      null, "rep",                Constants.Template.TYPE_TASK))
        columns.push(new ObjColumn(Table_C.ID_TASK_CREATOR,        "Created By",            Template_C.FIELD_TYPE_NON_REP,  null, "creator",            Constants.Template.TYPE_TASK))
        columns.push(new ObjColumn(Table_C.ID_TASK_DATE_CREATED,   "Create Date",           Template_C.FIELD_TYPE_DATE,     null, "dateCreated",        Constants.Template.TYPE_TASK))
        columns.push(new ObjColumn(Table_C.ID_TASK_FORM_TEMPLATE,  "Form",                  Template_C.FIELD_TYPE_TEMPLATE, null, "formTemplate",       Constants.Template.TYPE_TASK))
        columns.push(new ObjColumn(Table_C.ID_TASK_TEMPLATE,       "Template",              Template_C.FIELD_TYPE_TEMPLATE, null, "template",           Constants.Template.TYPE_TASK))
        columns.push(new ObjColumn(Table_C.ID_TASK_STATUS,         "Status",                Template_C.FIELD_TYPE_TEXT,     null, "status",             Constants.Template.TYPE_TASK))
        columns.push(new ObjColumn(Table_C.ID_TASK_RESOLUTION_TIME,"Resolved In (Hrs)",     Template_C.FIELD_TYPE_NUMBER,   null, "resolutionTimeHrs",  Constants.Template.TYPE_TASK))

        // Iterate through each lead template
        TemplateService.getByType(Constants.Template.TYPE_TASK).forEach(function (template) {
            // Iterate through each field
            template.fields.forEach(function (field, i) {
                // Add new column to array
                columns.push(new ObjColumn(field.id, field.title, field.type, field.id, String(field.id), Constants.Template.TYPE_TASK))
            })
        })

        return columns
    }

    // Method to parse lead sync response to tabular format
    vm.parseTableResponse = function (response) {
        // Parse response into rows for table
        var rows = []
        response.tasks.forEach(function (json) {
            // Parse to Task Object
            var task = ObjTask.fromJson(json)

            // Add to rows
            rows.push(task.toRow(vm.table))
        })

        return rows
    }

    // Init Lead Table
    vm.reset()
})