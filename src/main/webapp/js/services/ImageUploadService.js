/**
 * Created by Aroha on 6/13/2018.
 */
app.service('ImageUploadService', function ($http, $q, $localStorage, DialogService, FileService ) {
    /* ----------------------------- Globals --------------------------------*/
    var vm = this
    vm.companyIconUrl = null

    /* ----------------------------- Public APIs --------------------------------*/
    vm.uploadCompanyIcon = function (url, image) {
        var deferred = $q.defer()

        // Create form data to send
        var formData = new FormData()
        formData.append('image', image)

        // Perform http request
        $http({
            method: 'POST',
            url: url,
            headers: {
                'Content-Type': undefined,
                'X-Auth-Token': $localStorage.accessToken
            },
            data: formData
        }).then(
            // Success callback
            function (response) {
                deferred.resolve()
            },
            // Error callback
            function (error) {
                var errorMessage = error.data.error
                deferred.reject(errorMessage)
            }
        )

        // Return promise
        return deferred.promise
    }

    vm.getCompanyIcon = function () {
        var deferred = $q.defer()

        // Perform http request
        $http({
            method: 'GET',
            url: '/api/photos/getCompanyIcon',
            responseType: 'arraybuffer',
            headers: {
                'Content-Type': undefined,
                'X-Auth-Token': $localStorage.accessToken
            }
        }).then(
            // Success callback
            function (response) {
                vm.companyIconUrl = getPhotoUrl(response)
                deferred.resolve()
            },
            // Error callback
            function (error) {
                vm.companyIconUrl ="/static/images/ic_logo.png"
                var errorMessage = error.data.error
                deferred.reject(errorMessage)
            }
        )

        // Return promise
        return deferred.promise
    }

    /* ----------------------------- Public APIs --------------------------------*/
    function getPhotoUrl(response){
        var contentType = response.headers('Content-Type')

        // Create blob element
        var blob = new Blob([response.data], {type: contentType})
        var url = window.URL.createObjectURL(blob)

        return url
    }
})