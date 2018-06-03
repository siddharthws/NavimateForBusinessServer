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