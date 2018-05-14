/**
 * Created by aroha on 14-01-2018.
 */
app.service('TemplateService', function($q, $http, $localStorage, ObjTemplate) {
    /* ----------------------------- INIT --------------------------------*/
    var vm = this
    vm.cache = []

    /* ----------------------------- Public APIs --------------------------------*/
    // Method to reset Service
    vm.reset = function () {
        // Reset cache
        vm.cache = []
    }

    //API to sync all templates
    vm.sync = function (){
        // Prepare deferred object
        var deferred = $q.defer()

        $http({
            method:     'POST',
            url:        '/api/manager/templates/getAll',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            }
        }).then(
            // Success callback
            function (response) {
                // Reset cache
                vm.cache = []

                // Parse response into Template objects
                response.data.forEach(function (templateJson) {
                    vm.cache.push(ObjTemplate.fromJson(templateJson))
                })

                // Resolve promise
                deferred.resolve()
            },
            // Error callback
            function (error) {
                // Reject promise
                deferred.reject()
            })

        // Return promise
        return deferred.promise
    }

    //API to edit templates
    vm.edit = function (templates){
        // Prepare deferred object
        var deferred = $q.defer()

        // Convert templates to JSON
        var templatesJson = []
        templates.forEach(function (template) {
            templatesJson.push(ObjTemplate.toJson(template))
        })

        $http({
            method:     'POST',
            url:        '/api/admin/templates/edit',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            },
            data: {
                templates: templatesJson
            }
        }).then(
            // Success callback
            function (response) {
                // Resolve promise
                deferred.resolve()
            },
            // Error callback
            function (error) {
                // Reject promise
                deferred.reject(error.data.error)
            })

        // Return promise
        return deferred.promise
    }

    // API to get template object by id
    vm.getById = function (id) {
        // Iterate through all form templates
        for (var i = 0; i < vm.cache.length; i++) {
            // Get template
            var template = vm.cache[i]

            // Return object id ID matches
            if (template.id == id) {
                return template
            }
        }

        return null
    }

    // API to get field object by id
    vm.getFieldById = function (id) {
        // Iterate through all form templates
        for (var i = 0; i < vm.cache.length; i++) {
            // Get template
            var template = vm.cache[i]

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

    // Methods to get specific types of templates
    vm.getByType = function (type) {
        var templates = []

        // Get all templates of given type
        vm.cache.forEach(function (template) {
            if (template.type == type) {
                templates.push(template)
            }
        })

        return templates
    }

    /* ----------------------------- Private APIs --------------------------------*/
})
