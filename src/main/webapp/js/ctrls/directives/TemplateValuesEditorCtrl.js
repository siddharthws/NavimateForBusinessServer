/**
 * Created by Aroha on 24-07-2018.
 */

app.controller('TemplateValuesEditorCtrl', function (ProductService, DialogService) {
    var vm = this

    vm.pickProduct = function (value) {
        DialogService.table2Picker("Pick product", ProductService.table, function (id, name) {
            value.value = {id: id, name: name}
        })
    }

    vm.viewProduct = function (value) {
        DialogService.productViewer(value.value.id)
    }
})
