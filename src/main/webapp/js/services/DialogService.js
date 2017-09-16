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
    this.confirm = function (message, yesCb) {
        // Show Dialog
        $mdDialog.show({    templateUrl: '../views/dialogs/Confirm.html',
                            controller: 'ConfirmCtrl',
                            clickOutsideToClose: true,
                            locals: {
                                message:    message,
                                yesCb:      yesCb
                            }
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
    this.addRep = function (teamUpdateCb) {
        // Show Dialog
        $mdDialog.show({    templateUrl: '../views/dialogs/AddRep.html',
                            controller: 'AddRepCtrl',
                            clickOutsideToClose: true,
                            locals: { teamUpdateCb: teamUpdateCb}
        })
    }

    // Launch Task Creator Dialog
    this.taskCreator = function (taskAddedCb) {
        // Show Dialog
        $mdDialog.show({    templateUrl: '../views/dialogs/TaskCreator.html',
                            controller: 'TaskCreatorCtrl',
                            clickOutsideToClose: true,
                            locals: { taskAddedCb: taskAddedCb}
        })
    }

    // Launch Lead Editor Dialog
    this.leadEditor = function (leads, leadUpdateCb) {
        // Show Dialog
        $mdDialog.show({    templateUrl: '../views/dialogs/LeadEditor.html',
                            controller: 'LeadEditorCtrl',
                            clickOutsideToClose: true,
                            locals: { leads: leads, leadUpdateCb: leadUpdateCb }
        })
    }
})