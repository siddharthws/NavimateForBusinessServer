/**
 * Created by Siddharth on 07-09-2017.
 */

// Controller for Alert Dialog
app.controller('AddRepCtrl', function ($scope, $http, $localStorage, $mdDialog) {

    /*------------------------------- INIT -----------------------------------*/
    /*------------------------------- APIs -----------------------------------*/
    // Button Click APIs
    $scope.add = function () {
        $http({
            method:     'POST',
            url:        '/api/users/team',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            },
            data:       {
                name:           $scope.name,
                phoneNumber:    $scope.phoneNumber
            }
        })
        .then(
            function (response) {
                $mdDialog.hide()
            },
            function (error) {
                console.log("Unable to add rep : " + error)
            })
    }

    $scope.cancel = function () {
        $mdDialog.hide()
    }
})
