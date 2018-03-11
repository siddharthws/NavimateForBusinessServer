/**
 * Created by Siddharth on 11-03-2018.
 */

app.factory('ObjValue', function() {
    // ----------------------------------- Constructor ------------------------------------//
    function ObjValue (value, field) {
        this.value = value
        this.field = field
    }

    // ----------------------------------- Public APIs ------------------------------------//
    // ----------------------------------- Private APIs ------------------------------------//
    // ----------------------------------- Static APIs ------------------------------------//
    // Method to convert JSON to Template Object
    ObjValue.getValueFromString = function (valueString, fieldType) {
        var value = valueString

        // Parse string for specific types
        switch (fieldType) {
            case Constants.Template.FIELD_TYPE_RADIOLIST:
            case Constants.Template.FIELD_TYPE_CHECKLIST:
                if (value.length) {
                    value = JSON.parse(value)
                }
                break
            case Constants.Template.FIELD_TYPE_CHECKBOX:
                value = (value == 'true')
                break
        }

        // Return Value object
        return value
    }

    ObjValue.getStringFromValue = function (value, fieldType) {
        var valueString = value

        // Parse string for specific types
        switch (fieldType) {
            case Constants.Template.FIELD_TYPE_RADIOLIST:
            case Constants.Template.FIELD_TYPE_CHECKLIST:
                if (value.length) {
                    valueString = JSON.stringify(value)
                }
                break
            case Constants.Template.FIELD_TYPE_CHECKBOX:
                valueString = value ? 'true' : 'false'
                break
        }

        // Return Value object
        return valueString
    }

    return ObjValue
})
