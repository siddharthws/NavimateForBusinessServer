/**
 * Created by Siddharth on 15-12-2017.
 */

// Controller for Lead Template Editor Dialog
app.controller('LeadTemplateEditorCtrl', function ( $scope, $rootScope, $mdDialog,
                                                    ToastService, ObjTemplate, TemplateService,
                                                    template, cb) {
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
        var templates = []
        templates.push($scope.template)
        TemplateService.edit(templates).then(
            // Success callback
            function () {
                // Hide dialog and show toast
                $rootScope.hideWaitingDialog()
                $mdDialog.hide()
                ToastService.toast("Template saved successfully...")

                // Trigger callback
                cb()
            },
            // Error callback
            function () {
                // Hide waiting and show error toast
                $rootScope.hideWaitingDialog()
                ToastService.toast("Unable to save template")
            }
        )
    }

    /* ------------------------------- Init -----------------------------------*/
    // Attach template to scope to pass to child template editor view
    if (template) {
        // CLone template to avoid manipulating original object
        $scope.template = template.Clone()
    } else {
        // Create empty template object
        $scope.template = new ObjTemplate(null, "", Constants.Template.TYPE_LEAD, [])
    }
    $scope.availableFieldTypes = Constants.Template.LEAD_FIELD_TYPES

    // Add event listener
    // Event listener for Template validation success
    $scope.$on(Constants.Events.TEMPLATE_VALIDATE_SUCCESS, function (event, args) {
        // Emit success event if validation completes
        savePostValidation()
    })
})
