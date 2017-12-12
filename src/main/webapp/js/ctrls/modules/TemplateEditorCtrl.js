/**
 * Created by Siddharth on 28-11-2017.
 */

// Controller for Template Editor Dialog
app.controller('TemplateEditorCtrl', function ($scope, $rootScope, ToastService) {
    var vm = this
    vm.Const = $rootScope.Constants.Template

    /*------------------------------- Html APIs -------------------------------*/
    // Field Related APIs
    vm.addNewField = function () {
        // Add entry to fields
        vm.fields.push({
            type: vm.Const.FIELD_TYPE_TEXT,
            title: ''
        })

        // Add entry to data
        vm.data.values.push({
            value: ''
        })
    }

    vm.removeField = function (fieldIdx) {
        // Remove field entry
        vm.fields.splice(fieldIdx, 1)

        // Remove data entry
        vm.data.values.splice(fieldIdx, 1)
    }

    vm.updateFieldType = function (fieldIdx, newType) {
        var field = vm.fields[fieldIdx]
        var value

        // Ignore if type is same
        if (field.type == newType) {
            return
        }

        // Update Value data structure according to selection
        if ((newType == vm.Const.FIELD_TYPE_TEXT) ||
            (newType == vm.Const.FIELD_TYPE_PHOTO) ||
            (newType == vm.Const.FIELD_TYPE_SIGN)) {
            value = ""
        } else if (newType == vm.Const.FIELD_TYPE_NUMBER) {
            value = 0
        } else if (newType == vm.Const.FIELD_TYPE_RADIOLIST) {
            value = {
                options: [],
                selection: 0
            }
        } else if (newType == vm.Const.FIELD_TYPE_CHECKLIST) {
            value = []
        }

        // Update field type
        field.type = newType
        vm.data.values[fieldIdx].value = value
    }

    vm.isDupeFieldTitle = function (title) {
        // Ignore Empty title
        if (!title) {
            return false
        }

        // Iterate through all field titles
        var count = 0
        vm.fields.forEach(function (field) {
            // Count appearances of given title
            if (field.title == title) {
                count++
            }
        })

        // return true if multiple counts
        return (count > 1)
    }

    // RadioList related APIs
    vm.addRadioOption = function (fieldIdx) {
        // Get Value of radioList
        var value = vm.data.values[fieldIdx].value

        // Add blank option
        value.options.push('')
    }

    vm.removeRadioOption = function (fieldIdx, optionIdx) {
        // Get Value of radioList
        var value = vm.data.values[fieldIdx].value

        // Check if this option was selected
        if (value.selection == optionIdx) {
            // Reset selection index to 0
            value.selection = 0
        }

        // Remove option
        value.options.splice(optionIdx, 1)
    }

    // Checklist related APIs
    vm.addCheckOption = function (fieldIdx) {
        // Get Value of checklist
        var value = vm.data.values[fieldIdx].value

        // Add blank unchecked option
        value.push({
            name: '',
            selection: false
        })
    }

    vm.removeCheckOption = function(fieldIdx, optionIdx) {
        // Get Value of checklist
        var value = vm.data.values[fieldIdx].value

        // Remove option
        value.splice(optionIdx, 1);
    }

    vm.isDupesInChecklist = function (fieldIdx) {
        // Get Value of checklist
        var value = vm.data.values[fieldIdx].value

        // Create array of option names
        var names = []
        value.forEach(function (option) {
            names.push(option.name)
        })

        // Check if dupes in the names
        return names.hasDupes()
    }

    // API to validate entered data
    $scope.callbacks.validateTemplate = function () {
        var toastMessage = ""

        if (!vm.template.name) {
            toastMessage = "Please fill template name..."
        } else if (!vm.fields.length) {
            toastMessage = "Please add atleast 1 field..."
        } else {
            for (var i = 0; i < vm.fields.length; i++) {
                var field = vm.fields[i]
                var value = vm.data.values[i].value

                if (!field.title) {
                    toastMessage = "Field Title cannot be empty..."
                } else if (vm.isDupeFieldTitle(field.title)) {
                    toastMessage = "Field titles must be unique..."
                } else if (!field.type) {
                    toastMessage = "Field type cannot be empty..."
                } else if ((field.type == vm.Const.FIELD_TYPE_NUMBER) && (field.value == "null")) {
                    toastMessage = "Please fill mandatory values..."
                } else if (field.type == vm.Const.FIELD_TYPE_RADIOLIST) {
                    if (!value.options.length) {
                        toastMessage = "Please add atleast 1 option to list..."
                    } else if (value.options.indexOf('') != -1) {
                        toastMessage = "Please enter valid option name..."
                    } else if (value.options.hasDupes()) {
                        toastMessage = "Option names must be unique..."
                    }
                } else if (field.type == vm.Const.FIELD_TYPE_CHECKLIST) {
                    if (!value.length) {
                        toastMessage = "Please add atleast 1 option to list..."
                    } else if (vm.isDupesInChecklist(i)) {
                        toastMessage = "Option names must be unique..."
                    } else {
                        // Check for empty option names
                        for (var j = 0; j < value.length; j++) {
                            var option = value[j]
                            if (!option.name) {
                                toastMessage = "Please enter valid option name..."
                                break
                            }
                        }
                    }
                }

                // Break from loop if invalid data found
                if (toastMessage.length) {
                    break
                }
            }
        }

        // Show Error Toast and eror UI for invalid data
        if (toastMessage.length) {
            ToastService.toast(toastMessage)
            vm.bShowError = true
        } else {
            vm.bShowError = false
        }

        return !vm.bShowError
    }

    /*------------------------------- Local APIs -------------------------------*/

    /*------------------------------- Init -------------------------------*/
    // Init Objects
    vm.template = $scope.template
    vm.fields = vm.template.fields
    vm.data = vm.template.defaultData
    vm.bShowError = false
})