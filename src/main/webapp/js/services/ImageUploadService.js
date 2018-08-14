/**
 * Created by Rohan on 6/13/2018.
 */
app.service('ImageUploadService', function ($http, $q, $localStorage ) {

    this.uploadImage = function (url, image) {

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
})