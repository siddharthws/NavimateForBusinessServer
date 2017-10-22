/**
 * Created by aroha on 15-09-2017.
 */

// Controller for Alert Dialog
app.controller('LiveTrackingCtrl', function ($scope, $mdDialog, team) {

    var liveTrack
    $scope.team =  team
    $scope.cancel = function () {
        $mdDialog.hide()
        clearInterval(liveTrack)
    }

    $scope.temp = function () {

                 initTrackData()

                fakeData = function(){
                    $scope.team[0].trackData.location.lat  +=0.2
                    $scope.team[0].trackData.location.lng  +=0.2
                if ($scope.team[0].trackData.location.lat == 25 || $scope.team[0].trackData.location.lng == 80)
                {
                    $scope.team[0].trackData.location.lat =20 ,$scope.team[0].trackData.location.lng=75;
                }
                else
                {
                    console.log($scope.team);
                }


            };
            liveTrack =  setInterval(fakeData, 1000);
            fakeData();
    }

    function initTrackData(){
        var trackData= {
            status: false,
            location: {
                lat: 20.0,
                lng: 75.0
            },
            speed: 0,
            lastUpdated : 0
        }

        for(var i = 0; i<$scope.team.length ;i++){
            $scope.team[i].trackData = trackData
        }
    }

    $scope.temp()
})
