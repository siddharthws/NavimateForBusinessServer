/**
 * Created by Siddharth on 11-09-2017.
 */

// Controller for Alert Dialog
app.controller('AddRepCtrl', function ( $scope, $rootScope, $http, $localStorage, $mdDialog,
                                        ToastService, TeamService,
                                        cb) {

    $scope.add = function () {
        if (validate()) {
            // Get Phone Number with country code
            var phone
            if ($scope.countryCode) {
                phone = '+' + $scope.countryCode + $scope.phoneNumber
            }
            else {
                phone = "+91" + $scope.phoneNumber
            }

            // Get Email
            var email = $scope.email ? $scope.email : "";

            $rootScope.showWaitingDialog("Please wait while we are adding rep...")
            // Send Register Request
            $http({
                method:     'POST',
                url:        '/api/users/team',
                headers:    {
                    'X-Auth-Token':    $localStorage.accessToken
                },
                data:       {
                    name:           $scope.name,
                    phoneNumber:    phone,
                    email:          email
                }
            })
            .then(
                function (response) {
                    $rootScope.hideWaitingDialog()
                    // Hide dialog and show toast
                    $mdDialog.hide()
                    ToastService.toast("An SMS has been sent to the rep...")

                    // Trigger Callback
                    cb()
                },
                function (error) {
                    $rootScope.hideWaitingDialog()
                    ToastService.toast("Unable to add rep")
                })
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
