/**
 * Created by Siddharth on 10-03-2018.
 */

app.factory('ObjField', function(ObjValue) {
    // ----------------------------------- Constructor ------------------------------------//
    function ObjField (id, title, type, value) {
        this.id = id
        this.type = type
        this.title = title
        this.value = value
    }

    // ----------------------------------- Public APIs ------------------------------------//
    // Method to clone a field object
    ObjField.prototype.Clone = function () {
        // Create clone for value
        var value = JSON.parse(JSON.stringify(this.value))

        // Return clone of field object
        return new ObjField(this.id, this.title, this.type, value)
    }

    // ----------------------------------- Private APIs ------------------------------------//
    // ----------------------------------- Static APIs ------------------------------------//
    // Methods to convert between Frontend Field Object and JSON
    ObjField.fromJson = function (json) {
        return new ObjField(json.id,
                            json.title,
                            json.type,
                            ObjValue.getValueFromString(json.value, json.type))
    }

    ObjField.toJson = function (field) {
        // Return field JSON
        return {
            id:     field.id,
            title:  field.title,
            type:   field.type,
            value:  ObjValue.getStringFromValue(field.value, field.type)
        }
    }

    return ObjField
})
