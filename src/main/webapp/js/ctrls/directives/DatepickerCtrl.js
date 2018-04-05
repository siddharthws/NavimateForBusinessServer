/**
 * Created by Siddharth on 19-02-2018.
 *
 * Controller for Date Picker Directive
 */

app.controller('datepickerCtrl', function ($scope, $timeout) {
    /* ------------------------------ INIT --------------------------------- */
    $scope.rawDate = null

    /* ------------------------------ Public APIs --------------------------------- */
    $scope.dateChanged = function () {
        // Update date model to required format
        $scope.dateModel = moment($scope.rawDate.toString()).format($scope.modelFormat)

        // Trigger callback on next digest cycle
        $timeout(function () {
            $scope.dateChange()
        })
    }

    /* ------------------------------ Private APIs --------------------------------- */
    // Update displayed text using date model
    function updateFromModel () {
        if ($scope.dateModel) {
            // Update display text in required format
            $scope.displayText = moment($scope.dateModel, $scope.modelFormat).format($scope.displayFormat)
        } else {
            // Update display text to empty
            $scope.displayText = ""
        }
    }

    /* ------------------------------ Listeners --------------------------------- */
    $scope.$watch('dateModel', function () {
        updateFromModel()
    })

    /* ------------------------------ Post INIT --------------------------------- */
    // Update display text from model
    updateFromModel()
})
