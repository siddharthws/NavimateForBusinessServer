/**
 * Created by Siddharth on 20-03-2018.
 */
app.service('LeadService', function($q, $http, $localStorage, ObjLead, TemplateService, ObjTable2, ObjColumn) {
    /* ----------------------------- INIT --------------------------------*/
    var vm = this
    vm.cache = []

    // Sync Serialization related variables
    var canceller = null
    var bOngoing = false

    /* ----------------------------- APIs --------------------------------*/
    // Method to reset Service
    vm.reset = function () {
        // Reset cache
        vm.cache = []

        // Init Lead Table
        vm.table = new ObjTable2(Constants.Table.TYPE_LEAD, vm.getColumnsCb, vm.getRowsCb, vm.getFilterCb, vm.getSorterCb, vm.getExportFilterCb)
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
            url:        '/api/admin/leads/edit',
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

    vm.getColumnsCb = function () {
        var columns = []

        // Get constant reference for usign locally
        var Table_C = Constants.Table
        var Template_C = Constants.Template

        // Add mandatory columns
        columns.push(new ObjColumn(Table_C.ID_LEAD_NAME,        "Name",     Template_C.FIELD_TYPE_LEAD,     "name", null))
        columns.push(new ObjColumn(Table_C.ID_LEAD_ADDRESS,     "Address",  Template_C.FIELD_TYPE_TEXT,     "address", null))
        columns.push(new ObjColumn(Table_C.ID_LEAD_LOCATION,    "Location", Template_C.FIELD_TYPE_LOCATION, "location", null))
        columns.push(new ObjColumn(Table_C.ID_LEAD_TEMPLATE,    "Template", Template_C.FIELD_TYPE_TEXT,     "template", null))

        // Iterate through each lead template
        TemplateService.getByType(Constants.Template.TYPE_LEAD).forEach(function (template) {
            // Iterate through each field
            template.fields.forEach(function (field, i) {
                // Add new column to array
                columns.push(new ObjColumn(columns.length, field.title, field.type, field.id))
            })
        })

        return columns
    }

    vm.getFilterCb = function () {
        // Prepare column filters for server
        var colFilters = {}

        // Add filters for mandatory columns
        colFilters.name     = vm.table.getColumnById(Constants.Table.ID_LEAD_NAME).filter.toJson()
        colFilters.address  = vm.table.getColumnById(Constants.Table.ID_LEAD_ADDRESS).filter.toJson()
        colFilters.location = vm.table.getColumnById(Constants.Table.ID_LEAD_LOCATION).filter.toJson()
        colFilters.template = vm.table.getColumnById(Constants.Table.ID_LEAD_TEMPLATE).filter.toJson()

        // Prepare filter for templated fields
        TemplateService.getByType(Constants.Template.TYPE_LEAD).forEach(function (template) {
            template.fields.forEach(function (field) {
                // Get column for this field
                var column = getColumnForField(vm.table.columns, field)

                // Add column filter for server
                colFilters[String(field.id)] = column.filter.toJson()
            })
        })

        // Return filter object
        return colFilters
    }

    vm.getRowsCb = function (response) {
        // Parse response into array of leads
        var leads = []
        response.leads.forEach(function (json) {
            leads.push(ObjLead.fromJson(json))
        })

        // Parse leads into rows
        var rows = []
        leads.forEach(function (lead) {
            // Create new row object with blank value for each column
            var values = Statics.getArray(vm.table.columns.length)
            values.fill('-')

            var row = {id: lead.id, name: lead.name, values: values}

            // Add data in mandatory columns
            row.values[Constants.Table.ID_LEAD_NAME]       = lead.name
            row.values[Constants.Table.ID_LEAD_ADDRESS]    = lead.address
            row.values[Constants.Table.ID_LEAD_LOCATION]   = lead.lat && lead.lng ? lead.lat + ',' + lead.lng : '-'
            row.values[Constants.Table.ID_LEAD_TEMPLATE]   = lead.template.name

            // Iterate through template values in lead
            lead.values.forEach(function (value) {
                // Get column for field
                var column = getColumnForField(vm.table.columns, value.field)

                // Add string value at appropriate index in row
                row.values[column.id] = value.getDisplayString()
            })

            // Add to rows
            rows.push(row)
        })

        return rows
    }


    vm.getSorterCb = function() {
        var sorter = []
        vm.table.sortOrder.forEach(function (colId) {
            // Get column
            var column = vm.table.getColumnById(colId)

            // Get filter name based on column ID
            switch (column.id) {
                case Constants.Table.ID_LEAD_NAME:
                    sorter.push({name: column.filter.sort})
                    break
                case Constants.Table.ID_LEAD_ADDRESS:
                    sorter.push({address: column.filter.sort})
                    break
                case Constants.Table.ID_LEAD_LOCATION:
                    sorter.push({location: column.filter.sort})
                    break
                case Constants.Table.ID_LEAD_TEMPLATE:
                    sorter.push({template: column.filter.sort})
                    break
                default:
                    var sortObj = {}
                    sortObj[String(column.fieldId)] = column.filter.sort
                    sorter.push(sortObj)
                    break
            }
        })
        return sorter
    }

    vm.getExportFilterCb = function () {
        var params = {
            columns: [],
            selection: vm.table.getSelectedIds()
        }

        // Iterate through columns in current order
        vm.table.columns.forEach(function (column) {
            if (column.filter.bShow) {
                // Column names for hardcoded columns
                if (column.id == Constants.Table.ID_LEAD_NAME)      {params.columns.push({field: "name", label: "Name", type: column.type})}
                else if (column.id == Constants.Table.ID_LEAD_ADDRESS)   {params.columns.push({field: "address", label: "Address", type: column.type})}
                else if (column.id == Constants.Table.ID_LEAD_LOCATION)  {params.columns.push({field: "location", label: "Location", type: column.type})}
                else if (column.id == Constants.Table.ID_LEAD_TEMPLATE)  {params.columns.push({field: "template", label: "Template", type: column.type})}
                // Column names for templated columns
                else if (column.fieldId) {
                    params.columns.push({field: column.fieldId, label: column.name, type: column.type})
                }
            }
        })

        return params
    }

    function getColumnForField(columns, field) {
        // Find column which matches field title and type
        var colIdx = -1
        for (var i = 0; i < columns.length; i++) {
            if (columns[i].fieldId == field.id) {
                colIdx = i
                break
            }
        }

        return columns[colIdx]
    }

    // Init Lead Table
    vm.table = new ObjTable2(Constants.Table.TYPE_LEAD, vm.getColumnsCb, vm.getRowsCb, vm.getFilterCb, vm.getSorterCb, vm.getExportFilterCb)
})
