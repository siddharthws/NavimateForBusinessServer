/**
 * Created by Siddharth on 12-03-2018.
 */

app.factory('ObjTask', function(TemplateService, ObjValue) {
    // ----------------------------------- Constructor ------------------------------------//
    function ObjTask (id, publicId, lead, manager, rep, creator, status, period, dateCreated, resolutionTime, formTemplate, template, values) {
        this.id             = id
        this.publicId       = publicId
        this.lead           = lead
        this.manager        = manager
        this.rep            = rep
        this.creator        = creator
        this.status         = status
        this.period         = period
        this.dateCreated    = dateCreated
        this.resolutionTime = resolutionTime
        this.formTemplate   = formTemplate
        this.template       = template
        this.values         = values
    }

    // ----------------------------------- Public APIs ------------------------------------//
    // Method to clone a task object
    ObjTask.prototype.Clone = function () {
        // Create clone for lead and rep
        var lead = this.lead  ? {id: this.lead.id, name: this.lead.name, lat: this.lead.lat, lng: this.lead.lng} : null
        var manager = this.manager  ? {id: this.manager.id, name: this.manager.name} : null
        var rep = this.rep  ? {id: this.rep.id, name: this.rep.name} : null
        var creator = this.creator  ? {id: this.creator.id, name: this.creator.name} : null

        // Create clone for values
        var values = []
        this.values.forEach(function (value) {
            values.push(value.Clone())
        })

        // Return clone of task object
        return new ObjTask(this.id, this.publicId, lead, manager, rep, creator, this.status, this.period, this.dateCreated, this.resolutionTime, this.formTemplate, this.template, values)
    }

    // Method to parse data into row
    ObjTask.prototype.toRow = function (table) {
        // Create new row object with blank value for each column
        var values = Statics.getArray(table.columns.length)
        values.fill('-')

        // Add id and name to row object
        var row = {id: this.id, name: this.publicId, values: values}

        // Parse values form task to row
        row = this.parseRow(table, row)

        return row
    }

    ObjTask.prototype.parseRow = function (table, row) {
        // Add data in mandatory columns
        row.values[table.getColumnIdxById(Constants.Table.ID_TASK_ID)]              = {id: this.id, name: this.publicId}
        row.values[table.getColumnIdxById(Constants.Table.ID_TASK_LEAD)]            = this.lead
        row.values[table.getColumnIdxById(Constants.Table.ID_TASK_LOCATION)]        = this.lead.lat || this.lead.lng ? this.lead.lat + ',' + this.lead.lng : '-'
        row.values[table.getColumnIdxById(Constants.Table.ID_TASK_MANAGER)]         = this.manager.name
        row.values[table.getColumnIdxById(Constants.Table.ID_TASK_REP)]             = this.rep ? this.rep.name : '-'
        row.values[table.getColumnIdxById(Constants.Table.ID_TASK_CREATOR)]         = this.creator.name
        row.values[table.getColumnIdxById(Constants.Table.ID_TASK_DATE_CREATED)]    = this.dateCreated
        row.values[table.getColumnIdxById(Constants.Table.ID_TASK_FORM_TEMPLATE)]   = this.formTemplate.name
        row.values[table.getColumnIdxById(Constants.Table.ID_TASK_TEMPLATE)]        = this.template.name
        row.values[table.getColumnIdxById(Constants.Table.ID_TASK_STATUS)]          = Constants.Task.STATUS_NAME[this.status]
        row.values[table.getColumnIdxById(Constants.Table.ID_TASK_RESOLUTION_TIME)] = this.resolutionTime

        // Iterate through template values
        this.values.forEach(function (value) {
            // Add string value at appropriate index in row
            row.values[table.getColumnIdxById(value.field.id)] = value.getDisplayString()
        })

        return row
    }

    // ----------------------------------- Validation APIs ------------------------------------//
    ObjTask.prototype.isValid = function () {
        if (this.getPublicIdErr().length) {
            return false
        }
        if (this.getLeadErr().length) {
            return false
        }
        if (this.getPeriodErr().length) {
            return false
        }
        if (this.getFormErr().length) {
            return false
        }
        if (this.getTemplateErr().length) {
            return false
        }
        for (var i = 0; i < this.values.length; i++) {
            if (this.values[i].getErr().length > 0) {
                return false
            }
        }
        return true
    }

    ObjTask.prototype.getPublicIdErr = function () {
        if (!this.publicId) {
            return 'ID is mandatory'
        }

        return ''
    }

    ObjTask.prototype.getLeadErr = function () {
        if (!this.lead) {
            return 'Select a lead'
        }

        return ''
    }

    ObjTask.prototype.getPeriodErr = function () {
        if (!Statics.validateNumber(this.period)) {
            return 'Cannot be empty'
        }

        return ''
    }

    ObjTask.prototype.getFormErr = function () {
        if (!this.formTemplate) {
            return 'Select a form'
        }

        return ''
    }

    ObjTask.prototype.getTemplateErr = function () {
        if (!this.template) {
            return 'Select a template'
        }

        return ''
    }

    // ----------------------------------- Private APIs ------------------------------------//
    // ----------------------------------- Static APIs ------------------------------------//
    // Methods to convert between Frontend Field Object and JSON
    ObjTask.fromJson = function (json) {
        // Create values from JSON
        var values = []
        json.values.forEach(function (value) {
            values.push(ObjValue.fromJson(value))
        })

        return new ObjTask( json.id,
                            json.publicId,
                            json.lead,
                            json.manager,
                            json.rep,
                            json.creator,
                            json.status,
                            json.period,
                            json.dateCreated,
                            json.resolutionTime,
                            TemplateService.getById(json.formTemplateId),
                            TemplateService.getById(json.templateId),
                            values)
    }

    ObjTask.toJson = function (task) {
        // Convert values to JSON
        var valuesJson = []
        task.values.forEach(function (value) {
            valuesJson.push(ObjValue.toJson(value))
        })

        // Return field JSON
        return {
            id:             task.id,
            publicId:       task.publicId,
            leadId:         task.lead.id,
            managerId:      task.manager ? task.manager.id : 0,
            repId:          task.rep ? task.rep.id : null,
            status:         task.status,
            period:         task.period,
            formTemplateId: task.formTemplate.id,
            templateId:     task.template.id,
            values:         valuesJson
        }
    }

    return ObjTask
})
