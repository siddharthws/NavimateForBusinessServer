/**
 * Created by aroha on 09-06-2018.
 */

app.controller("ProductEditorCtrl", function ( $scope, $rootScope, $mdDialog, NavService, DialogService, ObjProduct,
                                               ProductService, ids, cb, TemplateService, ObjValue, ToastService) {
    /* ------------------------------- INIT -----------------------------------*/
    var vm = this

    // Product List
    vm.products = []
    vm.selectedProduct= {}

    // Error / Waiting UI
    vm.bLoading = false
    vm.bLoadError = false
    vm.bInputError = false

    // Create array of product template objects to send to dropdowns
    vm.templates = []
    TemplateService.getByType(Constants.Template.TYPE_PRODUCT).forEach(function (template) {
        vm.templates.push({id: template.id, name: template.name})
    })

    /* ------------------------------- Scope APIs -----------------------------------*/
    // Button Click APIs
    vm.add = function () {
        // Create empty product
        var product = new ObjProduct(
            null,
            "",
            "",
            null,
            [])

        // Add empty product to array
        vm.products.push(product)

        // Select this product
        vm.selectedProduct = product

        // Add template data
        vm.updateTemplate(0)
    }

    vm.copy = function () {
        // Clone selected product
        var clonedProduct = vm.selectedProduct.Clone()

        // Remove ID
        clonedProduct.id = null

        // Add to array
        vm.products.push(clonedProduct)

        // Mark as selected
        vm.selectedProduct = clonedProduct
    }

    vm.updateTemplate = function (idx) {
        // Get template by ID
        var id = vm.templates[idx].id
        var template = TemplateService.getById(id)

        // Ignore if same template is selected
        if (vm.selectedProduct.template && vm.selectedProduct.template.id == template.id) return

        // Update product template
        vm.selectedProduct.template = template

        // Update template values
        vm.selectedProduct.values = []
        template.fields.forEach(function (field) {
            // Create new value Object
            var value = new ObjValue(JSON.parse(JSON.stringify(field.value)), field)

            // Add to values
            vm.selectedProduct.values.push(value)
        })
    }

    vm.save = function () {
        // Validate Entered Data
        if (validate()) {
            $rootScope.showWaitingDialog("Please wait while Products are edited...")
            ProductService.edit(vm.products).then(
                // Success callback
                function () {
                    // Dismiss Dialog & notify user
                    $rootScope.hideWaitingDialog()
                    $mdDialog.hide()
                    ToastService.toast("Products edited successfully...")

                    // Trigger callback
                    cb()
                },
                // Error callback
                function () {
                    // Show Error Toast
                    $rootScope.hideWaitingDialog()
                    ToastService.toast("Unable to edit Products...")
                }
            )
        }
    }

    vm.remove = function (idx) {
        // Close dialog if all items removed
        if (vm.products.length == 1) {
            vm.close()
        }

        // Update selected product if required
        if (vm.products == vm.products[idx]) {
            if (idx == vm.products.length - 1) {
                vm.selectedProduct = vm.products[idx - 1]
            } else {
                vm.selectedProduct = vm.products[idx + 1]
            }
        }

        // Remove item from array
        vm.products.splice(idx, 1)
    }

    vm.close = function () {
        $mdDialog.hide()
    }

    /* ----------------------------- Post INIT --------------------------------*/
    // API to validate entered data
    function validate () {
        // Reset error flag
        vm.bInputError = false

        // Validate each product
        for (var i = 0; i < vm.products.length; i++) {
            if (!vm.products[i].isValid()) {
                vm.bInputError = true
                return false
            }
        }

        return true
    }

    /* ----------------------------- INIT --------------------------------*/
    // Init products
    if (ids) {
        // Sync using service
        vm.bLoading = true
        ProductService.sync(ids).then(
            function () {
                vm.bLoading = false
                vm.products = ProductService.cache
                vm.selectedProduct= vm.products[0]
            },
            function () {
                vm.bLoading = false
                vm.bLoadError = true
            }
        )
    } else {
        vm.add()
    }
})
