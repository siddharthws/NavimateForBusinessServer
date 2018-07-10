/**
 * Created by aroha on 06-06-2018.
 */

app.controller("ProductManageCtrl", function ( $scope, $rootScope, NavService, DialogService, TableService, ProductService) {
        /* ------------------------------- INIT -----------------------------------*/
        var vm = this

        // Set Active Tab and Menu
        NavService.setActive(NavService.product, 0)

        // Set products table as active
        TableService.activeTable = ProductService.table
        vm.table = TableService.activeTable

        /* ------------------------------- Scope APIs -----------------------------------*/
        vm.add = function() {
            DialogService.productEditor(null, vm.reset)
        }

        vm.edit = function () {
            //Launch products-Editor dialog
            DialogService.productEditor(vm.table.getSelectedIds(), vm.reset)
        }

        vm.reset = function () {
            // Broadcast Toggle Columns Event
            $scope.$broadcast(Constants.Events.TABLE_RESET)
        }

        vm.toggleColumns = function () {
            // Broadcast Toggle Columns Event
            $scope.$broadcast(Constants.Events.TABLE_TOGGLE_COLUMNS)
        }

        vm.sync = function () {
            // Broadcast Toggle Columns Event
            $scope.$broadcast(Constants.Events.TABLE_SYNC)
        }

        /* ------------------------------- Local APIs -----------------------------------*/
})
