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
            hint:               '@',
            modelFormat:        '@',
            displayFormat:      '@',
            dateModel:          '=',
            dateChange:         '&'
        },
        // Controller and view
        controller: 'datepickerCtrl as vm',
        templateUrl: '/static/views/directives/datepicker.html',
    }
})

// Loader directive
app.directive('loader', function () {
    return {
        restrict: 'E',
        templateUrl: '/static/views/directives/loader.html'
    }
})