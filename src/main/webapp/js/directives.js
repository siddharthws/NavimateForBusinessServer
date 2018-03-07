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

// Directive for image buttons file picker
app.directive("ibFilepicker", function () {
    return {
        scope: {
            image:      "@",
            text:       "@",
            filePicked: "&"
        },
        templateUrl: '/static/views/directives/ibFilepicker.html',
        controller: 'ibFilepickerCtrl',
    }
})

// Directive for custom scrolling related functionality
app.directive('scroll', function ($timeout) {
    return {
        restrict: 'A',
        scope: {
            // Callback for when bottom is reached in element while scrolling
            onBottomReached: '&',
            // Condition to check for scrolling to bottom
            scrollToBottom: '='
        },
        link: function (scope, element, attrs, vm) {
            // Method for scrolling to bottom
            var scrollToBottom = function () {
                if (scope.scrollToBottom) {
                    // Scroll element to bottom
                    $timeout(function () {
                        element[0].scrollTop = element[0].scrollHeight
                    }, 0)
                }
            }

            // Method to check if bottom is reached
            var scrollListener = function (event) {
                if ($(this).scrollTop() + $(this).innerHeight() >= $(this)[0].scrollHeight) {
                    // Bottom Reached. Trigger callback
                    scope.onBottomReached()
                }
            }

            // Set scroll listener on element
            element.on('scroll', _.debounce(scrollListener, 500))

            // Set watcher for scrolling to bottom
            scope.$watch('scrollToBottom', scrollToBottom)
        }
    }
})