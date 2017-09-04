/**
 * Created by Siddharth on 23-08-2017.
 */

app.controller('DashboardCtrl', function ($scope, $location, $localStorage, AuthService) {

    $scope.logout = function(){
        AuthService.logout()
            .then(
                function (response) {
                    $localStorage.accessToken = ""
                    $location.path("")
                },
                function (error) {
                    console.log(error)
                }
            )
    }
})
