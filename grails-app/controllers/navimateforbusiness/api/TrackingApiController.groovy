package navimateforbusiness.api

import grails.gorm.transactions.Transactional
import navimateforbusiness.ApiException
import navimateforbusiness.Constants
import navimateforbusiness.Tracking
import navimateforbusiness.User
import navimateforbusiness.WebsocketClient
import org.grails.web.json.JSONObject
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessageHeaderAccessor

class TrackingApiController {

    def userService
    def trackingService
    def stompSessionService

    @Transactional
    @MessageMapping("/start-tracking")
    protected void startTracking(SimpMessageHeaderAccessor sha, Map messageMap) {
        // Get client
        def client = stompSessionService.getClientFromPrincipalName(sha.user.name)

        // Parse message to repIds
        def repIds = messageMap.reps
        client.reps.clear()
        repIds.each {repId ->
            // Get Rep
            User rep = userService.getRepForUserById(client.user, repId)
            client.reps.push(rep)

            // Request Tracking from rep
            trackingService.startTracking(client, rep)
        }
    }

    @MessageMapping("/tracking-update")
    protected void trackingUpdate(SimpMessageHeaderAccessor sha, Map messageMap) {
        // Get client
        WebsocketClient repClient = stompSessionService.getClientFromPrincipalName(sha.user.name)
        if (!repClient) {
            // Throw Exception
            throw new ApiException("Rep's client not found for tracking update", Constants.HttpCodes.CONFLICT)
        }

        // Check if anyone has requested this rep's tracking
        List<WebsocketClient> managerClients = stompSessionService.getClientsFromRep(repClient.user)
        if (!managerClients) {
            // Stop tracking rep
            trackingService.stopTracking(repClient.user)
            return
        }

        // Parse Tracking object
        JSONObject msgJson = new JSONObject(messageMap)
        Tracking trackObj = trackingService.fromJson(msgJson, repClient.user)

        // Send tracking update to each client
        managerClients.each {WebsocketClient managerClient ->
            // Send Update
            trackingService.handleTrackingUpdate(managerClient, trackObj)
        }
    }
}
