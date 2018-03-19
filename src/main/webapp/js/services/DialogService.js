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
    this.register = function (name, email, password,role,companyName) {
        // Show Dialog
        $mdDialog.show({    templateUrl: '/static/views/dialogs/Register.html',
                            controller: 'RegisterCtrl',
                            clickOutsideToClose: true,
                            locals: {
                                name: name,
                                email: email,
                                password: password,
                                role:role,
                                companyName:companyName
                            }
        })
    }

    // Team Editor Dialog
    this.teamEditor = function (ids, cb) {
        // Show Dialog
        $mdDialog.show({    templateUrl: '/static/views/dialogs/TeamEditor.html',
                            controller: 'TeamEditorCtrl as vm',
                            clickOutsideToClose: true,
                            locals: { ids: ids, cb: cb}
        })
    }

    // Launch Task Creator Dialog
    this.taskCreator = function (taskIds, editCb) {
        // Show Dialog
        $mdDialog.show({    templateUrl: '/static/views/dialogs/TaskCreator.html',
                            controller: 'TaskCreatorCtrl',
                            clickOutsideToClose: true,
                            locals: { taskIds: taskIds, editCb: editCb}
        })
    }

    // Launch Lead Editor Dialog
    this.leadEditor = function (leads, editCb) {
        // Show Dialog
        $mdDialog.show({    templateUrl: '/static/views/dialogs/LeadEditor.html',
                            controller: 'LeadEditorCtrl',
                            clickOutsideToClose: true,
                            locals: { leads: leads, editCb: editCb }
        })
    }

    // Launch Form Template Editor Dialog
    this.formTemplateEditor = function (template, cb) {
        // Show Dialog
        $mdDialog.show({    templateUrl: '/static/views/dialogs/FormTemplateEditor.html',
                            controller: 'FormTemplateEditorCtrl as $ctrl',
                            clickOutsideToClose: true,
                            locals: { template: template, cb: cb }
        })
    }

    // Launch Lead Template Editor Dialog
    this.leadTemplateEditor = function (template, cb) {
        // Show Dialog
        $mdDialog.show({    templateUrl: '/static/views/dialogs/LeadTemplateEditor.html',
                            controller: 'LeadTemplateEditorCtrl as $ctrl',
                            clickOutsideToClose: true,
                            locals: { template: template, cb: cb }
        })
    }

    // Launch Task Template Editor Dialog
    this.taskTemplateEditor = function (template, cb) {
        // Show Dialog
        $mdDialog.show({    templateUrl: '/static/views/dialogs/TaskTemplateEditor.html',
                            controller: 'TaskTemplateEditorCtrl as $ctrl',
                            clickOutsideToClose: true,
                            locals: { template: template, cb: cb }
        })
    }

    // Launch Live Tracking Dialog
    this.liveTracking = function (reps) {
        // Show Dialog
        $mdDialog.show({    templateUrl: '/static/views/dialogs/LiveTracking.html',
                            controller: 'LiveTrackingCtrl as $ctrl',
                            clickOutsideToClose: false,
                            locals: {
                                reps: reps
                            }
        })
    }

    // Launch Email Verify Dialog
    this.emailVerify = function (name, email, password, role, companyName) {
        // Show Dialog
        $mdDialog.show({    templateUrl: '/static/views/dialogs/EmailVerify.html',
                            controller: 'EmailVerifyCtrl',
                            clickOutsideToClose: false,
                            locals: {
                                name: name,
                                email: email,
                                password: password,
                                role:role,
                                companyName:companyName
                            }
        })
    }

    // Launch Toggle Columns Dialog
    this.toggleColumnsOld = function (columns, resultCb) {
        // Show Dialog
        $mdDialog.show({    templateUrl: '/static/views/dialogs/ToggleColumnsOld.html',
                            controller: 'ToggleColumnsOldCtrl as $ctrl',
                            clickOutsideToClose: true,
                            locals: {
                                columns: columns,
                                resultCb: resultCb
                            }
        })
    }
    this.toggleColumns = function (columns) {
        // Show Dialog
        $mdDialog.show({    templateUrl: '/static/views/dialogs/ToggleColumns.html',
            controller: 'ToggleColumnsCtrl as $ctrl',
            clickOutsideToClose: true,
            locals: {
                columns: columns
            }
        })
    }

    // Launch Change Password Dialog
    this.changePassword = function () {
        // Show Dialog
        $mdDialog.show({    templateUrl: '/static/views/dialogs/ChangePassword.html',
                            controller: 'ChangePasswordCtrl',
                            clickOutsideToClose: true
        })
    }

    // Launch Location Viewer Dialog
    this.locationViewer = function (locations) {
        // Show Dialog
        $mdDialog.show({    templateUrl: '/static/views/dialogs/LocationViewer.html',
                            controller: 'LocationViewerCtrl',
                            clickOutsideToClose: true,
                            locals: {
                                locations: locations
                            }
        })
    }
})