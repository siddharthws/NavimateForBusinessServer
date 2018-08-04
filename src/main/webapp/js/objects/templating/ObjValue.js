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
    ObjValue.prototype.getDisplayString = function () {
        // Parse string for specific types
        switch (this.field.type) {
            case Constants.Template.FIELD_TYPE_DATE:
                if (this.value && this.value.length) {
                    return moment(this.value, Constants.Date.FORMAT_LONG).format(Constants.Date.FORMAT_DESCRIPTIVE)
                }
                break
            case Constants.Template.FIELD_TYPE_RADIOLIST:
                if (this.value) {
                    return this.value.options[this.value.selection]
                }
                break
            case Constants.Template.FIELD_TYPE_PRODUCT:
                if (this.value && this.value.id) {
                    return this.value
                } else {
                    return '-'
                }
                break
            case Constants.Template.FIELD_TYPE_CHECKLIST:
                if (this.value) {
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
                break
        }

        // Return stored value by default
        return this.value
    }

    // Method to validate the value
    ObjValue.prototype.getErr = function () {
        var err = ""

        switch (this.field.type) {
            case Constants.Template.FIELD_TYPE_TEXT:
            case Constants.Template.FIELD_TYPE_PHOTO:
            case Constants.Template.FIELD_TYPE_FILE:
            case Constants.Template.FIELD_TYPE_SIGN:
            case Constants.Template.FIELD_TYPE_LOCATION:
            case Constants.Template.FIELD_TYPE_DATE:
            case Constants.Template.FIELD_TYPE_PRODUCT:
                if(this.field.settings.bMandatory){
                    if(!this.value){
                        err = 'Field is mandatory'
                    }
                }
                break
            case Constants.Template.FIELD_TYPE_NUMBER:
                if (!Statics.validateNumber(this.value)) {
                    err = 'Cannot be empty'
                }
                break
            case Constants.Template.FIELD_TYPE_RADIOLIST:
            case Constants.Template.FIELD_TYPE_CHECKLIST:
            case Constants.Template.FIELD_TYPE_CHECKBOX:
                break
            default:
                err = "Invalid Field Type"
        }

        return err
    }

    ObjValue.prototype.Clone = function () {
        return ObjValue.fromJson(ObjValue.toJson(this))
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
