/**
 * Created by aroha on 15-09-2017.
 */

// Controller for Alert Dialog
app.controller('LiveTrackingCtrl', function (   $scope, $rootScope, $mdDialog, $interval, $localStorage,
                                                ToastService, DialogService,
                                                ObjMap, ObjMarker,
                                                reps) {
    /* ------------------------------- Scope APIs -----------------------------------*/
    var vm = this

    // Init user info
    vm.reps         = reps
    vm.selectedRep  = vm.reps[0]

    // Init web socket relates vars
    var socket = new SockJS('/ws-endpoint')
    var wsClient = Stomp.over(socket)
    var bFirstUpdate = true

    // Refresh Callback to trigger screen update
    var refreshCb = null

    // Create marker for each rep
    var markers = []
    vm.reps.forEach(function (rep) {
        // Create marker object
        markers.push(new ObjMarker(rep.id, rep.name, new google.maps.LatLng(0, 0)))

        // Add status to rep
        rep.status = Constants.Tracking.ERROR_WAITING
    })

    // Create map object
    vm.map = new ObjMap(markers)

    /* ------------------------------- Scope APIs -----------------------------------*/
    // APIs to refresh rep status
    vm.refreshAll = function () {
        var repIds = []

        // Prepare Rep ID Array
        vm.reps.forEach(function (rep) {
            repIds.push(rep.id)
            rep.status = Constants.Tracking.ERROR_WAITING
        })

        // Send request on socket
        wsClient.send("/rxc/start-tracking", {}, JSON.stringify({reps: repIds}))
    }

    vm.close = function () {
        //wsClient.disconnect()
        $mdDialog.hide()
    }
    
    // API to handle list click event
    vm.listItemClick = function (idx) {
        // Update Selected Rep
        vm.selectedRep = vm.reps[idx]

        // Set selected marker
        vm.map.selectedMarker = vm.map.markers[idx]

        // Center map on this marker
        vm.map.setCenter(vm.map.selectedMarker.position)
    }

    vm.onMarkerClick = function (idx) {
        // Update Selected Rep
        vm.selectedRep = vm.reps[idx]
    }

    /* ------------------------------- Local APIs -----------------------------------*/
    // WS Client Connection Callbacks
    function wsConnectSuccess () {
        // Subscribe to tracking channels
        wsClient.subscribe("/user/txc/tracking-update", trackingUpdateCb)

        // Start refresh callbacks to trigger screen updates
        refreshCb = $interval(function () {}, 1000)

        // Send Refresh Reps request
        vm.refreshAll()
    }

    function wsConnectError () {
        // Show alert dialog with error
        DialogService.alert("Unable to start live tracking. Your session may have expired !!!")
    }

    // Tracking related callbacks
    function trackingUpdateCb (message) {
        var msgBody = JSON.parse(message.body)

        // Get rep from message ID
        var rep = getRepById(msgBody.repId)

        // parse params as per status
        rep.status = msgBody.status
        if (rep.status == Constants.Tracking.ERROR_NONE) {
            rep.lastUpdateTimeMs = msgBody.timestamp
            rep.speed = msgBody.speed

            // Update marker position
            var marker = vm.map.markers[vm.reps.indexOf(rep)]
            marker.position = new google.maps.LatLng(msgBody.lat, msgBody.lng)

            // If this is the first update, set map center
            if (bFirstUpdate) {
                bFirstUpdate = false
                vm.map.setCenter(marker.position)
            }
        }
    }

    // APi to get rep by ID
    function getRepById(id) {
        var rep = null
        for (var i = 0; i < vm.reps.length; i++) {
            if (vm.reps[i].id == id) {
                rep = vm.reps[i]
            }
        }

        return rep
    }

    /* ------------------------------- Event listeners -----------------------------------*/
    // Set Scope Destroy listener so that socket is always closed when window closes
    $scope.$on('$destroy', function () {
        // Stop periodic updates
        if (refreshCb) {
            $interval.cancel(refreshCb)
        }

        // Disconnect client
        wsClient.disconnect()
    })

    /* ------------------------------- Post Init -----------------------------------*/
    // Connect Websocket
    wsClient.connect({id: $localStorage.id}, wsConnectSuccess, wsConnectError)
})
