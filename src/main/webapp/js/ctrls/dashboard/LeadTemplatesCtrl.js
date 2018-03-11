/**
 * Created by Siddharth on 15-12-2017.
 */

app.controller("LeadTemplatesCtrl", function (  $scope, $rootScope, $http, $localStorage, $state,
                                                DialogService, ToastService, TemplateService) {
    var vm = this

    /*------------------------------- Scope APIs -------------------------------*/
    vm.edit = function () {
        var selectedItems = vm.getSelectedItems()
        if (selectedItems.length != 1) {
            ToastService.toast("Please select a single template to edit...")
        } else {
            DialogService.leadTemplateEditor(selectedItems[0], function () {
                TemplateService.sync().then(init)
            })
        }
    }

    vm.create = function () {
        DialogService.leadTemplateEditor(null, function () {
            TemplateService.sync().then(init)
        })
    }

    // Full List Selection Toggling
    vm.toggleAll = function () {
        for (var i = 0; i < vm.selection.length; i++) {
            vm.selection[i] = vm.bCheckAll
        }
    }

    // API to get selected items
    vm.getSelectedItems = function () {
        var selectedItems = []
        for (var i = 0; i < vm.selection.length; i++) {
            if (vm.selection[i]) {
                selectedItems.push(vm.templates[i])
            }
        }
        return selectedItems
    }

    //API to delete templates
    vm.remove = function () {
        DialogService.confirm("Are you sure you want to remove these " + vm.getSelectedItems().length + " templates ?",
            function () {
                //creating array for template array
                var templateId = []
                vm.getSelectedItems().forEach(function (template) {
                    templateId.push(template.id)
                })

                //http call to close tasks
                $rootScope.showWaitingDialog("Removing Templates...")
                $http({
                    method: 'POST',
                    url:    '/api/admin/removeTemplates',
                    headers: {
                        'X-Auth-Token': $localStorage.accessToken
                    },
                    data: {
                        templateIds : templateId
                    }
                }).then(
                    function (response) {
                        $rootScope.hideWaitingDialog()
                        // Show Toast
                        ToastService.toast("Templates removed successfully...")

                        // Re-sync Template data since template has been removed
                        TemplateService.sync().then(init)
                    },
                    function (error) {
                        $rootScope.hideWaitingDialog()
                        ToastService.toast(error.data.error)
                    })
            })
    }

    /*------------------------------- Local APIs -------------------------------*/

    function init() {
        // Get Template Data
        vm.templates = TemplateService.getByType(Constants.Template.TYPE_LEAD)

        // Re-Init selection array with all unselected
        vm.selection = []
        if(vm.templates) {
            vm.templates.forEach(function () {
                vm.selection.push(false)
            })
        }
    }

    /*------------------------------- INIT -------------------------------*/
    // Set menu and option
    $scope.nav.item       = Constants.DashboardNav.Menu[Constants.DashboardNav.ITEM_TEMPLATES]
    $scope.nav.option     = Constants.DashboardNav.Options[Constants.DashboardNav.OPTION_LEAD]

    // Init Object
    vm.selection = []
    vm.templates = []
    vm.bCheckAll = false

    // Sync templates if required
    if (!TemplateService.cache.length) {
        TemplateService.sync().then(init)
    } else {
        init()
    }
})
