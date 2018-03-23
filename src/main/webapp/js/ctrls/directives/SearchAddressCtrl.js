/**
 * Created by Siddharth on 23-03-2018.
 */

app.controller('SearchAddressCtrl', function ($scope, GoogleApiService) {
    /* ------------------------------ INIT --------------------------------- */
    var vm = this

    // List of address suggestions based on search text
    vm.suggestions = []

    // Flag to show / hide dropdown
    vm.bShowDropdown = false

    // Flags to show / hide loading /error UI
    vm.bSearching = false
    vm.bSearchError = false

    // Search text bound to input box
    vm.searchText = ""

    /* ------------------------------ Public APIs --------------------------------- */
    // Method to handle any text change in input box
    vm.search = function () {
        // Reset suggestions
        vm.suggestions = []

        // Reset error flag
        vm.bSearchError = false

        // Set searching flag
        vm.bSearching = true
        GoogleApiService.search(vm.searchText).then(
            // Success
            function (results) {
                // Reset flag
                vm.bSearching = false

                // Set search results
                vm.suggestions = results
            },
            function () {
                // Reset flag
                vm.bSearching = false
                vm.bError = true
            }
        )
    }

    // Method to handle list item click
    vm.onSelect = function (idx) {
        // Get item on this index
        var address = vm.suggestions[idx]

        // Set search text
        vm.searchText = address

        // Reset items
        vm.suggestions = []

        // Trigger callback with selected ID
        $scope.onSelect({address: address})

        // Hide dropdown
        vm.bShowDropdown = false
    }

    /* ------------------------------ Private APIs --------------------------------- */
    /* ------------------------------ Event Listeners --------------------------------- */
})
