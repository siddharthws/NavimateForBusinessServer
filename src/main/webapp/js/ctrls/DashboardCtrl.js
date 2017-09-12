/**
 * Created by Siddharth on 23-08-2017.
 */

app.controller('DashboardCtrl', function ($scope, $state, $localStorage, AuthService) {

    /*------------------------------------ INIT --------------------------------*/
    // Menu Selection Parameters
    $scope.selection = {}

    /*------------------------------------ APIs --------------------------------*/
    // Button Click APIs
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

    $scope.onOptionClick = function (option) {
        // Prepare State URL
        var state = "dashboard." + $scope.selection.item.name + "-" + option.name

        // Update State to this option
        $state.go(state)
    }
})
