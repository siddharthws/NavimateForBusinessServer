/**
 * Created by Siddharth on 29-08-2017.
 */

app.service('DialogService', function($mdDialog) {

    // Launch Alert Dialog
    this.alert = function (message) {
        // Show Dialog
        $mdDialog.show({    templateUrl: '/static/views/dialogs/Alert.html',
                            controller: 'AlertCtrl',
                            clickOutsideToClose: true,
                            locals: { message: message }
        })
    }

    // Launch Confirm Dialog
    this.confirm = function (message, yesCb) {
        // Show Dialog
        $mdDialog.show({    templateUrl: '/static/views/dialogs/Confirm.html',
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
        $mdDialog.show({    templateUrl: '/static/views/dialogs/Login.html',
                            controller: 'LoginCtrl',
                            clickOutsideToClose: true
        })
    }

    // Launch Register Dialog
    this.register = function (name, email, password) {
        // Show Dialog
        $mdDialog.show({    templateUrl: '/static/views/dialogs/Register.html',
                            controller: 'RegisterCtrl',
                            clickOutsideToClose: true,
                            locals: {
                                name: name,
                                email: email,
                                password: password,
                            }
        })
    }

    // Launch Rep Add Dialog
    this.addRep = function (teamUpdateCb) {
        // Show Dialog
        $mdDialog.show({    templateUrl: '/static/views/dialogs/AddRep.html',
                            controller: 'AddRepCtrl',
                            clickOutsideToClose: true,
                            locals: { teamUpdateCb: teamUpdateCb}
        })
    }

    // Launch Task Creator Dialog
    this.taskCreator = function (tasks, taskAddedCb) {
        // Show Dialog
        $mdDialog.show({    templateUrl: '/static/views/dialogs/TaskCreator.html',
                            controller: 'TaskCreatorCtrl',
                            clickOutsideToClose: true,
                            locals: {tasks: tasks, taskAddedCb: taskAddedCb}
        })
    }

    // Launch Lead Editor Dialog
    this.leadEditor = function (leads, leadUpdateCb) {
        // Show Dialog
        $mdDialog.show({    templateUrl: '/static/views/dialogs/LeadEditor.html',
                            controller: 'LeadEditorCtrl',
                            clickOutsideToClose: true,
                            locals: { leads: leads, leadUpdateCb: leadUpdateCb }
        })
    }

    // Launch Edit Form Dialog
    this.editForm = function (form, updateCb) {
        // Show Dialog
        $mdDialog.show({    templateUrl: '/static/views/dialogs/EditForm.html',
                            controller: 'EditFormCtrl',
                            clickOutsideToClose: true,
                            locals: { form: form, updateCb: updateCb }
        })
    }

    // Launch Live Tracking Dialog
    this.liveTracking = function (reps) {
        // Show Dialog
        $mdDialog.show({    templateUrl: '/static/views/dialogs/LiveTracking.html',
                            controller: 'LiveTrackingCtrl',
                            clickOutsideToClose: false,
                            locals: {
                                reps: reps
                            }
        })
    }

    // Launch Email Verify Dialog
    this.emailVerify = function (name, email, passowrd) {
        // Show Dialog
        $mdDialog.show({    templateUrl: '/static/views/dialogs/EmailVerify.html',
                            controller: 'EmailVerifyCtrl',
                            clickOutsideToClose: false,
                            locals: {
                                name: name,
                                email: email,
                                password: passowrd,
                            }
        })
    }
})