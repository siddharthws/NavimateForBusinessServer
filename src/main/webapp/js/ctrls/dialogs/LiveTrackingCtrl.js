/**
 * Created by aroha on 15-09-2017.
 */

// Controller for Alert Dialog
app.controller('LiveTrackingCtrl', function ($scope, $rootScope, $mdDialog, $interval, $localStorage, ToastService, DialogService, reps) {
    var vm = this

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

        // Center map on this marker
        var marker = $scope.mapParams.markers[idx]
        $scope.$broadcast(Constants.Events.MAP_CENTER, {latitude: marker.latitude, longitude: marker.longitude})
    }

    /* ------------------------------- Local APIs -----------------------------------*/
    // WS Client Connection Callbacks
    function wsConnectSuccess () {
        // Subscribe to tracking channels
        wsClient.subscribe("/user/txc/tracking-update", trackingUpdateCb)
        wsClient.subscribe("/user/txc/tracking-error", trackingErrorCb)


        // Start periodic Data Refresh
        refreshCb = $interval(function () {
            // Do Nothing. Digest cycle runs automatically
        }, 1000)

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

        // Update rep properties
        rep.lastUpdateTimeMs = msgBody.timestamp
        rep.speed = msgBody.speed
        rep.status = Constants.Tracking.ERROR_NONE

        // Get marker object for this rep
        var marker = $scope.mapParams.markers[vm.reps.indexOf(rep)]

        // Update marker details
        marker.latitude = msgBody.latitude
        marker.longitude = msgBody.longitude

        // If this is the first update, set map center
        if (bFirstUpdate) {
            bFirstUpdate = false
            $scope.$broadcast(Constants.Events.MAP_CENTER, {latitude: marker.latitude, longitude: marker.longitude})
        }
    }

    function trackingErrorCb(message) {
        var msgBody = JSON.parse(message.body)

        // Get rep from message
        var rep = getRepById(msgBody.repId)

        // Update status
        rep.status = msgBody.errorCode
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

    /* ------------------------------- INIT -----------------------------------*/
    // Init Objects
    vm.reps = reps
    vm.selectedRep = vm.reps[0]
    var refreshCb = null
    var bFirstUpdate = true

    // Init web socket relates vars
    var socket = new SockJS('/ws-endpoint')
    var wsClient = Stomp.over(socket)

    // Init Map Parameters
    $scope.mapParams = {}
    $scope.mapParams.markers = []

    // Create marker for each
    vm.reps.forEach(function (rep) {
        // Add marker
        $scope.mapParams.markers.push({
            title: rep.name,
            latitude: 0,
            longitude: 0
        })

        // Add status to rep
        rep.status = Constants.Tracking.ERROR_WAITING
    })
    
    // Set event listeners
    $scope.$on(Constants.Events.MAP_MARKER_CLICK, function (event, params) {
        // Perform List click action
        vm.listItemClick(params.idx)
    })

    // Set Scope Destroy listener to close socket
    $scope.$on('$destroy', function () {
        // Close Websocket
        wsClient.disconnect()

        // Stop periodic updates
        if (refreshCb) {
            $interval.cancel(refreshCb)
        }
    })

    // Connect Websocket
    wsClient.connect({id: $localStorage.id}, wsConnectSuccess, wsConnectError)
})
