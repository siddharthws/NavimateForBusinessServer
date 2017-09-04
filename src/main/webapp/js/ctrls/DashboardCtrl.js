/**
 * Created by Siddharth on 23-08-2017.
 */

app.controller('DashboardCtrl', function ($scope, $state, $localStorage, AuthService) {

    $scope.logout = function(){
        AuthService.logout()
            .then(
                function (response) {
                    $localStorage.accessToken = ""
                    $state.go("home")
                },
                function (error) {
                    console.log(error)
                }
            )
    }
})
