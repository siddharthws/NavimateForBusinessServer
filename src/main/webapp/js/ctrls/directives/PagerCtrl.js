/**
 * Created by Siddharth on 04-03-2018.
 */

app.controller('PagerCtrl', function ($scope, $timeout) {
    /* ------------------------------ INIT --------------------------------- */
    var vm = this

    vm.visiblePages = []
    vm.currentPage = 0
    vm.bPrevEllipses = false
    vm.bNextEllipses = false

    /* ------------------------------ Public APIs --------------------------------- */
    // Method to update page number
    vm.page = function (pageNum) {
        // Ignore if page is already open
        if (vm.currentPage == pageNum) {
            return
        }

        // Update current page
        vm.currentPage = pageNum

        // Refresh pages if current page is not visible
        if (vm.visiblePages.indexOf(vm.currentPage) == -1) {
            refreshDisplay()
        }

        // Trigger callback
        $scope.onPageChanged({page: pageNum})
    }
    /* ------------------------------ Private APIs --------------------------------- */
    function refreshDisplay() {
        // Get closest 5 pages
        var visibleCount = 5
        var firstPage = vm.currentPage
        var lastPage = vm.currentPage + visibleCount - 1

        // Validate first and last page
        if (lastPage > $scope.pageCount) {
            lastPage = $scope.pageCount
            firstPage = $scope.pageCount - visibleCount
        }
        if (firstPage < 1) {
            firstPage = 1
        }

        // Show / Hide ellipses buttons
        vm.bPrevEllipses = (firstPage > 1)
        vm.bNextEllipses = (lastPage < $scope.pageCount)

        // Refresh visisble pages array
        vm.visiblePages = []
        for (var i = firstPage; i <= lastPage; i++) {
            vm.visiblePages.push(i)
        }
    }

    /* ------------------------------ Listeners --------------------------------- */
    // Page count change listener
    $scope.$watch('pageCount', function () {
        if ($scope.pageCount) {
            vm.currentPage = 1
            refreshDisplay()
        }
    })

    /* ------------------------------ POST INIT --------------------------------- */
    refreshDisplay()
})