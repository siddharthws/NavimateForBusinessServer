/**
 * Created by aroha on 06-06-2018.
 */

app.controller("ProductManageCtrl", function ( $scope, $http, $rootScope, $localStorage,  ToastService,
                                               NavService, DialogService, TableService, ProductService) {
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

        // APIs for actions in dropdown
        vm.remove = function() {
            // Launch Confirm Dialog
            DialogService.confirm(
                "Are you sure you want to remove " + vm.table.selection.length + " products ?",
                function () {
                    $rootScope.showWaitingDialog("Please wait while we are removing products...")
                    // Make Http call to remove products
                    $http({
                        method: 'POST',
                        url: '/api/manager/products/remove',
                        headers: {
                            'X-Auth-Token': $localStorage.accessToken
                        },
                        data: {
                            ids: vm.table.getSelectedIds()
                        }
                    }).then(
                        function (response) {
                            $rootScope.hideWaitingDialog()

                            // Reset table Selection
                            vm.table.selection = []

                            // Sync data again
                            vm.sync()

                            // Show Toast
                            ToastService.toast("products removed...")
                        },
                        function (error) {
                            $rootScope.hideWaitingDialog()
                            ToastService.toast("Failed to remove products!!!")
                        })
                })
        }

        /* ------------------------------- Local APIs -----------------------------------*/
})
