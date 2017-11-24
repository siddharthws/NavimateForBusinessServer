/**
 * Created by Siddharth on 24-11-2017.
 */
app.controller('PhotosCtrl', function($scope, $rootScope, $localStorage, $stateParams, $http){
    /*------------------------------------ Scope APIs --------------------------------*/
    /*------------------------------------ Local APIs --------------------------------*/
    function init() {
        // Extract photo name
        var photoName = $stateParams.name

        if (!photoName) {
            $scope.bError = true
            return
        }

        // Get photo from server
        $rootScope.showWaitingDialog("Getting Photo..")
        // Get Photo
        $http({
            method:     'GET',
            url:        '/api/photos/get',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            },
            params: {
                'filename': photoName
            }
        })
        .then(
            function (response) {
                $rootScope.hideWaitingDialog()
                $scope.image = response.data.image
            },
            function (error) {
                $rootScope.hideWaitingDialog()
                $scope.bError = true
            }
        )
    }

    /*------------------------------------ INIT --------------------------------*/
    // Init Objects
    $scope.image = []
    $scope.bError = false

    init()
});
