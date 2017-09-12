/**
 * Created by Siddharth on 22-08-2017.
 */

app.controller("TaskManageCtrl", function ($scope, $http, $localStorage, $state, DialogService) {

    // Set menu and option
    $scope.selection.item       = MENU_ITEMS[MENU_ITEM_TASKS]
    $scope.selection.option     = ITEM_OPTIONS[ITEM_OPTION_MANAGE]

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
    
    /*-------------------------------- APIs --------------------------------*/
    $scope.add = function () {
        // Launch Task Creator dialog
        DialogService.taskCreator()
    }
})
