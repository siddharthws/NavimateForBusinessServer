/**
 * Created by Siddharth on 19-03-2018.
 */

// Controller for Alert Dialog
app.controller('TaskEditorCtrl', function ( $scope, $rootScope, $mdDialog, $localStorage,
                                            ToastService, TemplateService, TaskService, DialogService, TableService, TeamService, LeadService,
                                            ObjTask, ObjValue,
                                            taskIds, cb) {
    /* ----------------------------- INIT --------------------------------*/
    var vm = this

    // Task List
    vm.tasks = []
    vm.selectedTask = {}

    // Error / Waiting UI
    vm.bLoading = false
    vm.bLoadError = false
    vm.bInputError = false

    // Create array of form and task template objects to send to dropdowns
    vm.formTemplates = []
    TemplateService.getByType(Constants.Template.TYPE_FORM).forEach(function (formTemplate) {
        vm.formTemplates.push({id: formTemplate.id, name: formTemplate.name})
    })

    vm.taskTemplates = []
    TemplateService.getByType(Constants.Template.TYPE_TASK).forEach(function (taskTemplate) {
        vm.taskTemplates.push({id: taskTemplate.id, name: taskTemplate.name})
    })

    // Create array of manager objects to send to dropdown
    vm.managers = []
    vm.managers.push({id: $localStorage.id, name: $localStorage.name})
    TeamService.managers.forEach(function (manager) {
        vm.managers.push({id: manager.id, name: manager.name})
    })

    /* ----------------------------- APIs --------------------------------*/
    // Button Click APIs
    vm.add = function () {
        // Create empty task
        var task = new ObjTask( null,
                                null,
                                null,
                                {id: $localStorage.id, name: $localStorage.name},
                                null,
                                Constants.Task.STATUS_OPEN,
                                0,
                                null,
                                null,
                                [])

        // Add empty task to array
        vm.tasks.push(task)

        // Select this task
        vm.selectedTask = task

        // Add form template
        vm.updateFormTemplate(0)

        // Add template data
        vm.updateTemplate(0)
    }

    vm.copy = function () {
        // Clone selected lead
        var clonedTask = vm.selectedTask.Clone()

        // Remove ID
        clonedTask.id = null

        // Add to array
        vm.tasks.push(clonedTask)

        // Mark as selected
        vm.selectedTask = clonedTask
    }

    vm.updateManager = function (idx) {
        var id = vm.managers[idx].id
        if (id == $localStorage.id) {
            vm.selectedTask.manager = {id: $localStorage.id, name: $localStorage.name}
        } else {
            var manager = TeamService.getManagerById(id)
            vm.selectedTask.manager = {id: manager.id, name: manager.name}
        }
    }

    vm.updateFormTemplate = function (idx) {
        var id = vm.formTemplates[idx].id
        vm.selectedTask.formTemplate = TemplateService.getById(id)
    }

    vm.updateTemplate = function (idx) {
        // Get template by ID
        var id = vm.taskTemplates[idx].id
        var template = TemplateService.getById(id)

        // Ignore if same template is selected
        if (vm.selectedTask.template && vm.selectedTask.template.id == template.id) return

        // Update task template
        vm.selectedTask.template = template

        // Update template values
        vm.selectedTask.values = []
        template.fields.forEach(function (field) {
            // Create new value Object
            var value = new ObjValue(JSON.parse(JSON.stringify(field.value)), field)

            // Add to values
            vm.selectedTask.values.push(value)
        })
    }

    vm.save = function () {
        // Validate Entered Data
        if (validate()) {
            $rootScope.showWaitingDialog("Please wait while task is created...")
            TaskService.edit(vm.tasks).then(
                // Success callback
                function () {
                    // Dismiss Dialog & notify user
                    $rootScope.hideWaitingDialog()
                    $mdDialog.hide()
                    ToastService.toast("Tasks Created successfully...")

                    // Trigger callback
                    cb()
                },
                // Error callback
                function () {
                    // Show Error Toast
                    $rootScope.hideWaitingDialog()
                    ToastService.toast("Unable to create tasks...")
                }
            )
        }
    }

    vm.remove = function (idx) {
        // Close dialog if all items removed
        if (vm.tasks.length == 1) {
            vm.close()
        }

        // Update selected task if required
        if (vm.selectedTask == vm.tasks[idx]) {
            if (idx == vm.tasks.length - 1) {
                vm.selectedTask = vm.tasks[idx - 1]
            } else {
                vm.selectedTask = vm.tasks[idx + 1]
            }
        }

        // Remove item from array
        vm.tasks.splice(idx, 1)
    }

    vm.close = function () {
        $mdDialog.hide()
    }

    /*
     * Pick and View methods
     */
    vm.pickLead = function () {
        DialogService.table2Picker("Pick Lead", LeadService.table, function (id, name) {
            vm.selectedTask.lead = {id: id, name: name}
        })
    }
    
    vm.viewLead = function () {
        DialogService.leadViewer(vm.selectedTask.lead.id)
    }

    vm.pickRep = function () {
        DialogService.tablePicker("Pick Representative", TableService.teamTable, function (id, name) {
            vm.selectedTask.rep = {id: id, name: name}
        })
    }

    vm.viewRep = function () {
        DialogService.teamViewer(vm.selectedTask.rep.id)
    }

    /* ----------------------------- Post INIT --------------------------------*/
    // API to validate entered data
    function validate () {
        // Reset error flag
        vm.bInputError = false

        // Validate each task
        for (var i = 0; i < vm.tasks.length; i++) {
            if (!vm.tasks[i].isValid()) {
                vm.bInputError = true
                return false
            }
        }

        return true
    }

    /* ----------------------------- INIT --------------------------------*/
    // Init tasks
    if (taskIds) {
        // Sync using service
        vm.bLoading = true
        TaskService.sync(taskIds).then(
            function () {
                vm.bLoading = false
                vm.tasks = TaskService.cache
                vm.selectedTask = vm.tasks[0]
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
