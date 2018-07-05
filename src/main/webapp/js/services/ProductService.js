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

})