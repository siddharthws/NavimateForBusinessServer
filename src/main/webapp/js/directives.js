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

// Loader directive
app.directive('loading', function () {
    return {
        restrict: 'E',
        // Isolated scope with attributes
        scope: {
            msg:               '@',
            bWhite:            '@'
        },
        templateUrl: '/static/views/directives/loading.html'
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
            image:          "@",
            text:           "@",
            bTransparent:   "@"
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

// Directive for table pagination
app.directive('inputBox', function () {
    return {
        restrict: 'E',
        // Isolated scope with attributes
        scope: {
            label:      '@',
            hint:       '@',
            model:      '=',
            bNumber:    '@',
            bSmall:     '@',
            bShowError: '=',
            err:        '='
        },
        // view
        templateUrl: '/static/views/directives/inputBox.html'
    }
})

// Directive for table pagination
app.directive('inputUl', function () {
    return {
        restrict: 'E',
        // Isolated scope with attributes
        scope: {
            label:      '@',
            type:       '@',
            model:      '=',
            bShowError: '=',
            err:        '='
        },
        // view
        templateUrl: '/static/views/directives/inputUl.html'
    }
})

// Directive for table pagination
app.directive('textBox', function () {
    return {
        restrict: 'E',
        // Isolated scope with attributes
        scope: {
            label:       '@',
            text:        '@',
            bShowError:  '=',
            err:         '='
        },
        // view
        templateUrl: '/static/views/directives/textBox.html'
    }
})

// dropdown to select from a list of items
app.directive('dropdown', function ($timeout) {
    return {
        restrict: 'E',
        // Isolated scope with attributes
        scope: {
            label:       '@',
            text:        '=',
            items:       '=',
            onSelect:    '&',
            bShowError:  '=',
            err:         '='
        },
        // Controller and view
        templateUrl: '/static/views/directives/dropdown.html',
        // Link function
        link: function (scope, element, attrs, vm) {
            // init dropdown flag
            scope.bShowDropdown = false

            // Set dropdown hide listener on clicking anywhere outside the directive
            $('body').on('click', function (e) {
                var $element = $(element)

                // Check if a child of this element was clicked
                var bShow = false
                if ($element.is(e.target) || $element.has(e.target).length) {
                    if ($(e.target).hasClass('li-dropdown')) {
                        // Close dropdown if a list item was clicked
                        bShow = !scope.bShowDropdown
                    } else {
                        // Show dropdown
                        bShow = true
                    }
                } else {
                    // Hide dropdown
                    bShow = false
                }

                // Update using digest cycle
                if (scope.bShowDropdown != bShow) {
                    $timeout(function () {
                        scope.bShowDropdown = bShow
                    }, 0)
                }
            })
        }
    }
})

// Search dropdown to select something
app.directive('searchSelect', function ($timeout) {
    return {
        restrict: 'E',
        // Isolated scope with attributes
        scope: {
            // Hint text
            hint:       '@',
            // URL for search
            name:       '@',
            // Callback when selection is made
            onSelect:   '&'
        },
        // Controller and view
        controller: 'SearchSelectCtrl as vm',
        templateUrl: '/static/views/directives/searchSelect.html',
        // Link function
        link: function (scope, element, attrs, vm) {
            // Assign attributes to controller
            vm.hint         = attrs.hint
            vm.name         = attrs.name

            // Set dropdown hide listener
            $('body').on('click', function (e) {
                if (vm.bShowDropdown) {
                    var $element = $(element)
                    if (!$element.is(e.target) && !$element.has(e.target).length) {
                        $timeout(function () {
                            vm.bShowDropdown = false
                        }, 0)
                    }
                }
            })
        }
    }
})

// dropdown to select from a list of items
app.directive('searchAddress', function ($timeout) {
    return {
        restrict: 'E',
        // Isolated scope with attributes
        scope: {
            onSelect:    '&'
        },
        // Controller and view
        controller: 'SearchAddressCtrl as vm',
        templateUrl: '/static/views/directives/searchAddress.html',
        // Link function
        link: function (scope, element, attrs, vm) {
            // Set dropdown show / hide listener
            $('body').on('click', function (e) {
                var $element = $(element)

                // Check if a child of this element was clicked
                var bShow = false
                if ($element.is(e.target) || $element.has(e.target).length) {
                    if ($(e.target).hasClass('li-dropdown')) {
                        // Close dropdown if a list item was clicked
                        bShow = !vm.bShowDropdown
                    } else {
                        // Show dropdown
                        bShow = true
                    }
                } else {
                    // Hide dropdown
                    bShow = false
                }

                // Update using digest cycle
                if (vm.bShowDropdown != bShow) {
                    $timeout(function () {
                        vm.bShowDropdown = bShow
                    }, 0)
                }
            })
        }
    }
})

// Directive for table pagination
app.directive('pager', function () {
    return {
        restrict: 'E',
        // Isolated scope with attributes
        scope: {
            // URL for search
            pageCount:       '=',
            // Callback when selection is made
            onPageChanged:   '&'
        },
        // Controller and view
        controller: 'PagerCtrl as vm',
        templateUrl: '/static/views/directives/pager.html'
    }
})

// Directive for picking / viewing objects
app.directive('pickNView', function () {
    return {
        restrict: 'E',
        // Isolated scope with attributes
        scope: {
            label:      '@',
            text:       '=',
            onPick:     '&',
            onView:     '&',
            bShowError: '=',
            err:        '='
        },
        // Controller and view
        templateUrl: '/static/views/directives/pickNView.html'
    }
})

// Directive for showing checklist
app.directive('checklist', function () {
    return {
        restrict: 'E',
        // Isolated scope with attributes
        scope: {
            label:      '@',
            value:      '='
        },
        // View
        templateUrl: '/static/views/directives/checklist.html'
    }
})

// Directive for showing radiolist
app.directive('radiolist', function () {
    return {
        restrict: 'E',
        // Isolated scope with attributes
        scope: {
            label:      '@',
            value:      '='
        },
        // View
        templateUrl: '/static/views/directives/radiolist.html'
    }
})

// Directive to edit template values array
app.directive('templateValuesEditor', function () {
    return {
        restrict: 'E',
        // Isolated scope with attributes
        scope: {
            bShowError:  '=',
            values:      '='
        },
        // Controller and view
        templateUrl: '/static/views/directives/templateValuesEditor.html'
    }
})

// Directive to showing map
app.directive('nvMap', function () {
    return {
        restrict: 'E',
        // Isolated scope with attributes
        scope: {
            objMap:         '=',
            onMarkerClick:  '&'
        },
        // Controller and view
        controller: 'NvMapCtrl as vm',
        templateUrl: '/static/views/directives/nvMap.html'
    }
})