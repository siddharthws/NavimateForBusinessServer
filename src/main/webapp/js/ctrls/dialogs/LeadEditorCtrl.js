/**
 * Created by Siddharth on 15-09-2017.
 */

// Controller for Alert Dialog
app.controller('LeadEditorCtrl', function ($scope, $mdDialog, ToastService, leads) {

    /* ------------------------------- Scope APIs -----------------------------------*/
    $scope.add = function () {
        // Add empty lead to array
        var lead = {}
        $scope.leads.push(lead)

        // Select the newly added lead
        $scope.selectedLead = lead
    }

    $scope.listItemClick = function (lead) {
        $scope.selectedLead = lead
    }

    $scope.upload = function () {
        // Placeholder
    }

    $scope.save = function () {
        // Validate entered leads
        if (validate()) {
            // Data Validated. Save in database
        }
    }

    $scope.isLeadValid = function (lead) {
        return (lead.company && lead.name && lead.phoneNumber && lead.latitude && lead.longitude && lead.address)
    }

    $scope.cancel = function () {
        $mdDialog.hide()
    }

    /* ------------------------------- Script APIs -----------------------------------*/
    function validate() {
        // Reset Error flag
        $scope.bShowError = false

        // Validate leads
        $scope.leads.forEach(function (lead) {
            if (!$scope.isLeadValid(lead)) {
                $scope.bShowError = true
            }
        })
    }

    /* ------------------------------- INIT -----------------------------------*/
    // Init objects
    $scope.leads = []
    $scope.selectedLead = {}
    $scope.bShowError = false

    if (leads) {
        // Assign the passed leads & mark the first one as selected
        $scope.leads = leads
        $scope.selectedLead = $scope.leads[0]
    } else {
        // Add new
        $scope.add()
    }
})
