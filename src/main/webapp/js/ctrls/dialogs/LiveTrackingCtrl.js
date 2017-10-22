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
            var repData= {
                status: 'false',
                location: {
                    lat: 20.0,
                    lng: 75.0
                },
                speed: 0,
                lastUpdated : 0
            }
                fakeData = function(){
                repData.location.lat  +=0.2
                repData.location.lng  +=0.2
                if (repData.location.lat == 25 || repData.location.lng == 80)
                {
                    repData.location.lat =20 ,repData.location.lng=75;
                }
                else
                {
                    console.log(repData);
                }
            };
            liveTrack =  setInterval(fakeData, 1000);
            fakeData();
    }
})
