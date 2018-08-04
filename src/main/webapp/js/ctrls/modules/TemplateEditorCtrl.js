/**
 * Created by Siddharth on 28-11-2017.
 */

// Controller for Template Editor Dialog
app.controller('TemplateEditorCtrl', function ( $scope, $rootScope,
                                                ToastService, ObjField, DialogService, TemplateService) {
    var vm = this
    vm.Const = $rootScope.Constants.Template

    /*------------------------------- Html APIs -------------------------------*/
    // Field Related APIs
    vm.addNewField = function () {
        // Add entry to fields
        vm.fields.push(new ObjField(null, "", vm.Const.FIELD_TYPE_TEXT, ""))
    }

    vm.removeField = function (fieldIdx) {
        // Remove field entry
        vm.fields.splice(fieldIdx, 1)
    }

    vm.fieldSettings = function (fieldIdx) {
        // Open Field Settings Dialog
        DialogService.FieldSettings(vm.fields[fieldIdx])
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
            (newType == vm.Const.FIELD_TYPE_FILE) ||
            (newType == vm.Const.FIELD_TYPE_SIGN) ||
            (newType == vm.Const.FIELD_TYPE_DATE)) {
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
        } else if (newType == vm.Const.FIELD_TYPE_CHECKBOX) {
            value = false
        } else if (newType == vm.Const.FIELD_TYPE_PRODUCT) {
            value = {}
        }

        // Update field type
        field.type = newType
        field.value = value
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
        var value = vm.fields[fieldIdx].value

        // Add blank option
        value.options.push('')
    }

    vm.removeRadioOption = function (fieldIdx, optionIdx) {
        // Get Value of radioList
        var value = vm.fields[fieldIdx].value

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
        // Add blank unchecked option
        vm.fields[fieldIdx].value.push({
            name: '',
            selection: false
        })
    }

    vm.removeCheckOption = function(fieldIdx, optionIdx) {
        // Remove option
        vm.fields[fieldIdx].value.splice(optionIdx, 1);
    }

    vm.isDupesInChecklist = function (fieldIdx) {
        // Get Value of checklist
        var value = vm.fields[fieldIdx].value

        // Create array of option names
        var names = []
        value.forEach(function (option) {
            names.push(option.name)
        })

        // Check if dupes in the names
        return names.hasDupes()
    }

    // API to validate entered data
    function validateTemplate () {
        var toastMessage = ""

        if (!vm.template.name) {
            toastMessage = "Please fill template name..."
        } else if (vm.templateNames.contains(vm.template.name)) {
            toastMessage = "Another template with this name already exists"
        } else if (vm.fields.length)  {
            for (var i = 0; i < vm.fields.length; i++) {
                var field = vm.fields[i]
                var value = field.value

                if (!field.title) {
                    toastMessage = "Field Title cannot be empty..."
                } else if (vm.isDupeFieldTitle(field.title)) {
                    toastMessage = "Field titles must be unique..."
                } else if (!field.type) {
                    toastMessage = "Field type cannot be empty..."
                } else if ((field.type == vm.Const.FIELD_TYPE_NUMBER) && (value == null)) {
                    toastMessage = "Number field must have a numeric value..."
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

        // Show Error Toast and error UI for invalid data
        if (toastMessage.length) {
            // Shoe Error UI
            vm.bShowError = true

            // Show error toast
            ToastService.toast(toastMessage)
        } else {
            // Hide Error UI
            vm.bShowError = false

            // Emit Validation success event
            $scope.$emit(Constants.Events.TEMPLATE_VALIDATE_SUCCESS)
        }
    }

    /*------------------------------- Local APIs -------------------------------*/

    /*------------------------------- Init -------------------------------*/
    // Init Objects
    vm.template = $scope.template
    vm.availableFieldTypes = $scope.availableFieldTypes
    vm.fields = vm.template.fields
    vm.bShowError = false

    // Get all template names
    vm.templateNames = []
    TemplateService.cache.forEach(function (template) {
        if (template.id != vm.template.id) {
            vm.templateNames.push(template.name)
        }
    })

    // Set broadcast listener
    // Validation event
    $scope.$on(Constants.Events.TEMPLATE_VALIDATE, function (event, args) {
        // Emit success event if validation completes
        validateTemplate()
    })
})
