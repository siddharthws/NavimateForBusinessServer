/**
 * Created by Siddharth on 22-08-2017.
 */

app.controller("TeamManageCtrl", function ($scope, $http, $localStorage, $state, DialogService) {

    /*--------------------------------- INIT -----------------------------------*/
    // Get team for this user
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

    /*--------------------------------- APIs -----------------------------------*/
    $scope.add = function () {
        // Show Add rep dialog
        DialogService.addRep()
    }
})
