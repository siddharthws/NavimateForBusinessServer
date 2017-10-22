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

               var team = addTrackData()

                fakeData = function(){
                team[0].trackData.location.lat  +=0.2
                team[0].trackData.location.lng  +=0.2
                if (team[0].trackData.location.lat == 25 || team[0].trackData.location.lng == 80)
                {
                    team[0].trackData.location.lat =20 ,team[0].trackData.location.lng=75;
                }
                else
                {
                    console.log(team);
                }


            };
            liveTrack =  setInterval(fakeData, 1000);
            fakeData();
    }

    function addTrackData(){
        var trackData= {
            status: 'false',
            location: {
                lat: 20.0,
                lng: 75.0
            },
            speed: 0,
            lastUpdated : 0
        }

        for(var i = 0; i<team.length ;i++){
            team[i].trackData = trackData
        }

        return team

    }

})
