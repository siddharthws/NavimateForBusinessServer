/**
 * Created by Siddharth on 15-09-2017.
 */

// Controller for Alert Dialog
app.controller('LeadEditorCtrl', function ( $scope, $rootScope, $mdDialog,
                                            ToastService, TemplateService, LeadService, DialogService,
                                            ObjLead, ObjValue,
                                            ids, cb) {
    /* ----------------------------- INIT --------------------------------*/
    var vm = this

    // Task List
    vm.leads = []
    vm.selectedLead = {}

    // Error / Waiting UI
    vm.bLoading = false
    vm.bLoadError = false
    vm.bInputError = false

    // Create array of lead template objects to send to dropdowns
    vm.templates = []
    TemplateService.getByType(Constants.Template.TYPE_LEAD).forEach(function (template) {
        vm.templates.push({id: template.id, name: template.name})
    })

    /* ----------------------------- APIs --------------------------------*/
    // Button Click APIs
    vm.add = function () {
        // Create empty task
        var lead = new ObjLead(
            null,
            null,
            "",
            "",
            0,
            0,
            null,
            [])

        // Add empty task to array
        vm.leads.push(lead)

        // Select this task
        vm.selectedLead = lead

        // Add template data
        vm.updateTemplate(0)
    }

    vm.copy = function () {
        // Clone selected lead
        var clonedLead = vm.selectedLead.Clone()

        // Remove ID
        clonedLead.id = null
        clonedLead.owner = null

        // Add to array
        vm.leads.push(clonedLead)

        // Mark as selected
        vm.selectedLead = clonedLead
    }

    vm.updateTemplate = function (idx) {
        // Get template by ID
        var id = vm.templates[idx].id
        var template = TemplateService.getById(id)

        // Ignore if same template is selected
        if (vm.selectedLead.template && vm.selectedLead.template.id == template.id) return

        // Update task template
        vm.selectedLead.template = template

        // Update template values
        vm.selectedLead.values = []
        template.fields.forEach(function (field) {
            // Create new value Object
            var value = new ObjValue(JSON.parse(JSON.stringify(field.value)), field)

            // Add to values
            vm.selectedLead.values.push(value)
        })
    }

    vm.save = function () {
        // Validate Entered Data
        if (validate()) {
            // Get editable leads
            var editableLeads = []
            vm.leads.forEach(function (lead) {
                if (lead.canEdit()) {editableLeads.push(lead)}
            })

            // Save in backend
            $rootScope.showWaitingDialog("Please wait while leads are edited...")
            LeadService.edit(editableLeads).then(
                // Success callback
                function () {
                    // Dismiss Dialog & notify user
                    $rootScope.hideWaitingDialog()
                    $mdDialog.hide()
                    ToastService.toast("Leads edited successfully...")

                    // Trigger callback
                    cb()
                },
                // Error callback
                function () {
                    // Show Error Toast
                    $rootScope.hideWaitingDialog()
                    ToastService.toast("Unable to edit leads...")
                }
            )
        }
    }

    vm.remove = function (idx) {
        // Close dialog if all items removed
        if (vm.leads.length == 1) {
            vm.close()
        }

        // Update selected task if required
        if (vm.selectedLead == vm.leads[idx]) {
            if (idx == vm.leads.length - 1) {
                vm.selectedLead = vm.leads[idx - 1]
            } else {
                vm.selectedLead = vm.leads[idx + 1]
            }
        }

        // Remove item from array
        vm.leads.splice(idx, 1)
    }

    vm.close = function () {
        $mdDialog.hide()
    }

    // Method to pick location using lcoation picker
    vm.pickLocation = function () {
        DialogService.locationPicker(vm.selectedLead.lat, vm.selectedLead.lng, function (address, lat, lng) {
            // Update lead details
            vm.selectedLead.address = address
            vm.selectedLead.lat = lat
            vm.selectedLead.lng = lng
        })
    }

    /* ----------------------------- Post INIT --------------------------------*/
    // API to validate entered data
    function validate () {
        // Reset error flag
        vm.bInputError = false

        // Validate each task
        for (var i = 0; i < vm.leads.length; i++) {
            if (vm.leads[i].canEdit() && !vm.leads[i].isValid()) {
                vm.bInputError = true
                return false
            }
        }

        return true
    }

    /* ----------------------------- INIT --------------------------------*/
    // Init tasks
    if (ids) {
        // Sync using service
        vm.bLoading = true
        LeadService.sync(ids).then(
            function () {
                vm.bLoading = false
                vm.leads = LeadService.cache
                vm.selectedLead = vm.leads[0]
            },
            function () {
                vm.bLoading = false
                vm.bLoadError = true
            }
        )
    } else {
        vm.add()
    }
})
