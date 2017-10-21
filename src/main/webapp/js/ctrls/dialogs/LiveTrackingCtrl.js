/**
 * Created by aroha on 15-09-2017.
 */

// Controller for Alert Dialog
app.controller('LiveTrackingCtrl', function ($scope , $mdDialog) {

    var liveTrack

    $scope.cancel = function () {
        $mdDialog.hide()
        clearInterval(liveTrack)
    }

    $scope.lead = function () {
            var leadData= {
                status: 'false',
                location: {
                    lat: 20.0,
                    lng: 75.0
                },
                speed: 0,
                lastUpdated : 0
            }
                fakeData = function(){
                leadData.location.lat  +=0.2
                leadData.location.lng  +=0.2
                if (leadData.location.lat == 25 || leadData.location.lng == 80)
                {
                    leadData.location.lat =20 ,leadData.location.lng=75;
                }
                else
                {
                    console.log(leadData);
                }
            };
            liveTrack =  setInterval(fakeData, 1000);
            fakeData();
    }
})
