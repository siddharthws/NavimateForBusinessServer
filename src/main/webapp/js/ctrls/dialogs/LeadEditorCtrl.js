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

        // Scroll to bottom
        scrollList($scope.leads.length)
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
        for (var  i = 0; i < $scope.leads.length; i++) {
            var lead = $scope.leads[i]

            if (!$scope.isLeadValid(lead)) {
                // Assert error flag
                $scope.bShowError = true

                // Scroll to this item
                scrollList(i)

                // Show error toast
                ToastService.toast("Please fill all fields in leads...")

                // Return
                return false
            }
        }

        return true
    }

    function scrollList(index) {
        // Get list and list item
        var list = $('.dialog-container .list-group')[0]
        var items = $(list).find('.list-group-item')
        var scrollingOffset = 0

        // Get Scrolling offset depending on index
        if (index >= items.length) {
            // Scroll to bottom
            scrollingOffset = $(document).height()
        } else if (index > 0) {
            // get relative position
            scrollingOffset = $(items[index]).offset().top - $(items[0]).offset().top
        }

        // Animate the list
        $(list).animate({scrollTop: scrollingOffset})
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
