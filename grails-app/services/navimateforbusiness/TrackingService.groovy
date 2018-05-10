package navimateforbusiness

import grails.gorm.transactions.Transactional
import org.grails.web.json.JSONObject

import com.mongodb.client.FindIterable
import static com.mongodb.client.model.Filters.and
import static com.mongodb.client.model.Filters.eq

@Transactional
class TrackingService {
    // Service injection
    def stompSessionService

    def getForRep(User rep) {
        // Prepare mongo filters
        def mongoFilters = []

        // Add accountId filter
        mongoFilters.push(eq("accountId", rep.accountId))

        // Add Rep ID Filter
        mongoFilters.push(eq("repId", rep.id))

        // Get results
        FindIterable fi = Tracking.find(and(mongoFilters))

        // Return response
        return fi[0]
    }

    // API to handle tracking request
    def startTracking(navimateforbusiness.WebsocketClient managerClient, User rep) {
        // Check if rep is online
        navimateforbusiness.WebsocketClient repClient = stompSessionService.getClientFromUserId(rep.id)
        if (!repClient || !repClient.session.isOpen()) {
            // Rep is offline. Send tracking update with offline status
            Tracking trackObj = new navimateforbusiness.Tracking(repId: rep.id)
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

    def handleTrackingUpdate(navimateforbusiness.WebsocketClient managerClient, Tracking trackObj) {
        // Send error code to manager
        stompSessionService.sendMessage("/txc/tracking-update", managerClient, new JSONObject(toJson(trackObj)))
    }

    def fromJson(def json, User rep) {
        // Find existing object or create a new one
        Tracking trackObj = getForRep(rep)
        if (!trackObj) {
            trackObj = new Tracking(accountId: rep.account.id, repId: rep.id, dateCreated: new Date())
        }

        // Update from JSON params
        trackObj.lat = json.lat ?: trackObj.lat
        trackObj.lng = json.lng ?: trackObj.lng
        trackObj.speed = json.speed ?: trackObj.speed
        trackObj.locUpdateTime = json.timestamp ? new Date(json.timestamp) : trackObj.locUpdateTime
        trackObj.status = json.status

        // Update last update time
        trackObj.lastUpdated = new Date()

        trackObj
    }

    def toJson(Tracking trackObj) {
        return [
            lat:            trackObj.lat,
            lng:            trackObj.lng,
            speed:          trackObj.speed,
            timestamp:      trackObj.locUpdateTime ? trackObj.locUpdateTime.time : 0,
            status:         trackObj.status,
            repId:          trackObj.repId
        ]
    }
}
