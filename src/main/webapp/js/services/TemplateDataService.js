/**
 * Created by aroha on 14-01-2018.
 */
app.service('TemplateDataService', function($rootScope, $http, $localStorage, ToastService) {

    /* ----------------------------- INIT --------------------------------*/
    var vm = this
    vm.cache ={
        data: { forms:[],
                leads: [],
                tasks: []
        }
    }

    /* ----------------------------- APIs --------------------------------*/
    //API to get Template data
    vm.syncForms = function (){
        $rootScope.showWaitingDialog("Please wait while we are fetching forms...")
        $http({
            method:     'GET',
            url:        '/api/users/template',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken,
                'templateType':    Constants.Template.TYPE_FORM
            }
        }).then(
                function (response) {
                    $rootScope.hideWaitingDialog()

                    // Update cache data
                    vm.cache.data.forms = response.data.templates
                    $rootScope.$broadcast(Constants.Events.FORM_TEMPLATE_DATA_READY)
                },
                function (error) {
                    $rootScope.hideWaitingDialog()
                    ToastService.toast("Unable to load forms templates !!!")
                })
    }

    //API to get Lead Templates
    vm.syncLeads = function (){
        $http({
            method:     'GET',
            url:        '/api/users/template',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken,
                'templateType':    Constants.Template.TYPE_LEAD
            }
        }).then(
            function (response) {
                // Update cache data
                vm.cache.data.leads = response.data.templates

                // Broadcast Data ready event
                $rootScope.$broadcast(Constants.Events.LEAD_TEMPLATE_DATA_READY)
            },
            function (error) {
                ToastService.toast("Unable to load lead templates !!!")
            })
    }

    //API to get Task Templates
    vm.syncTasks = function (){
        $http({
            method:     'GET',
            url:        '/api/users/template',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken,
                'templateType':    Constants.Template.TYPE_TASK
            }
        }).then(
            function (response) {
                // Update cache data
                vm.cache.data.tasks = response.data.templates

                // Broadcast Data ready event
                $rootScope.$broadcast(Constants.Events.TASK_TEMPLATE_DATA_READY)
            },
            function (error) {
                ToastService.toast("Unable to load task templates !!!")
            })
    }

    // API to get template object by id
    vm.getTemplateById = function (id) {
        // Iterate through all form templates
        for (var i = 0; i < vm.cache.data.forms.length; i++) {
            // Get template
            var template = vm.cache.data.forms[i]
            if (template.id == id) {
                return template
            }
        }

        // Iterate through all lead templates
        for (var i = 0; i < vm.cache.data.leads.length; i++) {
            // Get template
            var template = vm.cache.data.leads[i]
            if (template.id == id) {
                return template
            }
        }

        // Iterate through all task templates
        for (var i = 0; i < vm.cache.data.tasks.length; i++) {
            // Get template
            var template = vm.cache.data.tasks[i]
            if (template.id == id) {
                return template
            }
        }

        return null
    }

    // API to get field object by id
    vm.getFieldById = function (id) {
        // Iterate through all form templates
        for (var i = 0; i < vm.cache.data.forms.length; i++) {
            // Get template
            var template = vm.cache.data.forms[i]

            for (var j = 0; j < template.fields.length; j++) {
                // Get field
                var field = template.fields[j]

                // Check for id
                if (id == field.id) {
                    return field
                }
            }
        }

        // Iterate through all lead templates
        for (var i = 0; i < vm.cache.data.leads.length; i++) {
            // Get template
            var template = vm.cache.data.leads[i]

            for (var j = 0; j < template.fields.length; j++) {
                // Get field
                var field = template.fields[j]

                // Check for id
                if (id == field.id) {
                    return field
                }
            }
        }

        // Iterate through all task templates
        for (var i = 0; i < vm.cache.data.tasks.length; i++) {
            // Get template
            var template = vm.cache.data.tasks[i]

            for (var j = 0; j < template.fields.length; j++) {
                // Get field
                var field = template.fields[j]

                // Check for id
                if (id == field.id) {
                    return field
                }
            }
        }

        return null
    }

})