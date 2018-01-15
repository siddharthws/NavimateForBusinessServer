/**
 * Created by aroha on 14-01-2018.
 */
app.service('TemplateDataService', function($rootScope, $http, $localStorage, ToastService) {

    /* ----------------------------- INIT --------------------------------*/
    var vm = this
    vm.cache ={
        data: { forms:[] }
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

})