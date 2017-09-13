/**
 * Created by Siddharth on 12-09-2017.
 */

// Controller for Alert Dialog
app.controller('TaskCreatorCtrl', function ($scope, $http, $localStorage, $state, $mdDialog, ToastService) {

    /* ----------------------------- INIT --------------------------------*/
    $scope.tasks = [{}]
    $scope.leads = []
    $scope.team = []
    $scope.formTemplates = []

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
            $scope.leads = response.data
        },
        function (error) {
            console.log(error)
            $state.go('home')
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
            $state.go('home')
        }
    )

    // Get Form Templates
    $http({
        method:     'GET',
        url:        '/api/users/form',
        headers:    {
            'X-Auth-Token':    $localStorage.accessToken
        }
    })
    .then(
        function (response) {
            $scope.formTemplates = response.data
        },
        function (error) {
            console.log(error)
            $state.go('home')
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
                    // Dismiss Dialog
                    $mdDialog.hide()

                    // Show Toast
                    ToastService.toast("Tasks Created successfully...")
                },
                function (error) {
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
