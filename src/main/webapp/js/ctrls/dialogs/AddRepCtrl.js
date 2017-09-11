/**
 * Created by Siddharth on 11-09-2017.
 */

// Controller for Alert Dialog
app.controller('AddRepCtrl', function ($scope, $mdDialog, ToastService) {

    $scope.add = function () {
        if (validate()) {
            // Placeholder for success validation
        }
    }

    $scope.cancel = function () {
        $mdDialog.hide()
    }

    // API to validate entered data
    function validate () {
        var bValid = true

        // Reset Error Fields
        $scope.bNameError = false
        $scope.bPhoneError = false

        // Validate Name
        if (!$scope.name) {
            bValid = false
            $scope.bNameError = true
        }

        // Validate number
        if (!$scope.phoneNumber) {
            bValid = false
            $scope.bPhoneError = true
        }

        if (!bValid) {
            ToastService.toast("Please fill all fields.")
        }

        return bValid
    }
})
