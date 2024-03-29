/**
 * Created by Siddharth on 25-04-2018.
 */

app.factory('ObjFilter', function() {
    // ----------------------------------- Constructor ------------------------------------//
    function ObjFilter (fieldType, bShow) {
        this.type = Constants.Filter.TYPE_NONE
        this.value = null

        // Assign type and value based on field type
        switch (fieldType) {
            case Constants.Template.FIELD_TYPE_TEXT:
            case Constants.Template.FIELD_TYPE_RADIOLIST:
            case Constants.Template.FIELD_TYPE_CHECKLIST:
            case Constants.Template.FIELD_TYPE_CHECKBOX: {
                this.type = Constants.Filter.TYPE_TEXT
                this.value = ""
                break
            }
            case Constants.Template.FIELD_TYPE_TEMPLATE:
            case Constants.Template.FIELD_TYPE_REP:
            case Constants.Template.FIELD_TYPE_NON_REP:
            case Constants.Template.FIELD_TYPE_LEAD:
            case Constants.Template.FIELD_TYPE_TASK:
            case Constants.Template.FIELD_TYPE_PRODUCT: {
                this.type = Constants.Filter.TYPE_SELECTION
                this.value = null
                break
            }
            case Constants.Template.FIELD_TYPE_NUMBER: {
                this.type = Constants.Filter.TYPE_NUMBER
                this.value = {from: null, to: null}
                break
            }
            case Constants.Template.FIELD_TYPE_DATE: {
                this.type = Constants.Filter.TYPE_DATE
                this.value = {from: null, to: null}
                break
            }
        }

        // Assign other parameters
        this.bNoBlanks = false
        this.bShow = bShow
        this.sort = Constants.Table.SORT_NONE
    }

    // ----------------------------------- Public APIs ------------------------------------//
    // Method to convert into JSON which can be understood by the server
    ObjFilter.prototype.toJson = function () {
        return {
            value: this.value,
            bNoBlanks: this.bNoBlanks
        }
    }

    // ----------------------------------- Private APIs ------------------------------------//
    // ----------------------------------- Static APIs ------------------------------------//

    return ObjFilter
})
