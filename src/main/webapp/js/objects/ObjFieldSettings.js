/**
 * Created by aroha on 19-04-2018.
 */

app.factory('ObjFieldSettings', function() {
    // ----------------------------------- Constructor ------------------------------------//
    function ObjFieldSettings (bMandatory) {
        this.bMandatory = bMandatory
    }

    // ----------------------------------- Public APIs ------------------------------------//
    // Method to clone a FieldSettings object
    ObjFieldSettings.prototype.Clone = function () {
        // Return clone of FieldSettings object
        return new ObjFieldSettings(this.bMandatory)
    }

    // ----------------------------------- Private APIs ------------------------------------//
    // ----------------------------------- Static APIs ------------------------------------//
    // Methods to convert between Frontend FieldSettings Object and JSON
    ObjFieldSettings.fromJson = function (json) {
        return new ObjFieldSettings(json.bMandatory)
    }

    ObjFieldSettings.toJson = function (fieldSetting) {
        // Return FieldSettings JSON
        return {
            bMandatory :  fieldSetting.bMandatory
        }
    }

    return ObjFieldSettings
})
