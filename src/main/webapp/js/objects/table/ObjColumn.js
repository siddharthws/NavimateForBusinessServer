/**
 * Created by Siddharth on 25-04-2018.
 */

app.factory('ObjColumn', function(ObjFilter) {
    // ----------------------------------- Constructor ------------------------------------//
    function ObjColumn (id, name, type, fieldId, label, objectId) {
        this.id = id
        this.name = name
        this.type = type
        this.fieldId = fieldId
        this.label = label
        this.objectId = objectId
        this.filter = new ObjFilter(type)
        this.size = Constants.Table.COL_SIZE_L
    }

    // ----------------------------------- Public APIs ------------------------------------//
    // ----------------------------------- Private APIs ------------------------------------//
    // ----------------------------------- Static APIs ------------------------------------//
    return ObjColumn
})
