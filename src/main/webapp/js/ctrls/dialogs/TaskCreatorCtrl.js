/**
 * Created by Siddharth on 12-09-2017.
 */

// Controller for Alert Dialog
app.controller('TaskCreatorCtrl', function ($scope, $rootScope, $http, $localStorage, $state, $mdDialog,
                                            ToastService, TeamDataService, LeadDataService, TemplateService, TaskDataService,
                                            taskIds, editCb) {

    /* ----------------------------- APIs --------------------------------*/
    // Button Click APIs
    $scope.add = function () {
        // Add empty task to array
        $scope.tasks.push({})

        // Update template data
        $scope.updateTemplate($scope.tasks.length - 1, 0)

        // Simulate a click on the new item
        $scope.listItemClick(task)
    }

    $scope.updateTemplate = function (taskIdx, templateIdx) {
        var task = $scope.tasks[taskIdx]
        var template = $scope.taskTemplates[templateIdx]

        // Ignore if same template is selected
        if (task.templateId == template.id) return

        // Create template data object from given template's default data
        var templateData = {}
        templateData.values = []
        template.fields.forEach(function (field) {
            if (field.type == Constants.Template.FIELD_TYPE_CHECKLIST ||
                field.type == Constants.Template.FIELD_TYPE_RADIOLIST) {
                templateData.values.push({
                    fieldId: field.id,
                    value: JSON.parse(JSON.stringify(field.value))
                })
            } else {
                templateData.values.push({
                    fieldId: field.id,
                    value: field.value
                })
            }
        })

        // Update lead's template data
        task.templateData = templateData
        task.templateId = template.id
    }

    $scope.getTemplateById = TemplateService.getById
    $scope.getFieldById = TemplateService.getFieldById

    $scope.listItemClick = function (task) {
        // Select this lead
        $scope.selectedTask = task
    }

    $scope.save = function () {
        // Validate Entered Data
        if (validate()) {
            $rootScope.showWaitingDialog("Please wait while task is created...")
            // Send to server for saving
            $http({
                method:     'POST',
                url:        '/api/users/task',
                headers:    {
                    'X-Auth-Token':    $localStorage.accessToken
                },
                data:       {
                    tasks:      $scope.tasks
                }
            }).then(
                function (response) {
                    $rootScope.hideWaitingDialog()
                    // Dismiss Dialog
                    $mdDialog.hide()

                    // Trigger callback
                    editCb()

                    // Show Toast
                    ToastService.toast("Tasks Created successfully...")
                },
                function (error) {
                    $rootScope.hideWaitingDialog()
                    // Show Error Toast
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
                if (!task.leadId || !task.repId || !task.formTemplateId)
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

    // Init data from services
    $scope.leads = LeadDataService.cache.data
    $scope.team = TeamDataService.cache.data
    $scope.formTemplates = TemplateService.getByType(Constants.Template.TYPE_FORM)
    $scope.taskTemplates = TemplateService.getByType(Constants.Template.TYPE_TASK)

    // Init tasks
    if (taskIds) {
        // Assign the passed leads & mark the first one as selected
        taskIds.forEach(function (taskId) {
            $scope.tasks.push(TaskDataService.getById(taskId))
        })

        // Update task templates
        $scope.tasks.forEach(function (task, i) {
            if (!task.templateId) {
                $scope.updateTemplate(i, 0)
            }
        })
    } else {
        $scope.tasks.push({})
        $scope.updateTemplate(0, 0)
    }
    $scope.selectedTask = $scope.tasks[0]
})
