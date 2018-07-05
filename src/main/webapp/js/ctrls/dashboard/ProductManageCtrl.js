/**
 * Created by aroha on 06-06-2018.
 */

app.controller("ProductManageCtrl", function ( $scope, $rootScope, NavService, DialogService) {
        /* ------------------------------- INIT -----------------------------------*/
        var vm = this

        // Set Active Tab and Menu
        NavService.setActive(NavService.product, 0)

        /* ------------------------------- Scope APIs -----------------------------------*/
        vm.add = function() {
            DialogService.productEditor(null, vm.reset)
        }

        vm.edit = function () {
            //Launch products-Editor dialog
            DialogService.productEditor(vm.table.getSelectedIds(), vm.reset)
        }

        /* ------------------------------- Local APIs -----------------------------------*/
})
