/**
 * Created by aroha on 16-06-2018.
 */

app.factory('ObjProduct', function(TemplateService, ObjValue) {
    // ----------------------------------- Constructor ------------------------------------//
    function ObjProduct (id, productId, name, template, values) {
        this.id             = id
        this.productId      = productId
        this.name           = name
        this.template       = template
        this.values         = values
    }

    // ----------------------------------- Public APIs ------------------------------------//
    // Method to clone a Product object
    ObjProduct.prototype.Clone = function () {
        // Create clone for values
        var values = []
        this.values.forEach(function (value) {
            values.push(value.Clone())
        })

        // Return clone of product object
        return new ObjProduct(this.id, this.productId, {id: this.id, name: this.name}, this.template, values)
    }

    // Method to parse data into row
    ObjProduct.prototype.toRow = function (table) {
        // Create new row object with blank value for each column
        var values = Statics.getArray(table.columns.length)
        values.fill('-')

        // Add id and name to row object
        var row = {id: this.id, name: this.name, values: values}

        // Add data in mandatory columns
        row.values[table.getColumnIdxById(Constants.Table.ID_PRODUCT_ID)]       = this.productId
        row.values[table.getColumnIdxById(Constants.Table.ID_PRODUCT_NAME)]     = {id: this.id, name: this.name}
        row.values[table.getColumnIdxById(Constants.Table.ID_PRODUCT_TEMPLATE)] = this.template.name

        // Iterate through template values
        this.values.forEach(function (value) {
            // Add string value at appropriate index in row
            row.values[table.getColumnIdxById(value.field.id)] = value.getDisplayString()
        })

        return row
    }

    // ----------------------------------- Validation APIs ------------------------------------//

    ObjProduct.prototype.isValid = function () {
        if (this.getNameErr().length) {
            return false
        }
        if (this.getTemplateErr().length) {
            return false
        }
        if (this.getProductIdErr().length) {
            return false
        }
        for (var i = 0; i < this.values.length; i++) {
            if (this.values[i].getErr().length > 0) {
                return false
            }
        }
        return true
    }

    ObjProduct.prototype.getProductIdErr = function () {
        if (!this.name) {
            return 'Set a product Id'
        }

        return ''
    }

    ObjProduct.prototype.getNameErr = function () {
        if (!this.name) {
            return 'Set a name'
        }

        return ''
    }

    ObjProduct.prototype.getTemplateErr = function () {
        if (!this.template) {
            return 'Select a template'
        }

        return ''
    }
    // ----------------------------------- Private APIs ------------------------------------//
    // ----------------------------------- Static APIs ------------------------------------//
    // Methods to convert between Frontend Field Object and JSON
    ObjProduct.fromJson = function (json) {
        // Create values from JSON
        var values = []
        json.values.forEach(function (value) {
            values.push(ObjValue.fromJson(value))
        })

        return new ObjProduct(
            json.id,
            json.productId,
            json.name,
            TemplateService.getById(json.templateId),
            values)
    }

    ObjProduct.toJson = function (product) {
        // Convert values to JSON
        var valuesJson = []
        product.values.forEach(function (value) {
            valuesJson.push(ObjValue.toJson(value))
        })

        // Return field JSON
        return {
            id:             product.id,
            productId:      product.productId,
            name:           product.name,
            templateId:     product.template.id,
            values:         valuesJson
        }
    }

    return ObjProduct
})
