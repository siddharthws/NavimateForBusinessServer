/**
 * Created by Siddharth on 11-03-2018.
 */

app.factory('ObjValue', function(TemplateService) {
    // ----------------------------------- Constructor ------------------------------------//
    function ObjValue (value, field) {
        this.value = value
        this.field = field
    }

    // ----------------------------------- Public APIs ------------------------------------//
    // ----------------------------------- Private APIs ------------------------------------//
    // ----------------------------------- Static APIs ------------------------------------//
    // Methods to convert between Frontend Field Object and JSON
    ObjValue.fromJson = function (json) {
        // Get field
        var field = TemplateService.getFieldById(json.fieldId)

        // Get value
        var value = Statics.getValueFromString(json.value, field.type)

        // Return value Object
        return new ObjValue(value, field)
    }

    ObjValue.toJson = function (value) {
        return {
            value:    Statics.getStringFromValue(value.value, value.field.type),
            fieldId:  value.field.id
        }
    }

    return ObjValue
})
