/**
 * Created by Siddharth on 12-03-2018.
 */

app.factory('ObjTask', function(TemplateService, ObjValue) {
    // ----------------------------------- Constructor ------------------------------------//
    function ObjTask (id, lead, rep, status, period, formTemplate, template, values) {
        this.id             = id
        this.lead           = lead
        this.rep            = rep
        this.status         = status
        this.period         = period
        this.formTemplate   = formTemplate
        this.template       = template
        this.values         = values
    }

    // ----------------------------------- Public APIs ------------------------------------//
    // Method to clone a task object
    ObjTask.prototype.Clone = function () {
        // Create clone for lead and rep
        var lead = this.lead  ? {id: this.lead.id, name: this.lead.name} : null
        var rep = this.rep  ? {id: this.rep.id, name: this.rep.name} : null

        // Create clone for values
        var values = []
        this.values.forEach(function (value) {
            values.push(value.Clone())
        })

        // Return clone of task object
        return new ObjTask(this.id, lead, rep, this.status, this.period, this.formTemplate, this.template, values)
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
                            json.lead,
                            json.rep,
                            json.status,
                            json.period,
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
            leadId:         task.lead.id,
            repId:          task.rep.id,
            status:         task.status,
            period:         task.period,
            formTemplateId: task.formTemplate.id,
            templateId:     task.template.id,
            values:         valuesJson
        }
    }

    return ObjTask
})
