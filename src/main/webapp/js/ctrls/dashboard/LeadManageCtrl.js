/**
 * Created by Siddharth on 04-09-2017.
 */

app.controller("LeadManageCtrl", function ($scope, $rootScope, $http, $localStorage, $state, $window,  ExcelService, DialogService, ToastService, LeadDataService, TemplateDataService) {
    var vm = this

    /* ------------------------------- Scope APIs -----------------------------------*/
    vm.add = function() {
        DialogService.leadEditor(null)
    }

    // APIs for table based actions
    vm.export = function () {
        // Broadcast Toggle Columns Event
        $scope.$broadcast(Constants.Events.TABLE_EXPORT, {filename: 'Navimate-Leads'})
    }

    vm.clearFilters = function () {
        // Broadcast Toggle Columns Event
        $scope.$broadcast(Constants.Events.TABLE_CLEAR_FILTERS)
    }

    vm.toggleColumns = function () {
        // Broadcast Toggle Columns Event
        $scope.$broadcast(Constants.Events.TABLE_TOGGLE_COLUMNS)
    }

    vm.sync = function () {
        LeadDataService.sync()
    }

    vm.showOnMaps = function (lead) {
        $window.open("https://www.google.com/maps/search/?api=1&query=" + lead.latitude + "," + lead.longitude, "_blank")
    }

    // List Actions
    vm.remove = function() {
        // Launch Confirm Dialog
        DialogService.confirm(
            "Are you sure you want to remove these " + vm.selection.length + " leads ?",
            function () {
                $rootScope.showWaitingDialog("Please wait while we are removing leads...")
                // Make Http call to remove leads
                $http({
                    method: 'POST',
                    url: '/api/users/lead/remove',
                    headers: {
                        'X-Auth-Token': $localStorage.accessToken
                    },
                    data: {
                        leads: vm.selection
                    }
                }).then(
                    function (response) {
                        $rootScope.hideWaitingDialog()

                        //Re-sync Lead data since lead has been deleted
                        LeadDataService.sync()

                        // Show Toast
                        ToastService.toast("leads removed...")
                    },
                    function (error) {
                        $rootScope.hideWaitingDialog()
                        ToastService.toast("Failed to remove leads!!!")
                    })
            })
    }
    
    vm.edit = function () {
        //Launch Leads-Editor dialog
        DialogService.leadEditor(vm.selection)
    }

    vm.createtasks = function () {
        // Create empty tasks for all seelcted leads
        var task = []
        vm.selection.forEach(function (lead) {
            task.push({
                lead: lead
            })
        })

        // Trigger task creation dialog
        DialogService.taskCreator(task)
    }

    /* ------------------------------- Local APIs -----------------------------------*/
    // Send request to get list of leads
    function init() {
        // Get Lead Data
        vm.leads =  LeadDataService.cache.data

        // Parse lead data into tabular format
        $scope.tableParams.columns = getColumns()
        $scope.tableParams.values  = getValues($scope.tableParams.columns)

        // Broadcast Table Init Event
        $scope.$broadcast(Constants.Events.TABLE_INIT)
    }

    // Method to create column array from lead information
    function getColumns() {
        var columns = []

        // Add mandatory columns
        columns.push({title: "Title", type: Constants.Template.FIELD_TYPE_TEXT, filterType: Constants.Filter.TYPE_SELECTION})
        columns.push({title: "Address", type: Constants.Template.FIELD_TYPE_TEXT, filterType: Constants.Filter.TYPE_TEXT})
        columns.push({title: "Location", type: Constants.Template.FIELD_TYPE_LOCATION, filterType: Constants.Filter.TYPE_NONE})
        columns.push({title: "Template", type: Constants.Template.FIELD_TYPE_TEXT, filterType: Constants.Filter.TYPE_SELECTION})

        // Add columns using templated data
        // Iterate through all leads
        vm.leads.forEach(function (lead) {
            // Iterate through all values in this lead
            lead.templateData.values.forEach(function (value) {
                // Get field of this value
                var field = TemplateDataService.getFieldById(value.fieldId)

                // Ignore value if field is not available
                if (!field) {
                    return
                }

                // Check if this field has been added to column
                var bColumnFound = false
                for (var  i = 0; i < columns.length; i++) {
                    var column = columns[i]
                    if ((column.title == field.title) && (column.type == field.type)) {
                        bColumnFound = true
                        break
                    }
                }

                // Add new column if not found
                if (!bColumnFound) {
                    columns.push({title: field.title, type: field.type, filterType: getFilterFromType(field.type)})
                }
            })
        })

        return columns
    }

    // Method to get values array
    function getValues(columns) {
        var values = []

        // Iterate through all leads
        vm.leads.forEach(function (lead) {
            // Create a row of empty entries
            var row = Array.apply(null, Array(columns.length)).map(function() { return '-' })

            // Add mandatory data to row
            row[0] = lead.title
            row[1] = lead.address
            row[2] = lead.latitude + "," + lead.longitude

            // Get template data
            var template = TemplateDataService.getTemplateById(lead.templateId)
            if (template) {
                row[3] = template.name
            }

            // iterate through template data
            lead.templateData.values.forEach(function (value) {
                // Get field of this value
                var field = TemplateDataService.getFieldById(value.fieldId)

                // Ignore value if field is not available
                if (!field) {
                    return
                }

                // Find column index for this field
                var colIdx
                for (colIdx = 0; colIdx < columns.length; colIdx++) {
                    var column = columns[colIdx]
                    if ((column.title == field.title) && (column.type == field.type)) {
                        break
                    }
                }

                // Add value to this cell depending on it's type
                switch (field.type) {
                    case Constants.Template.FIELD_TYPE_TEXT:
                    case Constants.Template.FIELD_TYPE_NUMBER:
                    case Constants.Template.FIELD_TYPE_SIGN:
                    case Constants.Template.FIELD_TYPE_PHOTO: {
                        row[colIdx] = value.value
                        break
                    }
                    case Constants.Template.FIELD_TYPE_RADIOLIST: {
                        row[colIdx] = value.value.options[value.value.selection]
                        break
                    }
                    case Constants.Template.FIELD_TYPE_CHECKLIST: {
                        value.value.forEach(function (optionJson) {
                            if (optionJson.selection) {
                                if (row[colIdx] == '-') {
                                    row[colIdx] = optionJson.name
                                } else {
                                    row[colIdx] += ', ' + optionJson.name
                                }
                            }
                        })
                        break
                    }
                    case Constants.Template.FIELD_TYPE_CHECKBOX: {
                        if (value.value) {
                            row[colIdx] = "Yes"
                        } else {
                            row[colIdx] = "No"
                        }
                        break
                    }
                }
            })

            // Add row to values
            values.push(row)
        })

        return values
    }

    // Method to return filter type based on field type
    function getFilterFromType(type) {
        var filterType = Constants.Filter.TYPE_NONE

        switch (type) {
            case Constants.Template.FIELD_TYPE_TEXT:
            case Constants.Template.FIELD_TYPE_CHECKLIST: {
                filterType = Constants.Filter.TYPE_TEXT
                break
            }
            case Constants.Template.FIELD_TYPE_NUMBER: {
                filterType = Constants.Filter.TYPE_NUMBER
                break
            }
            case Constants.Template.FIELD_TYPE_RADIOLIST:
            case Constants.Template.FIELD_TYPE_CHECKBOX: {
                filterType = Constants.Filter.TYPE_SELECTION
                break
            }
        }

        return filterType
    }

    /* ------------------------------- INIT -----------------------------------*/
    // Set menu and option
    $scope.nav.item       = Constants.DashboardNav.Menu[Constants.DashboardNav.ITEM_LEADS]
    $scope.nav.option     = Constants.DashboardNav.Options[Constants.DashboardNav.OPTION_MANAGE]

    // Init Variables
    vm.leads = []
    vm.selection = []
    vm.bCheckAll = false

    // Init Table Parameters
    $scope.tableParams = {}
    $scope.tableParams.columns = []
    $scope.tableParams.values = []
    $scope.tableParams.style = {bStriped: true, bSelectable: true}

    // Add event listeners
    // Listener for Lead data ready event
    $scope.$on(Constants.Events.LEAD_DATA_READY, function (event, data) {
        init()
    })

    // Listener for Lead template data ready event
    $scope.$on(Constants.Events.LEAD_TEMPLATE_DATA_READY, function (event, data) {
        init()
    })

    // Listener for table row selection / unselection events
    $scope.$on(Constants.Events.TABLE_ROW_SELECT, function (event, params) {
        // Clear selection array
        vm.selection = []

        // Push all seelcted leads
        params.selectedIndexes.forEach(function (selectedIdx) {
            vm.selection.push(vm.leads[selectedIdx])
        })
    })

    // Init View
    init()
})
