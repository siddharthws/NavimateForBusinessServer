/**
 * Created by Siddharth on 04-03-2018.
 */

app.controller('ibFilepickerCtrl', function ($scope, $timeout) {
    $scope.pickFile = function () {
        // Perform click on hidden input element
        $timeout(function () {
            // Reset file value
            $('#file-picker')[0].value = ""

            // Perform click event on picker
            $('#file-picker')[0].click()
        }, 0)
    }
})
