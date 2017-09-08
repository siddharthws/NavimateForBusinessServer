/**
 * Created by Siddharth on 06-09-2017.
 */

app.controller('LeadSelectorCtrl', function ($scope, $http, $localStorage, $mdDialog, DialogService, ExcelService) {

    /* ----------------------------- INIT --------------------------------*/
    $scope.selection = []

    // Get List of leads for this user
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
            // Launch Alert
            DialogService.alert("Unable to fetch leads !!!")
        }
    )

    /* ----------------------------- APIs --------------------------------*/
    // API to toggle checkbox selection
    $scope.toggleSelection = function (id) {
        var idx = $scope.selection.indexOf(id)

        // If present, remove
        if (idx > -1) {
            $scope.selection.splice(idx, 1)
        }
        // If not present, add
        else{
            $scope.selection.push(id)
        }
    }

    // Button Click APIs
    $scope.next = function () {
        var selectedLeads = []
        $scope.leads.forEach(function (lead) {
            // Check if element is present in selection
            if ($scope.selection.indexOf(lead.id) > -1) {
                // Add to selected leads
                selectedLeads.push(lead)
            }
        })

        // Pass to task creator dialog
        DialogService.taskCreator(selectedLeads)
    }

    $scope.cancel = function () {
        $mdDialog.hide()
    }

    // Excel related APIs
    $scope.excelRead = function (workbook) {
        ExcelService.excelRead(workbook).then(
            function (response) {
                // Launch Map Editor Dialog
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
