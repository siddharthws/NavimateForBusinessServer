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
    // Method to get a string value
    ObjValue.prototype.getString = function () {
        // Parse string for specific types
        switch (this.field.type) {
            case Constants.Template.FIELD_TYPE_RADIOLIST:
                return this.value.options[this.value.selection]
            case Constants.Template.FIELD_TYPE_CHECKLIST:
                var valueString = ""
                this.value.forEach(function (option) {
                    if (option.selection) {
                        if (!valueString) {
                            valueString += option.name
                        } else {
                            valueString += ', ' + option.name
                        }
                    }
                })
                return valueString
        }

        // Return stored value by default
        return this.value
    }

    // Method to validate the value
    ObjValue.prototype.getErr = function () {
        var err = ""

        switch (this.field.type) {
            case Constants.Template.FIELD_TYPE_TEXT:
            case Constants.Template.FIELD_TYPE_CHECKBOX:
            case Constants.Template.FIELD_TYPE_RADIOLIST:
            case Constants.Template.FIELD_TYPE_CHECKLIST:
            case Constants.Template.FIELD_TYPE_PHOTO:
            case Constants.Template.FIELD_TYPE_SIGN:
            case Constants.Template.FIELD_TYPE_LOCATION:
            case Constants.Template.FIELD_TYPE_DATE:
                // No Validation
                break
            case Constants.Template.FIELD_TYPE_NUMBER:
                if (!Statics.validateNumber(this.value)) {
                    err = 'Cannot be empty'
                }
                break
            default:
                err = "Invalid Field Type"
        }

        return err
    }

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
