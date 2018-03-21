/**
 * Created by Siddharth on 21-03-2018.
 */

app.controller('DropdownCtrl', function ($scope) {
    /* ------------------------------ INIT --------------------------------- */
    var vm = this

    // Flag to show / hide dropdown
    vm.bShowDropdown = false

    // Currently selected item (Set first as selected
    vm.selection = $scope.items[0]

    /* ------------------------------ Public APIs --------------------------------- */
    // Method to handle list item click
    vm.onItemSelected = function (idx) {
        // Set selected item to this
        vm.selection = $scope.items[idx]

        // Trigger callback with selected ID
        $scope.onSelect({id: vm.selection.id})

        // Hide dropdown
        vm.bShowDropdown = false
    }

    /* ------------------------------ Private APIs --------------------------------- */
    /* ------------------------------ Event Listeners --------------------------------- */
})
