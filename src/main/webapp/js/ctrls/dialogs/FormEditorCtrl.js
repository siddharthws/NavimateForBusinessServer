/**
 * Created by Siddharth on 10-11-2017.
 */

app.controller('FormEditorCtrl', function ($scope, $rootScope, $http, $localStorage, $mdDialog, ToastService, form, updateCb) {

    /* ------------------------------- Scope APIs -----------------------------------*/
    // Button Click APIs
    $scope.save = function () {
        if (validate()) {
            $rootScope.showWaitingDialog("Please wait while form is being saved...")
            $http({
                method:     'POST',
                url:        '/api/users/form',
                headers:    {
                    'X-Auth-Token':    $localStorage.accessToken
                },
                data: {
                    'form': $scope.form
                }
            })
                .then(
                    function (response) {
                        // Hide dialog and show toast
                        $rootScope.hideWaitingDialog()
                        $mdDialog.hide()
                        ToastService.toast("Form saved successfully...")

                        // Trigger Callback
                        updateCb()
                    },
                    function (error) {
                        $rootScope.hideWaitingDialog()
                        console.log(error)
                        ToastService.toast("Could not save form...")
                    }
                )
        }
    }

    $scope.close = function () {
        $mdDialog.hide()
    }

    // Form Data Update APIs
    $scope.radioOptionChanged = function (field, newOption, oldOption) {
        if (field.value.selection == oldOption) {
            field.value.selection = newOption
        }
    }

    $scope.radioOptionRemove = function (field, index) {
        if (field.value.selection == field.value.options[index]) {
            field.value.selection = ""
        }

        field.value.options.splice(index, 1)
    }

    $scope.updateFieldType = function (field, type) {
        // Ignore if type is same
        if (field.type == type) {
            return
        }

        // Update Value dtaa structure according to selection
        if ((type == 'text') || (type == 'number') || (type == 'photo') || (type == 'signature')) {
            field.value = ''
        } else if (type == 'radioList' || type == 'checkList') {
            field.value = {
                selection: [],
                options: []
            }
        }

        // Update field type
        field.type = type
    }

    // Duplicate Check APIs
    $scope.checkDupes = function(array) {
        var copiedArray = array.concat().sort(); // use whatever sort you want
        for (var i = 0; i < copiedArray.length -1; i++)
        {
            if ((copiedArray[i+1] == copiedArray[i]) && (copiedArray[i].length))
            {
                return true;
            }
        }
        return false;
    }

    $scope.isDupeFieldTitle = function (title) {
        // Ignore Empty title
        if (!title) {
            return false
        }

        // Iterate through all field titles
        var count = 0
        $scope.form.data.forEach(function (field) {
            // Count appearances of given title
            if (field.title == title) {
                count++
            }
        })

        // return true if multiple counts
        return (count > 1)
    }

    /* ------------------------------- Local APIs -----------------------------------*/
    // API to init data
    function init() {
        // Init Form Object
        $scope.form = form
        if (!$scope.form) {
            $scope.form = {name: '', data: []}
        }

        // Init existing form names to avoid template name duplication
        $http({
            method:     'GET',
            url:        '/api/users/form',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            }
        })
            .then(
                function (response) {
                    // make list of existing form titles
                    response.data.forEach(function (form) {
                        if ($scope.form.id != form.id) {
                            $scope.formNames.push(form.name)
                        }
                    })
                },
                function (error) {
                    console.log(error)
                }
            )
    }

    // API to validate entered data
    function validate() {
        var toastMessage = ""

        if (!$scope.form.name) {
            toastMessage = "Please fill template name..."
        } else if ($scope.formNames.indexOf($scope.form.name) != -1) {
            toastMessage = "Duplicate template name found..."
        } else if (!$scope.form.data.length) {
            toastMessage = "Please add atleast 1 field..."
        } else {
            for (var i = 0; i < $scope.form.data.length; i++) {
                var field = $scope.form.data[i]
                if (!field.title) {
                    toastMessage = "Field Title cannot be empty..."
                } else if ($scope.isDupeFieldTitle(field.title)) {
                    toastMessage = "Field titles must be unique..."
                } else if (!field.type) {
                    toastMessage = "Field type cannot be empty..."
                } else if (field.type == 'radioList') {
                    if (!field.value.options.length) {
                        toastMessage = "Please add atleast 1 option to list..."
                    } else if ($scope.checkDupes(field.value.options)) {
                        toastMessage = "Option names must be unique..."
                    } else if (field.value.options.indexOf('') != -1) {
                        toastMessage = "Please enter valid option name..."
                    } else if (!field.value.selection) {
                        toastMessage = "Please select atleast one option in radio list..."
                    }
                } else if (field.type == 'checkList') {
                    if (!field.value.options.length) {
                        toastMessage = "Please add atleast 1 option to list..."
                    } else if ($scope.checkDupes(field.value.options)) {
                        toastMessage = "Option names must be unique..."
                    } else if (field.value.options.indexOf('') != -1) {
                        toastMessage = "Please enter valid option name..."
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
            $scope.bShowError = true
        } else {
            $scope.bShowError = false
        }

        return !$scope.bShowError
    }

    /* ------------------------------- INIT -----------------------------------*/
    // Init objects
    $scope.form = {}
    $scope.formNames = []
    $scope.bShowError = false

    init()
})
