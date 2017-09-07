/**
 * Created by Siddharth on 29-08-2017.
 */

app.service('DialogService', function($mdDialog) {

    // Launch Alert Dialog
    this.alert = function (message) {
        // Show Dialog
        $mdDialog.show({    templateUrl: '../views/dialogs/Alert.html',
                            controller: 'AlertCtrl',
                            clickOutsideToClose: true,
                            locals: { message: message }
        })
    }

    // Launch Confirm Dialog
    this.confirm = function (message) {
        // Show Dialog
        $mdDialog.show({    templateUrl: '../views/dialogs/Confirm.html',
                            controller: 'ConfirmCtrl',
                            clickOutsideToClose: true,
                            locals: { message: message }
        })
    }

    // Launch Login Dialog
    this.login = function () {
        // Show Dialog
        $mdDialog.show({    templateUrl: '../views/dialogs/Login.html',
                            controller: 'LoginCtrl',
                            clickOutsideToClose: true
        })
    }

    // Launch Register Dialog
    this.register = function () {
        // Show Dialog
        $mdDialog.show({    templateUrl: '../views/dialogs/Register.html',
                            controller: 'RegisterCtrl',
                            clickOutsideToClose: true
        })
    }

    // Launch Map Editor Dialog
    this.mapEditor = function (leads) {
        // Show Dialog
        $mdDialog.show({    templateUrl: '../views/dialogs/MapEditor.html',
                            controller: 'MapEditorCtrl',
                            clickOutsideToClose: true,
                            locals: { leads: leads }
        })
    }

    // Launch Rep Add Dialog
    this.addRep = function () {
        // Show Dialog
        $mdDialog.show({    templateUrl: '../views/dialogs/AddRep.html',
                            controller: 'AddRepCtrl',
                            clickOutsideToClose: true
        })
    }
})