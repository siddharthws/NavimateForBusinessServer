/**
 * Created by Siddharth on 03-07-2018.
 */

app.controller('MultiselectFilterCtrl', function (  $scope, $timeout,
                                                    ObjMultiselect, DialogService) {
    /* ------------------------------ INIT --------------------------------- */
    var vm = this

    // Init local filter from scope's JSON
    vm.filter = $scope.filter ? ObjMultiselect.fromJson($scope.filter) : new ObjMultiselect(Constants.Table.MS_EXCLUDE, [])

    /* ------------------------------ Public APIs --------------------------------- */
    vm.open = function () {
        var title = ""
        var url = ""

        // Get title and URL of dialog based on field type
        if ($scope.fieldType == Constants.Template.FIELD_TYPE_TEMPLATE) {
            title = 'Templates'
            url = '/api/manager/templates/search'
        } else if ($scope.fieldType == Constants.Template.FIELD_TYPE_REP) {
            title = 'Users'
            url = '/api/manager/team/searchReps'
        } else if ($scope.fieldType == Constants.Template.FIELD_TYPE_NON_REP) {
            title = 'Users'
            url = '/api/manager/team/searchNonReps'
        } else if ($scope.fieldType == Constants.Template.FIELD_TYPE_LEAD) {
            title = 'Leads'
            url = '/api/manager/leads/search'
        } else if ($scope.fieldType == Constants.Template.FIELD_TYPE_TASK) {
            title = 'Tasks'
            url = '/api/manager/tasks/search'
        } else if ($scope.fieldType == Constants.Template.FIELD_TYPE_PRODUCT) {
            title = 'Products'
            url = '/api/manager/products/search'
        }

        // Trigger Multiselect dialog
        DialogService.multiselect(title, url, vm.filter, dialogCb)
    }

    /* ------------------------------ Private APIs --------------------------------- */
    function dialogCb(filter) {
        // Update filter object
        $scope.filter = filter.toJson()
        vm.filter = filter

        // Trigger callback
        $timeout(function () {
            $scope.onFilterChanged({})
        }, 0)
    }

    /* ------------------------------ Listeners --------------------------------- */
    /* ------------------------------ Post INIT --------------------------------- */
})
