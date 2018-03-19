/**
 * Created by Siddharth on 15-03-2018.
 */

app.factory('ObjUser', function() {
    // ----------------------------------- Constructor ------------------------------------//
    function ObjUser (id, name, role, phone, countryCode, email) {
        this.id             = id
        this.name           = name
        this.role           = role
        this.phone          = phone
        this.countryCode    = countryCode
        this.email          = email
    }

    // ----------------------------------- Public APIs ------------------------------------//
    // Method to clone a field object
    ObjUser.prototype.Clone = function () {
        // Return clone of this object
        return new ObjUser(this.id, this.name, this.role, this.phone, this.countryCode, this.email)
    }

    // Method to validate User object
    ObjUser.prototype.isValid = function () {
        if (this.getNameErr() || this.getPhoneErr()) {
            return false
        }

        return true
    }

    // Methods to check validity and return error text
    ObjUser.prototype.getNameErr = function () {
        if (!this.name) {
            return "Name is required"
        }

        return ""
    }

    ObjUser.prototype.getPhoneErr = function () {
        if (!this.phone || this.phone == "null") {
            return "Phone number is required"
        }

        return ""
    }

    // ----------------------------------- Private APIs ------------------------------------//
    // ----------------------------------- Static APIs ------------------------------------//
    // Methods to convert between Frontend Field Object and JSON
    ObjUser.fromJson = function (json) {
        return new ObjUser(
            json.id,
            json.name,
            json.role,
            json.phone,
            json.countryCode,
            json.email
        )
    }

    ObjUser.toJson = function (user) {
        // Return field JSON
        return {
            id:             user.id,
            name:           user.name,
            role:           user.role,
            phone:          user.phone,
            countryCode:    user.countryCode,
            email:          user.email
        }
    }

    return ObjUser
})
