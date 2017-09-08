/**
 * Created by Siddharth on 22-08-2017.
 */

app.controller("TaskManageCtrl", function ($scope, $http, $localStorage, $state, DialogService) {

    $http({
        method:     'GET',
        url:        '/api/users/task',
        headers:    {
            'X-Auth-Token':    $localStorage.accessToken
        }
    })
    .then(
        function (response) {
            $scope.tasks = response.data
        },
        function (error) {
            console.log(error)
            $state.go('home')
        }
    )

    $scope.add = function () {
        DialogService.leadSelector()
    }
})
