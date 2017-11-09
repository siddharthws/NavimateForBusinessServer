package navimateforbusiness.api

import grails.converters.JSON
import navimateforbusiness.ApiException
import navimateforbusiness.Constants
import navimateforbusiness.Marshaller
import navimateforbusiness.TrackingObject
import navimateforbusiness.User

class TrackingApiController {

    def fcmService
    def authService
    private static HashMap<Integer, ArrayList<TrackingObject>> trackMap = new HashMap<>()

    def start() {
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Create tracking object for every rep & add to track map
        ArrayList<TrackingObject> trackObjs = new ArrayList<>()
        def repsJson = request.JSON.reps
        def fcms = []
        repsJson.each {repJson ->
            User rep = User.findById(repJson.id)
            if (rep) {
                trackObjs.push(new TrackingObject(rep: rep))
                fcms.push(rep.fcmId)
            }
        }
        trackMap.put(user.id, trackObjs)

        // Send FCM message to all reps
        fcms.each {fcm ->
            fcmService.trackApp(fcm)
        }

        def resp = [success: true]
        render resp as JSON
    }

    def refresh() {
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get Tracking Objects for this manager
        ArrayList<TrackingObject> trackObjs = trackMap.get(user.id)
        if (!trackObjs) {
            throw new ApiException("Tracking not enabled for manager", Constants.HttpCodes.BAD_REQUEST)
        }

        // Update Status of rep is it is unavailable
        def repIdsJson = request.JSON.reps
        def fcms = []
        repIdsJson.each {repId ->
            trackObjs.each {trackObj ->
                if (trackObj.rep.id == repId) {
                    if (trackObj.status == Constants.Tracking.STATUS_UNAVAILABLE) {
                        User rep = User.findById(repId)
                        fcms.push(rep.fcmId)
                        trackObj.lastUpdated = System.currentTimeMillis()
                        trackObj.status = Constants.Tracking.STATUS_WAITING
                    }
                }
            }
        }

        // Send FCMs to all unavailable reps
        fcms.each {fcm ->
            fcmService.trackApp(fcm)
        }

        def resp = [success: true]
        render resp as JSON
    }

    def stop() {
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Remove user data form track map
        trackMap.remove(user.id)
    }

    def postData() {
        def id = request.getHeader("id")
        User rep = User.findById(id)
        if (!rep) {
            throw new ApiException("Unauthorized", Constants.HttpCodes.UNAUTHORIZED)
        }

        // Get Tracking Objects for this manager
        ArrayList<TrackingObject> trackObjs = trackMap.get(rep.manager.id)
        if (!trackObjs) {
            throw new ApiException("Tracking not enabled for this rep's manager", Constants.HttpCodes.BAD_REQUEST)
        }

        // Check is user's tracking is requested by manager
        boolean bRepFound = false
        trackObjs.each {trackObj ->
             if (trackObj.rep.id == rep.id) {
                 // Update tracking data
                 trackObj.position.latitude = request.JSON.latitude
                 trackObj.position.longitude = request.JSON.longitude
                 trackObj.lastUpdated = System.currentTimeMillis()
                 trackObj.status = Constants.Tracking.STATUS_AVAILABLE
                 trackObj.speed = request.JSON.speed
                 bRepFound = true
             }
        }

        // Throw error if rep's tracking is not required
        if (!bRepFound) {
            throw new ApiException("Tracking not required for rep", Constants.HttpCodes.BAD_REQUEST)
        }

        def resp = [success: true]
        render resp as JSON
    }

    def getData() {
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get Tracking Objects for this manager
        ArrayList<TrackingObject> trackObjs = trackMap.get(user.id)
        if (!trackObjs) {
            throw new ApiException("Tracking not enabled for manager", Constants.HttpCodes.BAD_REQUEST)
        }

        // Put tracking data for each rep in response
        def resp = []
        trackObjs.each {trackObj ->
            // Check if status update to Unavailable is required
            long currentTime = System.currentTimeMillis()
            int elapsedTimeS = (currentTime - trackObj.lastUpdated) / 1000
            if (elapsedTimeS > Constants.Tracking.MAX_UPDATE_WAIT_TIME_S) {
                trackObj.status = Constants.Tracking.STATUS_UNAVAILABLE
            }

            // Add data to response
            resp.push(Marshaller.serializeTrackObj(trackObj))
        }
        render resp as JSON
    }
}
