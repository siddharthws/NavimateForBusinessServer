/**
 * Created by Siddharth on 10-03-2018.
 */

app.factory('ObjField', function(ObjFieldSettings) {
    // ----------------------------------- Constructor ------------------------------------//
    function ObjField (id, title, type, value) {
        this.id = id
        this.type = type
        this.title = title
        this.value = value
        this.settings = new ObjFieldSettings(false)
    }

    // ----------------------------------- Public APIs ------------------------------------//
    // Method to clone a field object
    ObjField.prototype.Clone = function () {
        // Create clone for value
        var value = JSON.parse(JSON.stringify(this.value))
        // Return clone of field object
        var field = new ObjField(this.id, this.title, this.type, value)
        field.settings =  this.settings.Clone()

        return field
    }

    // ----------------------------------- Private APIs ------------------------------------//
    // ----------------------------------- Static APIs ------------------------------------//
    // Methods to convert between Frontend Field Object and JSON
    ObjField.fromJson = function (json) {

        var field =  new ObjField(json.id,
                            json.title,
                            json.type,
                            Statics.getValueFromString(json.value, json.type))

        field.settings = ObjFieldSettings.fromJson(json.settings)

        return field
    }

    ObjField.toJson = function (field) {
        // Return field JSON
        return {
            id:       field.id,
            title:    field.title,
            type:     field.type,
            value:    Statics.getStringFromValue(field.value, field.type),
            settings: ObjFieldSettings.toJson(field.settings)
    }
    }

    return ObjField
})
