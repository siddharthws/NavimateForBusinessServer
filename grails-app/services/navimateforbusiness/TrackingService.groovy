package navimateforbusiness

import grails.gorm.transactions.Transactional
import org.grails.web.json.JSONObject

import com.mongodb.client.FindIterable
import static com.mongodb.client.model.Filters.and
import static com.mongodb.client.model.Filters.eq

@Transactional
class TrackingService {
    // Service injection
    def formService
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

        // prepare tracking object
        Tracking trackObj = getLatestForRep(rep)

        // Set status
        if (repClient && repClient.session.isOpen()) {
            trackObj.status = navimateforbusiness.Constants.Tracking.ERROR_WAITING
        } else {
            trackObj.status = navimateforbusiness.Constants.Tracking.ERROR_OFFLINE
        }

        // Send tracking update to manager
        handleTrackingUpdate(managerClient, trackObj)

        // Send tracking request to rep
        if (repClient && repClient.session.isOpen()) {
            stompSessionService.sendMessage("/txc/start-tracking", repClient, null)
        }
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
            timestamp:      trackObj.locUpdateTime.time,
            status:         trackObj.status,
            repId:          trackObj.repId
        ]
    }

    // Method to get a tracking object with latest location information for given rep
    private def getLatestForRep(User rep) {
        // Get tracking object of rep
        Tracking trackObj = getForRep(rep)
        if (!trackObj) {
            trackObj = new Tracking(accountId: rep.accountId, repId: rep.id)
        }

        // Get latest form submission of rep with a valid latlng
        def forms = formService.getForUser(rep)
        Form latestForm = forms.find {it -> it.latitude || it.longitude}

        // Update location params from form if tracking object is invalid / older
        if (latestForm && trackObj.locUpdateTime < latestForm.lastUpdated) {
            trackObj.lat             = latestForm.latitude
            trackObj.lng             = latestForm.longitude
            trackObj.locUpdateTime   = latestForm.lastUpdated
        }

        trackObj
    }
}
