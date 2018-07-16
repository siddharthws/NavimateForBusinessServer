/**
 * Created by Rohan on 6/13/2018.
 */
app.service('ImageUploadService', function ($http, $q) {
    this.uploadImage = function (image, url) {
        var imageFormData = new FormData()
        imageFormData.append('image', image)

        //prepare a deffered object
        var deffered = $q.defer()
        $http.post(url, imageFormData, {
            transformRequest: angular.identity,
            headers: {'Content-Type': undefined}

        }).success(function (response) {
            deffered.resolve(response)
        }).error(function (response) {
            deffered.reject(response)
        })

        return deffered.promise
    }
})