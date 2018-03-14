/**
 * Created by Siddharth on 12-09-2017.
 */

// Controller for Alert Dialog
app.controller('TaskCreatorCtrl', function ($scope, $rootScope, $http, $localStorage, $state, $mdDialog,
                                            ToastService, TeamDataService, LeadDataService, TemplateService, TaskService,
                                            ObjTask, ObjValue,
                                            taskIds, editCb) {

    /* ----------------------------- APIs --------------------------------*/
    // Button Click APIs
    $scope.add = function () {
        // Create empty task
        var task = new ObjTask(null, null, null, Constants.Task.STATUS_OPEN, 0, $scope.formTemplates[0], $scope.taskTemplates[0], [])

        // Add empty task to array
        $scope.tasks.push(task)

        // Update template data
        $scope.updateTemplate($scope.tasks.length - 1, 0)

        // Select this task
        $scope.selectedTask = task
    }

    $scope.updateTemplate = function (taskIdx, templateIdx) {
        var task = $scope.tasks[taskIdx]
        var template = $scope.taskTemplates[templateIdx]

        // Ignore if same template is selected
        if (task.template.id == template.id) return

        // Update task template
        task.template = template

        // Create values based on field default values
        task.values = []
        template.fields.forEach(function (field) {
            // Add value object to task
            task.values.push(new ObjValue(JSON.parse(JSON.stringify(field.value)), field))
        })
    }

    $scope.save = function () {
        // Validate Entered Data
        if (validate()) {
            $rootScope.showWaitingDialog("Please wait while task is created...")
            TaskService.edit($scope.tasks).then(
                // Success callback
                function () {
                    // Dismiss Dialog & notify user
                    $rootScope.hideWaitingDialog()
                    $mdDialog.hide()
                    ToastService.toast("Tasks Created successfully...")

                    // Trigger callback
                    editCb()
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

    $scope.remove = function (task) {
        // Get object index
        var idx = $scope.tasks.indexOf(task)

        // Remove from list
        if (idx >= 0) {
            $scope.tasks.splice(idx, 1)
        }
    }

    $scope.cancel = function () {
        $mdDialog.hide()
    }

    // API to validate entered data
    function validate () {
        var bValid = true

        // Reset Error UI
        $scope.bShowError = false

        // Check if any tasks are present
        if ($scope.tasks.length == 0) {
            ToastService.toast("Add some tasks to create...")
            bValid = false
        }
        // Validate Task data
        else {
            $scope.tasks.forEach(function (task) {
                if (!task.lead || !task.rep || !task.formTemplate || !task.template)
                {
                    bValid = false
                    $scope.bShowError = true
                }
            })

            if (!bValid) {
                ToastService.toast("Please fill all fields")
            }
        }

        return bValid
    }

    /* ----------------------------- INIT --------------------------------*/
    $scope.tasks = []
    $scope.selectedTask = {}
    $scope.bShowError = false
    $scope.bError = false
    $scope.bWaiting = false

    // Init data from services
    $scope.leads = LeadDataService.cache.data
    $scope.team = TeamDataService.cache.data
    $scope.formTemplates = TemplateService.getByType(Constants.Template.TYPE_FORM)
    $scope.taskTemplates = TemplateService.getByType(Constants.Template.TYPE_TASK)

    // Init tasks
    if (taskIds) {
        // Sync using service
        $scope.bWaiting = true
        TaskService.sync(taskIds).then(
            function () {
                $scope.bWaiting = false
                $scope.tasks = TaskService.cache
                $scope.selectedTask = $scope.tasks[0]
            },
            function () {
                $scope.bWaiting = false
                $scope.bError = true
            }
        )
    } else {
        $scope.add()
    }

})
