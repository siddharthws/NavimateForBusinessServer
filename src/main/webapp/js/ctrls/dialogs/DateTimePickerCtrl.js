/**
 * Created by Siddharth on 15-05-2018.
 */

// Controller for Alert Dialog
app.controller('DateTimePickerCtrl', function ($scope, $mdDialog, $timeout,
                                               date, cb) {
    /* ------------------------------- INIT -----------------------------------*/
    var vm = this

    // Required by md-time-picker
    vm.message = {
        hour: 'Hour is required',
        minute: 'Minute is required',
        meridiem: 'Meridiem is required'
    }
    
    /* ------------------------------- Public methods -----------------------------------*/
    // Method to pick the date set currently in dialog
    vm.pick = function () {
        // Create picked date using date and time picker
        var pickedDate = new Date(  vm.date.getFullYear(),
                                    vm.date.getMonth(),
                                    vm.date.getDate(),
                                    vm.time.getHours(),
                                    vm.time.getMinutes(),
                                    vm.time.getSeconds(),
                                    vm.time.getMilliseconds())

        // Trigger Callback
        cb(pickedDate)

        // Hide dialog
        $mdDialog.hide()
    }

    // Method to clear the set date
    vm.clear = function () {
        // Trigger callback with null result
        cb(null)

        // Hide dialog
        $mdDialog.hide()
    }
    
    vm.close = function () {
        // hide dialog
        $mdDialog.hide()
    }
    
    /* ------------------------------- Private methods -----------------------------------*/
    /* ------------------------------- Post Init -----------------------------------*/
    // Initialize date sent in params if not available
    if (!date) {
        date = new Date()
    }

    // Set date picker variable
    vm.date = new Date(date.getTime())

    // Set time picker variable
    vm.time = new Date(date.getTime())
})
