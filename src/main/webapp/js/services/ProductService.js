/**
 * Created by aroha on 14-06-2018.
 */
app.service('ProductService', function($q, $http, $localStorage, TemplateService, ObjTable2, ObjColumn, ObjProduct) {
    /* ----------------------------- INIT --------------------------------*/
    var vm = this

    vm.cache = null
    vm.table = null

    // Sync Serialization related variables
    var canceller = null
    var bOngoing = false

    /* ----------------------------- APIs --------------------------------*/

    // Method to reset Service
    vm.reset = function () {
        // Reset cache
        vm.cache = []

        // Init Product Table
        vm.table = new ObjTable2(Constants.Table.TYPE_PRODUCT, vm.getTableColumns, vm.parseTableResponse)
    }

    // API to get product data
    vm.sync = function (ids) {
        if (bOngoing) {
            canceller.resolve()
            bOngoing = false
        }
        canceller = $q.defer()

        // Create deferred object
        var deferred = $q.defer()

        // Trigger request
        bOngoing = true
        $http({
            method: 'POST',
            url: '/api/manager/products/getByIds',
            timeout: canceller.promise,
            headers: {
                'X-Auth-Token': $localStorage.accessToken
            },
            data: {
                ids: ids
            }
        }).then(
            // Success
            function (response) {
                // Reset ongoing flag
                bOngoing = false

                // Update cache
                vm.cache = []
                response.data.forEach(function (productsJson) {
                    vm.cache.push(ObjProduct.fromJson(productsJson))
                })

                // Resolve promise
                deferred.resolve()
            },
            // Error
            function (error) {
                // Ignore if request was cancelled
                if (error.status == -1) {
                    return
                }

                // Reset ongoing flag
                bOngoing = false

                // Resolve promise
                deferred.reject()
            })

        return deferred.promise
    }

    // API to edit task data
    vm.edit = function (products) {
        // Create deferred object
        var deferred = $q.defer()

        // Convert Product to JSON
        var productJson = []
        products.forEach(function (product) {
            productJson.push(ObjProduct.toJson(product))
        })

        // Trigger request
        $http({
            method: 'POST',
            url: '/api/manager/products/edit',
            headers: {
                'X-Auth-Token': $localStorage.accessToken
            },
            data: {
                products: productJson
            }
        }).then(
            // Success
            function (response) {
                // Resolve promise
                deferred.resolve()
            },
            // Error
            function (error) {
                // Reject promise
                deferred.reject(error.data.error)
            })

        return deferred.promise
    }


    // API to get product by ID
    vm.getById = function (id) {
        for (var i = 0; i < vm.cache.length; i++) {
            var product = vm.cache[i]
            if (product.id == id) {
                return product
            }
        }

        return null
    }

    // Methods to get columns for different table types
    vm.getTableColumns = function () {
        var columns = []

        // Get constant reference for usign locally
        var Table_C = Constants.Table
        var Template_C = Constants.Template

        // Add mandatory columns
        columns.push(new ObjColumn(Table_C.ID_PRODUCT_ID,       "ID",       Template_C.FIELD_TYPE_TEXT,         null, "productId",  Constants.Template.TYPE_PRODUCT, 1), true)
        columns.push(new ObjColumn(Table_C.ID_PRODUCT_NAME,     "Name",     Template_C.FIELD_TYPE_PRODUCT,      null, "name",       Constants.Template.TYPE_PRODUCT, 2), true)
        columns.push(new ObjColumn(Table_C.ID_PRODUCT_TEMPLATE, "Template", Template_C.FIELD_TYPE_TEMPLATE,     null, "template",   Constants.Template.TYPE_PRODUCT, 3), false)

        // Iterate through each product template
        TemplateService.getByType(Constants.Template.TYPE_PRODUCT).forEach(function (template) {
            // Iterate through each field
            template.fields.forEach(function (field, i) {
                var bShow = i < 2

                // Add new column to array
                columns.push(new ObjColumn(field.id, field.title, field.type, field.id, String(field.id), Constants.Template.TYPE_PRODUCT, columns.length + 1, bShow))
            })
        })

        return columns
    }

    // Method to parse product sync response to tabular format
    vm.parseTableResponse = function (response) {
        // Parse response into rows for table
        var rows = []
        response.products.forEach(function (json) {
            // Parse to product Object
            var product = ObjProduct.fromJson(json)

            // Add to rows
            rows.push(product.toRow(vm.table))
        })

        return rows
    }

    // Init Product Table
    vm.reset()
})