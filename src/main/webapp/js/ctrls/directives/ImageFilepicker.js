/**
 * Created by Rohan on 6/15/2018.
 */

app.controller('imageFilePickerCtrl', function ($scope, $timeout) {
    $scope.pickImage = function () {
        // Perform click on hidden input element
        $timeout(function () {
            // Reset file value
            $('#image-picker')[0].value = ""

            // Perform click event on picker
            $('#image-picker')[0].click()
        }, 0)
    }
})