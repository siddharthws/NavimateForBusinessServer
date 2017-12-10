/**
 * Created by Siddharth on 12-09-2017.
 */

// Controller for Alert Dialog
app.controller('TaskCreatorCtrl', function ($scope, $rootScope, $http, $localStorage, $state, $mdDialog, ToastService, taskAddedCb, tasks) {

    /* ----------------------------- INIT --------------------------------*/
    $scope.tasks = tasks
    $scope.leads = []
    $scope.team = []
    $scope.formTemplates = []

    $rootScope.showWaitingDialog("Please wait while we are fetching information..")
    // Get all leads
    $http({
        method:     'GET',
        url:        '/api/users/lead',
        headers:    {
            'X-Auth-Token':    $localStorage.accessToken
        }
    })
    .then(
        function (response) {
            $rootScope.hideWaitingDialog()
            $scope.leads = response.data
        },
        function (error) {
            $rootScope.hideWaitingDialog()
            console.log(error)
        }
    )

    // Get Team
    $http({
        method:     'GET',
        url:        '/api/users/team',
        headers:    {
            'X-Auth-Token':    $localStorage.accessToken
        }
    })
    .then(
        function (response) {
            $scope.team = response.data
        },
        function (error) {
            console.log(error)
        }
    )

    // Get Form Templates
    $http({
        method:     'GET',
        url:        '/api/users/formTemplate',
        headers:    {
            'X-Auth-Token':    $localStorage.accessToken
        }
    })
    .then(
        function (response) {
            $scope.formTemplates = response.data.templates
        },
        function (error) {
            console.log(error)
        }
    )

    /* ----------------------------- APIs --------------------------------*/
    // Button Click APIs
    $scope.add = function () {
        // Add empty task to array
        $scope.tasks.push({})
    }

    $scope.create = function () {
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
                    tasks:      JSON.stringify($scope.tasks)
                }
            })
            .then(
                function (response) {
                    $rootScope.hideWaitingDialog()
                    // Dismiss Dialog
                    $mdDialog.hide()

                    // Show Toast
                    ToastService.toast("Tasks Created successfully...")

                    // Trigger callback
                    taskAddedCb()
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
                if (!task.lead || !task.rep || !task.template)
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
})
