/**
 * Created by Siddharth on 22-08-2017.
 */

app.controller("TaskManageCtrl", function ($scope, $http, $location, $localStorage, ExcelService) {

    $scope.init = function ()
    {
        $http({
            method:     'GET',
            url:        '/api/users/task',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            }
        })
            .then(
                function (response) {
                    console.log("Content View Info received")
                },
                function (error) {
                    console.log(error)
                    $location.path('/login')
                }
            )
    }
    
    $scope.excelRead = function (workbook) {
        ExcelService.excelRead(workbook).then(
            function (response) {
                // Excel File Parsed succesfully
                console.log(response.data)
            },
            function (error) {
                // Excel File Parse Error
                console.log(error)
            }
        )
    }

    $scope.excelError = function (e) {
        console.log("Excel Read Error = " + e)
    }

    // Init View
    $scope.init()
})
