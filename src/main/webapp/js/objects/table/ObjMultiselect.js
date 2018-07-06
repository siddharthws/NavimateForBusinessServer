/**
 * Created by Siddharth on 05-07-2018.
 */

app.factory('ObjMultiselect', function() {
    // ----------------------------------- Constructor ------------------------------------//
    function ObjMultiselect (type, list) {
        this.type = type
        this.list = list
    }

    // ----------------------------------- Public APIs ------------------------------------//
    // Method to select / unselect all
    ObjMultiselect.prototype.selectAll = function () {
        this.type = Constants.Table.MS_EXCLUDE
        this.list = []
    }

    ObjMultiselect.prototype.unselectAll = function () {
        this.type = Constants.Table.MS_INCLUDE
        this.list = []
    }

    // Method to toggle selection status of an id
    ObjMultiselect.prototype.toggle = function (id) {
        // Remove / Add in list depending on current list
        if (this.list.contains(id)) {
            this.list.splice(this.list.indexOf(id), 1)
        } else {
            this.list.push(id)
        }
    }

    // Method to check selection status of an id
    ObjMultiselect.prototype.isSelected = function (id) {
        switch (this.type) {
            case Constants.Table.MS_INCLUDE: {
                return this.list.contains(id)
            }
            case Constants.Table.MS_EXCLUDE: {
                return !this.list.contains(id)
            }
        }

        return false
    }

    // Method to add selection from another object
    ObjMultiselect.prototype.add = function (selection) {
        switch (this.type) {
            case Constants.Table.MS_INCLUDE:
                if (selection.type == Constants.Table.MS_INCLUDE) {
                    this.list.addAll(selection.list)
                } else {
                    this.type = Constants.Table.MS_EXCLUDE
                    var incList = this.list
                    var excList = selection.list
                    incList.forEach(function (id) {
                        if (excList.contains(id)) {
                            excList.splice(excList.indexOf(id), 1)
                        }
                    })
                    this.list = excList
                }
                break
            case Constants.Table.MS_EXCLUDE:
                if (selection.type == Constants.Table.MS_INCLUDE) {
                    var incList = selection.list
                    var excList = this.list
                    incList.forEach(function (id) {
                        if (excList.contains(id)) {
                            excList.splice(excList.indexOf(id), 1)
                        }
                    })
                    this.list = excList
                } else {
                    var commons = []
                    this.list.forEach(function (id) {
                        if (selection.list.contains(id)) {
                            commons.push(id)
                        }
                    })
                    this.list = commons
                }
                break
        }
    }

    // Method to get number of elements in selection
    ObjMultiselect.prototype.getCount = function (total) {
        switch (this.type) {
            case Constants.Table.MS_INCLUDE: {
                return this.list.length
            }
            case Constants.Table.MS_EXCLUDE: {
                return total - this.list.length
            }
        }
    }

    // Method to check if all are selected
    ObjMultiselect.prototype.isDefault = function () {
        if (this.type == Constants.Table.MS_EXCLUDE && this.list.length == 0) {
            return true
        }

        return false
    }


    // Method to convert object in server JSON
    ObjMultiselect.prototype.toJson = function () {
        return {
            type: this.type,
            list: this.list
        }
    }

    // ----------------------------------- Private APIs ------------------------------------//
    // ----------------------------------- Static APIs ------------------------------------//
    // Method to create Multiselect object from JSON
    ObjMultiselect.fromJson = function (json) {
        return new ObjMultiselect(json.type, json.list)
    }

    return ObjMultiselect
})
