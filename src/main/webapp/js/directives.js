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

// Directive for binding file read
app.directive("fileread", function () {
    return {
        scope: {
            fileread: "&"
        },
        link: function (scope, element, attributes) {
            element.bind("change", function (changeEvent) {
                scope.$apply(function () {
                    scope.fileread({file: changeEvent.target.files[0]})
                });
            });
        }
    }
})

// Directive for image buttons
app.directive("imagebutton", function () {
    return {
        scope: {
            image: "@",
            text: "@"
        },
        templateUrl: '/static/views/directives/imagebutton.html'
    }
})