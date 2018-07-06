/**
 * Created by Siddharth on 25-04-2018.
 */

app.factory('ObjFilter', function() {
    // ----------------------------------- Constructor ------------------------------------//
    function ObjFilter (fieldType) {
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
            case Constants.Template.FIELD_TYPE_TEMPLATE: {
                this.type = Constants.Filter.TYPE_SELECTION
                this.value = null
                break
            }
            case Constants.Template.FIELD_TYPE_LEAD:
            case Constants.Template.FIELD_TYPE_TASK: {
                this.type = Constants.Filter.TYPE_OBJECT
                this.value = ""
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
        this.bShow = true
        this.bSortable = isSortable()
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
    // Method to determine if sorting is allowed for this filter
    function isSortable() {
        // Mark non sortable for specific field types
        if (this.type == Constants.Template.FIELD_TYPE_PHOTO ||
            this.type == Constants.Template.FIELD_TYPE_FILE ||
            this.type == Constants.Template.FIELD_TYPE_SIGN ||
            this.type == Constants.Template.FIELD_TYPE_LOCATION) {
            return false
        }

        // Mark sortable for all other field types
        return true
    }

    // ----------------------------------- Static APIs ------------------------------------//

    return ObjFilter
})
