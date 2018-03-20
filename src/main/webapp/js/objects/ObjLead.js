/**
 * Created by Siddharth on 20-03-2018.
 */

app.factory('ObjLead', function(TemplateService, ObjValue) {
    // ----------------------------------- Constructor ------------------------------------//
    function ObjLead (id, name, address, lat, lng, template, values) {
        this.id             = id
        this.name           = name
        this.address        = address
        this.lat            = lat
        this.lng            = lng
        this.template       = template
        this.values         = values
    }

    // ----------------------------------- Public APIs ------------------------------------//
    // Method to clone a task object
    ObjLead.prototype.Clone = function () {
        // Create clone for values
        var values = []
        this.values.forEach(function (value) {
            values.push(value.Clone())
        })

        // Return clone of task object
        return new ObjLead(this.id, this.name, this.address, this.lat, this.lng, this.template, values)
    }

    // ----------------------------------- Private APIs ------------------------------------//
    // ----------------------------------- Static APIs ------------------------------------//
    // Methods to convert between Frontend Field Object and JSON
    ObjLead.fromJson = function (json) {
        // Create values from JSON
        var values = []
        json.values.forEach(function (value) {
            values.push(ObjValue.fromJson(value))
        })

        return new ObjLead(
            json.id,
            json.name,
            json.address,
            json.lat,
            json.lng,
            TemplateService.getById(json.templateId),
            values)
    }

    ObjLead.toJson = function (lead) {
        // Convert values to JSON
        var valuesJson = []
        lead.values.forEach(function (value) {
            valuesJson.push(ObjValue.toJson(value))
        })

        // Return field JSON
        return {
            id:             lead.id,
            name:           lead.name,
            address:        lead.address,
            lat:            lead.lat,
            lng:            lead.lng,
            templateId:     lead.template.id,
            values:         valuesJson
        }
    }

    return ObjLead
})
