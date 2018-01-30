/**
 * Created by Siddharth on 29-11-2017.
 */

// Controller for Alert Dialog
app.controller('FormTemplateEditorCtrl', function ($scope, $rootScope, $mdDialog, $http, $localStorage, ToastService, template, updateCb, TemplateDataService) {
    var vm = this

    /* ------------------------------- HTML APIs -----------------------------------*/
    // Close dialog
    vm.close = function () {
        $mdDialog.hide()
    }

    // API to save template
    vm.save = function () {
        // Broadcast Template validation event and wait for success.
        // Success event listener is set during init)
        $scope.$broadcast(Constants.Events.TEMPLATE_VALIDATE)
    }

    /* ------------------------------- Local APIs -----------------------------------*/
    function savePostValidation() {
        $rootScope.showWaitingDialog("Please wait while template is being saved...")
        $http({
            method:     'POST',
            url:        '/api/users/template',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            },
            data: {
                'template': $scope.template
            }
        }).then(
            function (response) {
                // Hide dialog and show toast
                $rootScope.hideWaitingDialog()
                $mdDialog.hide()
                ToastService.toast("Template saved successfully...")

                // Re-sync Template data since template has been added.
                TemplateDataService.syncForms()
            },
            function (error) {
                // Hide waiting and show error toast
                $rootScope.hideWaitingDialog()
                ToastService.toast(error.data.error)
            }
        )
    }
    /* ------------------------------- Init -----------------------------------*/
    // Attach template to scope to pass to child template editor view
    if (template) {
        $scope.template = JSON.parse(JSON.stringify(template))
    } else {
        // Create empty template object
        $scope.template = {
            type: Constants.Template.TYPE_FORM,
            name: '',
            fields: [],
            defaultData: {
                values: []
            }
        }
    }
    $scope.availableFieldTypes = Constants.Template.FORM_FIELD_TYPES

    // Add event listener
    // Event listener for Template validation success
    $scope.$on(Constants.Events.TEMPLATE_VALIDATE_SUCCESS, function (event, args) {
        // Emit success event if validation completes
        savePostValidation()
    })
})