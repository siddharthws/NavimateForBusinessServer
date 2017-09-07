/**
 * Created by Siddharth on 06-09-2017.
 */

// Controller for Task Creator Dialog
app.controller('TaskCreatorCtrl', function ($scope, $http, $localStorage, $state, leads) {

    /* ----------------------------- INIT --------------------------------*/
    $scope.tasks = []
    $scope.leads = []
    $scope.team = []
    $scope.formTemplates = []

    // Create Tasks from Leads (empty rep and template)
    leads.forEach(function (lead) {
        var task = {
            lead:       lead
        }
        $scope.tasks.push(task)
    })

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
        // Check if any entries are null
        $scope.tasks.forEach(function (task) {
            if (!task.lead || !task.rep || !task.template)
            {
                console.log("Empty Entries found")
                return
            }
        })

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
                $scope.leads = response.data
            },
            function (error) {
                console.log(error)
                $state.go('home')
            }
        )
    }
})
