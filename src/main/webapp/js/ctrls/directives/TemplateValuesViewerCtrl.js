/**
 * Created by Siddharth on 04-06-2018.
 */

app.controller('TemplateValuesViewerCtrl', function (   $rootScope, $http, $localStorage,
                                                        FileService, DialogService) {
    var vm = this

    // API to show photo on new page
    vm.showImage = function (value) {
        // Get photo from server
        $rootScope.showWaitingDialog("Downloading..")

        // Get Photo
        $http({
            method:     'GET',
            url:        '/api/photos/get',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            },
            responseType: 'arraybuffer',
            params: {
                'filename': value.value
            }
        }).then(
            function (response) {
                FileService.downloadPhoto(response, value.field.title + '_' + value.value)
                $rootScope.hideWaitingDialog()
            },
            function (error) {
                DialogService.alert("Error while downloading !!!")
                $rootScope.hideWaitingDialog()
            }
        )
    }

    vm.viewProduct = function (id) {
        DialogService.productViewer(id)
    }
})
