/**
 * Created by Siddharth on 10-03-2018.
 */

app.factory('ObjTemplate', function(ObjField) {
    // ----------------------------------- Constructor ------------------------------------//
    function ObjTemplate (id, name, type, fields) {
        this.id = id
        this.type = type
        this.name = name
        this.fields = fields
    }

    // ----------------------------------- Public APIs ------------------------------------//
    // Method to clone a template object
    ObjTemplate.prototype.Clone = function () {
        // Create clones for every field object
        var fields = []
        this.fields.forEach(function (field) {
            fields.push(field.Clone())
        })

        // Return clone of template object
        return new ObjTemplate(this.id, this.name, this.type, fields)
    }

    // ----------------------------------- Private APIs ------------------------------------//
    // ----------------------------------- Static APIs ------------------------------------//
    // Methods to convert between Frontend Template Object and JSON
    ObjTemplate.fromJson = function (json) {
        // Parse field objects
        var fields = []
        json.fields.forEach(function (fieldJson) {
            fields.push(ObjField.fromJson(fieldJson))
        })

        // Return template object
        return new ObjTemplate(json.id, json.name, json.type, fields)
    }

    ObjTemplate.toJson = function (template) {
        // Prepare field JSON array
        var fieldsJson = []
        template.fields.forEach(function (field) {
            fieldsJson.push(ObjField.toJson(field))
        })

        // Return template JSON
        return {
            id:     template.id,
            name:   template.name,
            type:   template.type,
            fields: fieldsJson,
        }
    }

    return ObjTemplate
})
