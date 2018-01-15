/**
 * Created by Siddharth on 15-12-2017.
 */

// Controller for Lead Template Editor Dialog
app.controller('LeadTemplateEditorCtrl', function ($scope, $rootScope, $mdDialog, $http, $localStorage, ToastService, template, updateCb) {
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

                // Trigger Callback
                updateCb()
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
    $scope.template = template
    $scope.availableFieldTypes = Constants.Template.LEAD_FIELD_TYPES
    if (!$scope.template) {
        // Create empty template object
        $scope.template = {
            name: '',
            type: Constants.Template.TYPE_LEAD,
            fields: [],
            defaultData: {
                values: []
            }
        }
    }

    // Add event listener
    // Event listener for Template validation success
    $scope.$on(Constants.Events.TEMPLATE_VALIDATE_SUCCESS, function (event, args) {
        // Emit success event if validation completes
        savePostValidation()
    })
})