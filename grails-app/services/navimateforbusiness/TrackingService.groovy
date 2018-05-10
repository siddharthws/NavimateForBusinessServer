package navimateforbusiness

import grails.gorm.transactions.Transactional
import org.grails.web.json.JSONObject

@Transactional
class TrackingService {
    // Service injection
    def stompSessionService

    // API to handle tracking request
    def startTracking(navimateforbusiness.WebsocketClient managerClient, User rep) {
        // Check if rep is online
        navimateforbusiness.WebsocketClient repClient = stompSessionService.getClientFromUserId(rep.id)
        if (!repClient || !repClient.session.isOpen()) {
            // Rep is offline. Send tracking update with offline status
            navimateforbusiness.TrackingObject trackObj = new navimateforbusiness.TrackingObject(rep: rep)
            handleTrackingUpdate(managerClient, trackObj)
            return
        }

        // Send tracking request to rep
        stompSessionService.sendMessage("/txc/start-tracking", repClient, null)
    }

    // API to handle tracking related messages
    def stopTracking(User rep) {
        // Check if rep is online
        navimateforbusiness.WebsocketClient repClient = stompSessionService.getClientFromUserId(rep.id)
        if (repClient && repClient.session.isOpen()) {
            // Send stop tracking request to rep
            stompSessionService.sendMessage("/txc/stop-tracking", repClient, null)
        }
    }

    def handleTrackingUpdate(navimateforbusiness.WebsocketClient managerClient,
                             navimateforbusiness.TrackingObject trackObj) {
        // Send error code to manager
        stompSessionService.sendMessage("/txc/tracking-update", managerClient, new JSONObject(toJson(trackObj)))
    }

    def fromJson(def json, User rep) {
        double lat = json.lat ?: 0
        double lng = json.lng ?: 0
        long timestamp = json.timestamp ?: 0
        float speed = json.speed ?: 0
        int status = json.status

        return new navimateforbusiness.TrackingObject(  position: new navimateforbusiness.LatLng(lat, lng),
                                                        lastUpdated: timestamp,
                                                        status: status,
                                                        rep: rep,
                                                        speed: speed)
    }

    def toJson(navimateforbusiness.TrackingObject trackObj) {
        return [
            lat: trackObj.position.lat,
            lng: trackObj.position.lng,
            speed: trackObj.speed,
            timestamp: trackObj.lastUpdated,
            status: trackObj.status,
            repId: trackObj.rep.id
        ]
    }
}
