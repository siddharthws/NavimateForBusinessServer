/**
 * Created by Siddharth on 27-02-2018.
 */

app.controller('SearchSelectCtrl', function ($scope, $q, SearchService) {
    /* ------------------------------ INIT --------------------------------- */
    var vm = this

    // List of items to show in dropdown
    vm.items = []

    // Flag to show / hide dropdown
    vm.bShowDropdown = false

    // Flags to show / hide loading /error UI
    vm.bLoading = false
    vm.bError = false

    // Search text bound to input box
    vm.searchText = ""

    // Pager object
    var pager = {
        startIdx: 0,
        count: Constants.Table.DEFAULT_COUNT_PER_PAGE
    }
    vm.totalResults = 0

    // Flags for request serialization
    var canceller = null

    /* ------------------------------ Public APIs --------------------------------- */
    // Method to handle any text change in input box
    vm.textChanged = function () {
        // Reset pager
        pager.startIdx = 0

        // Reset items list
        vm.items = []

        // Perform search with changed text
        search()
    }

    // Method to load more results
    vm.loadMore = function () {
        // Ignore if request already ongoing
        if (vm.bLoading) {
            return
        }

        // Ignore if all results have been synced
        if (vm.totalResults && (vm.totalResults == vm.items.length)) {
            return
        }

        // Update pager startIdx
        pager.startIdx += pager.count

        // Perform Search
        search()
    }

    // Method to handle list item click
    vm.onItemSelected = function (idx) {
        // Get item on this index
        var item = vm.items[idx]

        // Set search text
        vm.searchText = item.name

        // Reset items
        vm.items = []

        // Trigger callback with selected ID
        $scope.onSelect({id: item.id})

        // Hide dropdown
        vm.bShowDropdown = false
    }

    // Method to show dropdown when input box is focused
    vm.onFocused = function () {
        // Show Dropdwon
        vm.bShowDropdown = true

        // Start search if items in list
        if (!vm.items.length) {
            vm.textChanged()
        }
    }

    /* ------------------------------ Private APIs --------------------------------- */
    // Method to perform search and update waiting / error / data UI
    function search() {
        // Cancel ongoing request
        if (vm.bLoading) {
            canceller.resolve()
        }
        canceller = $q.defer()

        // mark loading flag
        vm.bLoading = true

        // Perform search
        SearchService.search(vm.searchText, vm.name, pager, canceller).then(
            // Success callback
            function (result) {
                // Reset loading flag
                vm.bLoading = false

                // Update total count
                vm.totalResults = result.totalCount

                // Add items to result
                vm.items = vm.items.concat(result.items)
            },
            // Error callback
            function () {
                // Mark error flag
                vm.bLoading = false
                vm.bError = true
            }
        )
    }

    /* ------------------------------ Event Listeners --------------------------------- */
})
