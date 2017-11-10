/**
 * Created by Siddharth on 10-11-2017.
 */

// Controller for Photo Viewer Dialog
app.controller('PhotoViewerCtrl', function ($scope, $rootScope, $http, $localStorage, filename) {

    $scope.image = []

    $rootScope.showWaitingDialog("Getting Photo..")
    // Get Photo
    $http({
        method:     'GET',
        url:        '/api/photos/get',
        headers:    {
            'X-Auth-Token':    $localStorage.accessToken
        },
        params: {
            'filename': filename
        }
    })
    .then(
        function (response) {
            $rootScope.hideWaitingDialog()
            $scope.image = response.data.image
        },
        function (error) {
            $rootScope.hideWaitingDialog()
            console.log(error)
        }
    )
})
