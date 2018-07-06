/**
 * Created by Siddharth on 20-03-2018.
 */
app.service('LeadService', function($q, $http, $localStorage, ObjLead, TemplateService, ObjTable2, ObjColumn) {
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
        vm.table = new ObjTable2(Constants.Table.TYPE_LEAD, vm.getTableColumns, vm.parseTableResponse)
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
            url:        '/api/manager/leads/getByIds',
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
                response.data.forEach(function (leadJson) {
                    vm.cache.push(ObjLead.fromJson(leadJson))
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
    vm.edit = function (leads){
        // Create deferred object
        var deferred = $q.defer()

        // Convert tasks to JSON
        var leadsJson = []
        leads.forEach(function (lead) {
            leadsJson.push(ObjLead.toJson(lead))
        })

        // Trigger request
        $http({
            method:     'POST',
            url:        '/api/manager/leads/edit',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            },
            data : {
                leads: leadsJson
            }
        }).then(
            // Success
            function (response) {
                // Resolve promise
                deferred.resolve()
            },
            // Error
            function (error) {
                // Reject promise
                deferred.reject(error.data.error)
            })

        return deferred.promise
    }

    // API to get task by ID
    vm.getById = function (id) {
        for (var i = 0; i < vm.cache.length; i++) {
            var lead = vm.cache[i]
            if (lead.id == id) {
                return lead
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
        columns.push(new ObjColumn(Table_C.ID_LEAD_NAME,        "Name",     Template_C.FIELD_TYPE_LEAD,     null, "lead"))
        columns.push(new ObjColumn(Table_C.ID_LEAD_ADDRESS,     "Address",  Template_C.FIELD_TYPE_TEXT,     null, "address"))
        columns.push(new ObjColumn(Table_C.ID_LEAD_LOCATION,    "Location", Template_C.FIELD_TYPE_LOCATION, null, "location"))
        columns.push(new ObjColumn(Table_C.ID_LEAD_TEMPLATE,    "Template", Template_C.FIELD_TYPE_TEMPLATE, null, "template"))

        // Iterate through each lead template
        TemplateService.getByType(Constants.Template.TYPE_LEAD).forEach(function (template) {
            // Iterate through each field
            template.fields.forEach(function (field, i) {
                // Add new column to array
                columns.push(new ObjColumn(columns.length, field.title, field.type, field.id, String(field.id)))
            })
        })

        return columns
    }

    // Method to parse lead sync response to tabular format
    vm.parseTableResponse = function (response) {
        // Parse response intorows for table
        var rows = []
        response.leads.forEach(function (json) {
            // Parse to Lead Object
            var lead = ObjLead.fromJson(json)

            // Add to rows
            rows.push(lead.toRow(vm.table))
        })

        return rows
    }

    // Init Lead Table
    vm.reset()
})
