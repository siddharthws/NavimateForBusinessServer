/**
 * Created by Siddharth on 19-02-2018.
 *
 * Controller for Date Picker Directive
 */

app.controller('datepickerCtrl', function ($scope, $filter) {
    /* ------------------------------ INIT --------------------------------- */
    var vm = this

    // Init function triggered after linking is complete
    vm.init = function () {
        // Init formatted date if initialized with valid date
        if (vm.pickedDate) {
            vm.formattedDate = $filter('date')(new Date(vm.pickedDate), 'dd/MM')
        }
    }

    /* ------------------------------ Public APIs --------------------------------- */
    // Method to handle new date picked by user
    vm.datePicked = function () {
        // Get date in different formats
        vm.pickedDate = $filter('date')(vm.rawDate, 'yyyy-MM-dd HH:mm:ss')
        vm.formattedDate = $filter('date')(vm.rawDate, 'dd/MM')

        // Trigger date change callback
        $scope.dateChange({pickedDate: vm.pickedDate})
    }

    /* ------------------------------ Private APIs --------------------------------- */
})
