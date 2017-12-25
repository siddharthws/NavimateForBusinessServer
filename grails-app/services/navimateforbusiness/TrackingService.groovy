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
            // Rep is offline. Report error
            handleTrackingError(managerClient, rep, new JSONObject([errorCode: navimateforbusiness.Constants.Tracking.ERROR_OFFLINE]))
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

    def handleTrackingUpdate(navimateforbusiness.WebsocketClient managerClient, navimateforbusiness.User rep, JSONObject update) {
        // Add rep ID to update
        update.put("repId", rep.id)

        // Send error code to manager
        stompSessionService.sendMessage("/txc/tracking-update", managerClient, update)
    }

    def handleTrackingError(navimateforbusiness.WebsocketClient managerClient, navimateforbusiness.User rep, JSONObject error) {
        // Add rep ID to error
        error.put("repId", rep.id)

        // Send error to manager
        stompSessionService.sendMessage("/txc/tracking-error", managerClient, error)
    }
}
