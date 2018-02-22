/**
 * Created by Siddharth on 20-02-2018.
 *
 * Declaration of various directive types
 */

// Date picker directive
app.directive('datepicker', function () {
    return {
        // Restrict usage to element only
        restrict: 'E',
        // Isolated scope with attributes
        scope: {
            // Hint text
            hint:       '@',
            // Init date
            initDate:   '@',
            // Callback when date is changed
            dateChange: '&'
        },
        // Controller and view
        controller: 'datepickerCtrl as vm',
        templateUrl: '/static/views/directives/datepicker.html',
        // Link function
        link: function (scope, element, attrs, vm) {
            // Assign attributes to controller
            vm.hint           = attrs.hint
            vm.pickedDate     = attrs.initDate

            // Trigger post linking callback
            vm.postLinking()
        }
    }
})

// Loader directive
app.directive('loader', function () {
    return {
        restrict: 'E',
        templateUrl: '/static/views/directives/loader.html'
    }
})