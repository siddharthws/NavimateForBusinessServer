/**
 * Created by Chandel on 06-02-2018.
 */

app.controller("CompanySettingsCtrl", function ($scope, $localStorage) {
    var vm = this

    /*------------------------------------ INIT --------------------------------*/
    // Set menu and option
    $scope.nav.item = Constants.DashboardNav.Menu[Constants.DashboardNav.ITEM_COMPANY]
    $scope.nav.option = Constants.DashboardNav.Options[Constants.DashboardNav.OPTION_SETTINGS]

    vm.startHr = $localStorage.startHr
    vm.endHr = $localStorage.endHr

    /*-------------------------------- Scope APIs --------------------------------*/
    vm.incrementStartHr = function () {
            if(vm.startHr == 23) {
                vm.startHr = 0
            }
            else {
                vm.startHr++
            }
    }

    vm.incrementEndHr = function () {
        if(vm.endHr == 23) {
            vm.endHr = 0
        }
        else {
            vm.endHr++
        }
    }

    vm.decrementStartHr = function () {
        if(vm.startHr == 0) {
            vm.startHr = 23
        }
        else {
            vm.startHr--
        }
    }


    vm.decrementEndHr = function () {
        if(vm.endHr == 0) {
            vm.endHr = 23
        }
        else {
            vm.endHr--
        }
    }
});