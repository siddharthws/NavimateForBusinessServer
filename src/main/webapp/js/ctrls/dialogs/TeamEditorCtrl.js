/**
 * Created by Siddharth on 14-03-2018.
 */

// Controller for Alert Dialog
app.controller('TeamEditorCtrl', function ( $scope, $rootScope, $mdDialog,
                                            ToastService, TeamService, ObjUser,
                                            ids, cb) {
    /* ------------------------------- Init -----------------------------------*/
    var vm = this

    vm.team = []
    vm.selectedUser = null

    // Error / Waiting flags
    vm.bLoading = false
    vm.bLoadError = false
    vm.bInputError

    /* ------------------------------- Public APIs -----------------------------------*/
    // Method to add a new item to list
    vm.add = function () {
        // Create empty rep object
        var rep = new ObjUser(0, "", Constants.Role.REP, "", "", "")

        // Add to team
        vm.team.push(rep)

        // Set selected
        vm.selectedUser = rep
    }

    // Method to remove an item from list
    vm.remove = function (idx) {
        // Close dialog if all items removed
        if (vm.team.length == 1) {
            vm.close()
        }

        // Update selected user if required
        if (vm.selectedUser == vm.team[idx]) {
            if (idx == vm.team.length - 1) {
                vm.selectedUser = vm.team[idx - 1]
            } else {
                vm.selectedUser = vm.team[idx + 1]
            }
        }

        // Remove item from array
        vm.team.splice(idx, 1)
    }

    // Method to save team information
    vm.save = function () {
        // Validate entered data
        if (!validate()) {
            return
        }

        // Save data using Team Service
        $rootScope.showWaitingDialog("Updating team...")
        TeamService.edit(vm.team).then(
            // Success
            function () {
                // Close dialog & Notify user
                $rootScope.hideWaitingDialog()
                $mdDialog.hide()
                ToastService.toast("Team edited succesfully...")

                // Trigger callback
                cb()
            },
            // Error
            function (errorMessage) {
                // Notify user of error
                $rootScope.hideWaitingDialog()
                ToastService.toast("Error : " + errorMessage)
            }
        )
    }

    // Method to close dialog
    vm.close = function () {
        $mdDialog.hide()
    }
    /* ------------------------------- Private APIs -----------------------------------*/
    // API to validate entered data
    function validate () {
        var bValid = true

        // Validate name and phone of each team member
        for (var i = 0; i < vm.team.length; i++) {
            var user = vm.team[i]
            if (!user.isValid()) {
                bValid = false
                break
            }
        }

        vm.bInputError = !bValid
        return bValid
    }

    /* ------------------------------- Post Init -----------------------------------*/
    if (ids) {
        // Get all users by IDs passed in parameters
        vm.bLoading = true
        TeamService.sync(ids).then(
            function () {
                vm.bLoading = false
                vm.team = TeamService.cache
                vm.selectedUser = vm.team[0]
            },
            function () {
                vm.bLoading = false
                vm.bLoadError = true
            }
        )
    } else {
        // Add a user to list
        vm.add()
    }
})