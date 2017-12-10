/**
 * Created by Siddharth on 29-11-2017.
 */

// Controller for Alert Dialog
app.controller('FormTemplateEditorCtrl', function ($scope, $rootScope, $mdDialog, $http, $localStorage, ToastService, template, updateCb) {
    var vm = this

    /* ------------------------------- HTML APIs -----------------------------------*/
    // Close dialog
    vm.close = function () {
        $mdDialog.hide()
    }

    // API to save template
    vm.save = function () {
        if ($scope.callbacks.validateTemplate()) {
            $rootScope.showWaitingDialog("Please wait while template is being saved...")
            $http({
                method:     'POST',
                url:        '/api/users/formTemplate',
                headers:    {
                    'X-Auth-Token':    $localStorage.accessToken
                },
                data: {
                    'template': $scope.template
                }
            })
                .then(
                    function (response) {
                        // Hide dialog and show toast
                        $rootScope.hideWaitingDialog()
                        $mdDialog.hide()
                        ToastService.toast("Form saved successfully...")

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
    }

    /* ------------------------------- Local APIs -----------------------------------*/
    /* ------------------------------- Init -----------------------------------*/
    // Attach template to scope to pass to child template editor view
    $scope.template = template
    if (!$scope.template) {
        // Create empty template object
        $scope.template = {
            name: '',
            fields: [],
            defaultData: {
                values: []
            }
        }
    }

    // Attach Callbacks
    $scope.callbacks = {}
})