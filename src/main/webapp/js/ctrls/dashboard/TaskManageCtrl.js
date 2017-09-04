/**
 * Created by Siddharth on 22-08-2017.
 */

app.controller("TaskManageCtrl", function ($scope, ExcelService, DialogService) {

    $scope.excelRead = function (workbook) {
        ExcelService.excelRead(workbook).then(
            function (response) {
                // Launch Map Editor Dialod
                DialogService.mapEditor(response.data)
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
})
