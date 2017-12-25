package navimateforbusiness.api

import grails.gorm.transactions.Transactional
import navimateforbusiness.User
import navimateforbusiness.WebsocketClient
import org.grails.web.json.JSONObject
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessageHeaderAccessor

class TrackingApiController {

    def trackingService
    def stompSessionService

    @Transactional
    @MessageMapping("/start-tracking")
    protected void startTracking(SimpMessageHeaderAccessor sha, Map messageMap) {
        // Get client
        def client = stompSessionService.getClientFromPrincipalName(sha.user.name)

        // Parse message to repIds
        def repIds = messageMap.reps
        repIds.each {repId ->
            // Get Rep
            User rep = User.findById(repId)

            // Request Tracking from rep
            trackingService.startTracking(client, rep)
        }
    }

    @MessageMapping("/tracking-update")
    protected void trackingUpdate(SimpMessageHeaderAccessor sha, Map messageMap) {
        // Get client
        WebsocketClient client = stompSessionService.getClientFromPrincipalName(sha.user.name)

        // Get rep
        User rep = client.user

        // Check if rep's manager is connected
        WebsocketClient managerClient = stompSessionService.getClientFromUserId(rep.manager.id)
        if (!managerClient || !managerClient.session.isOpen()) {
            // Disconnect rep
            trackingService.stopTracking(rep)
            return
        }

        // Handle Tracking update
        trackingService.handleTrackingUpdate(managerClient, rep, new JSONObject(messageMap))
    }

    @MessageMapping("/tracking-error")
    protected void trackingError(SimpMessageHeaderAccessor sha, Map messageMap) {
        // Get client
        WebsocketClient client = stompSessionService.getClientFromPrincipalName(sha.user.name)

        // Get rep
        User rep = client.user

        // Check if rep's manager is connected
        WebsocketClient managerClient = stompSessionService.getClientFromUserId(rep.manager.id)
        if (!managerClient || !managerClient.session.isOpen()) {
            // Disconnect rep
            trackingService.stopTracking(rep)
            return
        }

        // Handle Tracking update
        trackingService.handleTrackingError(managerClient, rep, new JSONObject(messageMap))
    }
}
