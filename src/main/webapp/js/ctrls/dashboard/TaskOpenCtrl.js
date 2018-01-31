/**
 * Created by Siddharth on 22-08-2017.
 */

app.controller("TaskOpenCtrl", function ($scope, $rootScope, $http, $localStorage, $state, DialogService, ToastService, TaskDataService, LeadDataService, TeamDataService, TemplateDataService) {
    var vm = this

    /*-------------------------------- Scope APIs --------------------------------*/
    vm.add = function () {
        // Launch Task Creator dialog
        DialogService.taskCreator(null)
    }

    // APIs for table based actions
    vm.export = function () {
        // Broadcast Toggle Columns Event
        $scope.$broadcast(Constants.Events.TABLE_EXPORT, {filename: 'Navimate-Tasks'})
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
        TaskDataService.sync()
    }

    vm.edit = function () {
        DialogService.taskCreator(vm.selection)
    }

    vm.close = function () {
        //Launch confirm Dialog box
        DialogService.confirm("Are you sure you want to close these " + vm.selection.length + " tasks ?",
            function () {
                //http call to close tasks
                $rootScope.showWaitingDialog("Closing Tasks...")
                $http({
                    method: 'POST',
                    url:    '/api/users/task/close',
                    headers: {
                        'X-Auth-Token': $localStorage.accessToken
                    },
                    data: {
                        tasks : vm.selection
                    }
                })
                .then(
                    function (response) {
                        $rootScope.hideWaitingDialog()

                        //Re-sync Task data since Task has been closed
                        TaskDataService.sync()

                        // Show Toast
                        ToastService.toast("Tasks closed...")
                    },
                    function (error) {
                        $rootScope.hideWaitingDialog()
                        ToastService.toast("Failed to close tasks!!!")
                    })
            })
    }

    vm.remove = function () {
        //Launch confirm Dialog box
        DialogService.confirm("Are you sure you want to remove these " + vm.selection.length + " tasks ?",
            function () {
                //http call to close tasks
                $rootScope.showWaitingDialog("Removing Tasks...")
                $http({
                    method: 'POST',
                    url:    '/api/users/task/remove',
                    headers: {
                        'X-Auth-Token': $localStorage.accessToken
                    },
                    data: {
                        tasks : vm.selection
                    }
                })
                .then(
                    function (response) {
                        $rootScope.hideWaitingDialog()

                        //Re-sync Task data since Task has been Removed.
                        TaskDataService.sync()

                        // Show Toast
                        ToastService.toast("Tasks removed successfully...")
                    },
                    function (error) {
                        $rootScope.hideWaitingDialog()
                        ToastService.toast("Failed to remove tasks!!!")
                    })
            })
    }
    
    vm.stopRenewal = function () {
        //Launch confirm Dialog box
        DialogService.confirm("Are you sure you want to stop renewal for " + vm.selection.length + " tasks ?",
            function () {
                //http call to stop task renewal
                $rootScope.showWaitingDialog("Stopping renewal period...")
                $http({
                    method: 'POST',
                    url:    '/api/users/task/stoprenew',
                    headers: {
                        'X-Auth-Token': $localStorage.accessToken
                    },
                    data: {
                        tasks : vm.selection
                    }
                })
                .then(
                    function (response) {
                        $rootScope.hideWaitingDialog()

                        //Re-sync Task data since Task renewal has been updated.
                        TaskDataService.sync()

                        // Show Toast
                        ToastService.toast("renewal period stopped...")
                    },
                    function (error) {
                        $rootScope.hideWaitingDialog()
                        ToastService.toast("Failed to stop renewal!!!")
                    })
            })
    }

    vm.showMap = function () {
        // Prepare location array for each task
        var locations = []
        vm.selection.forEach(function (task) {
            // Get lead of this task
            var lead = LeadDataService.getById(task.leadId)

            // Add lead title, lat and lng
            locations.push({
                title:      lead.title,
                latitude:   lead.latitude,
                longitude:  lead.longitude
            })
        })

        // Open Location Viewer dialog
        DialogService.locationViewer(locations)
    }

    /*-------------------------------- Local APIs --------------------------------*/
    function initTasks () {
        // Reset data
        vm.tasks = []

        // Get open Tasks
        var tasks = TaskDataService.cache.data
        tasks.forEach(function (task) {
            if (task.status == 'OPEN') {
                vm.tasks.push(task)
            }
        })

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
        columns.push({title: "ID",          type: Constants.Template.FIELD_TYPE_TEXT, filterType: Constants.Filter.TYPE_SELECTION})
        columns.push({title: "Lead",        type: Constants.Template.FIELD_TYPE_TEXT, filterType: Constants.Filter.TYPE_SELECTION})
        columns.push({title: "Rep",         type: Constants.Template.FIELD_TYPE_TEXT, filterType: Constants.Filter.TYPE_SELECTION})
        columns.push({title: "Status",      type: Constants.Template.FIELD_TYPE_TEXT, filterType: Constants.Filter.TYPE_SELECTION})
        columns.push({title: "Period",      type: Constants.Template.FIELD_TYPE_NUMBER, filterType: Constants.Filter.TYPE_NUMBER})
        columns.push({title: "Form",        type: Constants.Template.FIELD_TYPE_TEXT, filterType: Constants.Filter.TYPE_SELECTION})
        columns.push({title: "Template",    type: Constants.Template.FIELD_TYPE_TEXT, filterType: Constants.Filter.TYPE_SELECTION})

        // Add columns using templated data
        // Iterate through all leads
        vm.tasks.forEach(function (task) {
            // Iterate through all values in this lead
            task.templateData.values.forEach(function (value) {
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
        vm.tasks.forEach(function (task) {
            // Create a row of empty entries
            var row = Array.apply(null, Array(columns.length)).map(function() { return '-' })

            // Add mandatory data to row
            row[0] = task.cId

            // Add lead data
            var lead = LeadDataService.getById(task.leadId)
            // Ignore task if lead is not available
            if (!lead) {
                return
            }
            row[1] = lead.title

            // Add rep Data
            var rep = TeamDataService.getById(task.repId)
            if (rep) {
                row[2] = rep.name
            } else {
                row[2] = 'Unassigned'
            }

            // Add status and period
            row[3] = task.status
            row[4] = task.period

            // Ignore task if it's form template was removed
            var formTemplate = TemplateDataService.getTemplateById(task.formTemplateId)
            if (!formTemplate) {
                return
            }
            row[5] = formTemplate.name

            // Ignore task if it's task template was removed
            var taskTemplate = TemplateDataService.getTemplateById(task.templateId)
            if (!taskTemplate) {
                return
            }
            row[6] = taskTemplate.name

            // iterate through template data
            task.templateData.values.forEach(function (value) {
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

    /*-------------------------------- INIT --------------------------------*/
    // Set menu and option
    $scope.nav.item       = Constants.DashboardNav.Menu[Constants.DashboardNav.ITEM_TASKS]
    $scope.nav.option     = Constants.DashboardNav.Options[Constants.DashboardNav.OPTION_OPEN]

    // Init Objects
    vm.tasks = []
    vm.selection = []
    vm.bCheckAll = false

    // Init Table Parameters
    $scope.tableParams = {}
    $scope.tableParams.columns = []
    $scope.tableParams.values = []
    $scope.tableParams.style = {bStriped: true, bSelectable: true}

    // Add event listeners
    // Listener for Task data ready event
    $scope.$on(Constants.Events.TASK_DATA_READY, function (event, data) {
        initTasks()
    })

    // Listener for Lead data ready event
    $scope.$on(Constants.Events.LEAD_DATA_READY, function (event, data) {
        initTasks()
    })

    // Listener for Team data ready event
    $scope.$on(Constants.Events.TEAM_DATA_READY, function (event, data) {
        initTasks()
    })

    // Listener for Lead template data ready event
    $scope.$on(Constants.Events.TASK_TEMPLATE_DATA_READY, function (event, data) {
        initTasks()
    })

    // Listener for table row selection / unselection events
    $scope.$on(Constants.Events.TABLE_ROW_SELECT, function (event, params) {
        // Clear selection array
        vm.selection = []

        // Push all seelcted leads
        params.selectedIndexes.forEach(function (selectedIdx) {
            vm.selection.push(vm.tasks[selectedIdx])
        })
    })

    // Init View
    initTasks()
})
