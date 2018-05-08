/**
 * Created by Siddharth on 07-05-2018.
 */

app.service('LocationService', function() {
    /*------------------------------------ Init --------------------------------*/
    var vm = this

    // Init user's current location
    vm.curLocation = null

    /*------------------------------------ Public APIs --------------------------------*/
    vm.init = function () {
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(function (position) {
                // Set current Latlng cache
                vm.curLocation = new google.maps.LatLng(position.coords.latitude, position.coords.longitude)
            })
        }
    }
    /*------------------------------------ Private APIs --------------------------------*/
    /*------------------------------------ Post Init --------------------------------*/
})
