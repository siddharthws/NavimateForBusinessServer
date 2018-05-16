/**
 * Created by Siddharth on 19-02-2018.
 *
 * Controller for Date Picker Directive
 */

app.controller('datepickerCtrl', function ($scope, $timeout, DialogService) {
    /* ------------------------------ INIT --------------------------------- */
    $scope.rawDate = null

    /* ------------------------------ Public APIs --------------------------------- */
    $scope.pickDate = function () {
        // Launch Date Time Picker dialog with callback
        DialogService.dateTimePicker($scope.rawDate, handleDateChange)
    }

    /* ------------------------------ Private APIs --------------------------------- */
    // Callback to handle a date change event
    function handleDateChange(date) {
        // Update cache
        $scope.rawDate = date

        // Update model
        if ($scope.rawDate) {
            $scope.dateModel = $scope.rawDate.format($scope.modelFormat)
        } else {
            $scope.dateModel = null
        }

        // Trigger callback on next digest cycle
        $timeout(function () {
            $scope.dateChange()
        })
    }

    /* ------------------------------ Listeners --------------------------------- */
    /* ------------------------------ Post INIT --------------------------------- */
    // Init raw date from date model
    if ($scope.dateModel) {
        $scope.rawDate = moment($scope.dateModel, $scope.modelFormat).toDate()
    }
})
