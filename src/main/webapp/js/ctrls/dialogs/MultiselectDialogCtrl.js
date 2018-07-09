/**
 * Created by Siddharth on 03-07-2018.
 */

// Controller for Alert Dialog
app.controller('MultiselectDialogCtrl', function (  $scope, $mdDialog,
                                                    ObjMultiselect, SearchService,
                                                    title, url, filter, cb) {
    /* ------------------------------- Init -----------------------------------*/
    var vm = this

    vm.title = title
    vm.list = []
    vm.totalCount = 0
    vm.searchText = ""

    // Pager
    vm.pager = {
        start: 0,
        count: Constants.Table.DEFAULT_COUNT_PER_PAGE
    }

    // Selection params
    vm.selection = filter

    // Flags
    vm.bError = false
    vm.bSearching = false

    /* ------------------------------- Public APIs -----------------------------------*/
    // Method to perform search
    vm.searchTextChanged = function () {
        // Reset Pager
        vm.pager.start = 0

        //Reset List
        vm.list = []

        // Perform Search
        search()
    }

    // Method to load more results
    vm.loadMore = function () {
        // Set pager for next set of results
        vm.pager.start = vm.pager.start + vm.pager.count

        // Perform Search
        search()
    }

    // Button Callbacks
    vm.select = function () {
        // Trigger callback
        cb(vm.selection)

        // Hide dialog
        $mdDialog.hide()
    }

    vm.close = function () {
        $mdDialog.hide()
    }

    /* ------------------------------- Private APIs -----------------------------------*/
    function search() {
        // Set flags spproprietly
        vm.bError = false
        vm.bSearching = true

        // Get results from search service
        SearchService.search(url, vm.searchText, vm.pager).then(
            // Success Callback
            function (response) {
                // Add results to list
                vm.list.addAll(response.results)

                // Update total count of results
                vm.totalCount = response.totalCount

                // Update searching flag
                vm.bSearching = false
            },
            // Error Callback
            function (error) {
                // Set error flag
                vm.bError = true

                // Set search flag
                vm.bSearching = false
            }
        )
    }

    /* ------------------------------- Post Init -----------------------------------*/
    // Perform blank search
    search()
})
