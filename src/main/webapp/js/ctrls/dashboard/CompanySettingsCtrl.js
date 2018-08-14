/**
 * Created by Chandel on 06-02-2018.
 */

app.controller("CompanySettingsCtrl", function ($scope, $localStorage ,$rootScope, $http, DialogService, $filter,
                                                ToastService, NavService) {
    /*------------------------------------ INIT --------------------------------*/
    var vm = this

    // Set Active Tab and Menu
    NavService.setActive(NavService.company, 0)

    vm.message = {
        hour: 'Hour is required',
        minute: 'Minute is required',
        meridiem: 'Meridiem is required'
    }

    //get company details from local storage
    vm.companyName      = $localStorage.companyName
    vm.apiKey           = $localStorage.apiKey
    vm.companySize      = $localStorage.companySize

    vm.image            = ""
    /*-------------------------------- Scope APIs --------------------------------*/
    vm.update = function () {
        //set day start and day end
        var startHr = vm.dayStart.getHours()
        var endHr = vm.dayEnd.getHours()

        //check for valid selection of working hours
        if (startHr >= endHr) {
            ToastService.toast("Day End must be greater than Day Start!!!")
        } else {
            $rootScope.showWaitingDialog("Please wait while settings are updating...")
            $http({
                method: 'POST',
                url: '/api/admin/accSettings',
                headers: {
                    'X-Auth-Token': $localStorage.accessToken
                },
                data: {
                    startHr: startHr,
                    endHr: endHr
                }
            }).then(
                function (response) {
                    $localStorage.startHr = startHr
                    $localStorage.endHr = endHr

                    initTime()

                    $rootScope.hideWaitingDialog()
                    ToastService.toast("Settings Successfully Updated!!!")
                },
                function (error) {
                    $rootScope.hideWaitingDialog()
                    ToastService.toast("Failed to Update settings!!!")
                })
        }
    }

    vm.uploadCompanyIcon = function (image) {

        // Perform import
        ImageUploadService.import("/api/photos/uploadCompanyIcon", image).then(
            // Success callback
            function () {
                // Notify user about success
                ToastService.toast("Image Uploaded successfully...")
            },
            // Error callback
            function (message) {
                // Notify user about error
                DialogService.alert("Upload Error : " + message)
            }
        )
    }
    /*-------------------------------- Private APIs --------------------------------*/
    function initTime() {
        //get working hours from local storage
        vm.dayStart = new Date(0,0,0,$localStorage.startHr,0,0,0)
        vm.dayEnd = new Date(0,0,0,$localStorage.endHr,0,0,0)
    }

    /*-------------------------------- Post Init --------------------------------*/
    initTime()
});