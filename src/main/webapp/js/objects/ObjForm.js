/**
 * Created by Siddharth on 31-05-2018.
 */

app.factory('ObjForm', function(TemplateService, ObjValue) {
    // ----------------------------------- Constructor ------------------------------------//
    function ObjForm (id, rep, lead, task, status, lat, lng, submitTime, distance, template, values) {
        this.id             = id

        // User data
        this.rep            = rep

        // Task related data
        this.lead           = lead
        this.task           = task
        this.status         = status

        // Tracking Data
        this.lat            = lat
        this.lng            = lng
        this.submitTime     = submitTime
        this.distance       = distance

        // Templated Data
        this.template       = template
        this.values         = values
    }

    // ----------------------------------- Public APIs ------------------------------------//
    // Method to clone a task object
    ObjForm.prototype.Clone = function () {
        // Create clone for lead and rep
        var rep = this.rep  ? {id: this.rep.id, name: this.rep.name} : null
        var lead = this.lead  ? {id: this.lead.id, name: this.lead.name} : null
        var task = this.task  ? {id: this.task.id, name: this.task.name} : null

        // Create clone for values
        var values = []
        this.values.forEach(function (value) {
            values.push(value.Clone())
        })

        // Return clone of task object
        return new ObjForm(this.id, rep, lead, task, this.status, this.lat, this.lng, this.submitTime, this.distance, this.template, values)
    }

    // Method to parse data into row
    ObjForm.prototype.toRow = function (table) {
        // Create new row object with blank value for each column
        var values = Statics.getArray(table.columns.length)
        values.fill('-')

        // Add id and name to row object
        var row = {id: this.id, name: this.rep.name, values: values}

        // Parse values form task to row
        row = this.parseRow(table, row)

        return row
    }

    ObjForm.prototype.parseRow = function (table, row) {
        // Add data in mandatory columns
        row.values[table.getColumnIdxById(Constants.Table.ID_FORM_REP)]             = this.rep.name
        row.values[table.getColumnIdxById(Constants.Table.ID_FORM_TEMPLATE)]        = this.template.name
        row.values[table.getColumnIdxById(Constants.Table.ID_FORM_DATE)]            = this.submitTime
        row.values[table.getColumnIdxById(Constants.Table.ID_FORM_LOCATION)]        = this.lat || this.lng ? this.lat + ',' + this.lng : '-'
        row.values[table.getColumnIdxById(Constants.Table.ID_FORM_DISTANCE)]        = this.distance
        row.values[table.getColumnIdxById(Constants.Table.ID_FORM_LEAD)]            = this.lead
        row.values[table.getColumnIdxById(Constants.Table.ID_FORM_TASK)]            = this.task
        row.values[table.getColumnIdxById(Constants.Table.ID_FORM_TASK_STATUS)]     = this.status

        // Iterate through template values
        this.values.forEach(function (value) {
            // Add string value at appropriate index in row
            row.values[table.getColumnIdxById(value.field.id)] = value.getDisplayString()
        })

        return row
    }

    // ----------------------------------- Validation APIs ------------------------------------//
    // ----------------------------------- Private APIs ------------------------------------//
    // ----------------------------------- Static APIs ------------------------------------//
    // Methods to convert between Frontend Field Object and JSON
    ObjForm.fromJson = function (json) {
        // Create values from JSON
        var values = []
        json.values.forEach(function (value) {
            values.push(ObjValue.fromJson(value))
        })

        return new ObjForm( json.id,
                            json.rep,
                            json.lead,
                            json.task,
                            json.status,
                            json.lat,
                            json.lng,
                            json.submitTime,
                            json.distance,
                            TemplateService.getById(json.templateId),
                            values)
    }

    return ObjForm
})
