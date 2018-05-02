/**
 * Created by aroha on 19-04-2018.
 */

app.controller('FieldSettingsCtrl', function ($scope, $rootScope, $mdDialog, field) {
    /* ------------------------------- Init -----------------------------------*/
    var vm = this

    // Init Object
    vm.field= field
    vm.Const = $rootScope.Constants.Template

    /* ------------------------------- Public APIs -----------------------------------*/
    vm.close = function () {
        $mdDialog.hide()
    }

        /* ------------------------------- Private APIs -----------------------------------*/
        /* ------------------------------- Post Init -----------------------------------*/
})
