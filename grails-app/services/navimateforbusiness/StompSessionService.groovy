package navimateforbusiness

import grails.gorm.transactions.Transactional
import org.grails.web.json.JSONObject
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.messaging.SessionConnectEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent

import java.security.Principal
import java.util.concurrent.CopyOnWriteArrayList

@Transactional
class StompSessionService {
    // List of active clients
    CopyOnWriteArrayList<navimateforbusiness.WebsocketClient> activeClients = []

    // Simple Message Broker to send messaged from server
    SimpMessagingTemplate brokerMessagingTemplate

    // Hack to maintain unmapped session temporarily
    // Happens because service's instance cannot be injected into CustomSubProtocolWebsocketHandler
    // Following data object and function help store session temporarily
    // These are removed when client's onConnected evenet happens or cleanup happens
    static CopyOnWriteArrayList<navimateforbusiness.WebsocketClient.Unmapped> unmappedSessions = []
    static void newSession (WebSocketSession session) {
        // Feed new entry to unmapped sessions
        unmappedSessions.push(
                new navimateforbusiness.WebsocketClient.Unmapped(
                    session: session,
                    createTimeMs: System.currentTimeMillis()))
    }

    // APIs to handle connect and disconnect events
    @EventListener
    void handleSessionConnectEvent(SessionConnectEvent event) {
        // Get Accessor
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage())

        // Find session in unmapped sessions
        def unmappedSession = unmappedSessions.find{it -> it.session.principal.name == sha.user.name}
        if (!unmappedSession) {
            throw new navimateforbusiness.ApiException("Could not find session ", navimateforbusiness.Constants.HttpCodes.INTERNAL_SERVER_ERROR)
        }

        // Remove from unmapped sessions
        unmappedSessions.remove(unmappedSession)

        // Get User
        Long id = Long.parseLong(sha.getFirstNativeHeader("id"))
        User user = User.findById(id)
        if (!user) {
            // Close session
            unmappedSession.session.close()

            // Throw error
            throw new navimateforbusiness.ApiException("Invalid User ID", navimateforbusiness.Constants.HttpCodes.INTERNAL_SERVER_ERROR)
        }

        // Close any old session that this user has
        navimateforbusiness.WebsocketClient client = getClientFromUserId(id)
        if (client) {
            client.session.close()
        }

        // Prepare new client object
        client = new navimateforbusiness.WebsocketClient(   session: unmappedSession.session,
                                                            user: user,
                                                            lastHeartbeatTimeMs: System.currentTimeMillis())

        // Add to active clients
        activeClients.push(client)
    }

    @EventListener
    void handleSessionDisconnectEvent(SessionDisconnectEvent event) {
        // Get Accessor
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage())

        // Remove from active clients
        def client = getClientFromPrincipalName(sha.user.name)
        if (client) {
            activeClients.remove(client)
        }
    }

    // API to clean sessions
    // Removes unmapped session older than 30 seconds
    // Remove active rep clients whose last heartbeat was 5 mins ago
    // Called periodically by StompPeriodicJob
    void cleanSessions () {
        // Get current time
        long currentTimeMs = System.currentTimeMillis()

        // Check which unmapped sessions have expired (added 10 seconds ago and still present)
        def expiredUnmappedSessions = []
        unmappedSessions.each {unmappedSession ->
            // Check if old session
            if ((currentTimeMs - unmappedSession.createTimeMs) > 30 * 1000) {
                expiredUnmappedSessions.push(unmappedSession)
            }
        }

        // Remove expired unmapped sessions
        expiredUnmappedSessions.each {unmappedSession ->
            // Close session
            unmappedSession.session.close()

            // Remove
            unmappedSessions.remove(unmappedSession)
        }

        // Close rep client sessions whose heartbeat has expired (2 minutes)
        activeClients.each {client ->
            if (client.user.role == navimateforbusiness.Role.REP) {
                if ((currentTimeMs - client.lastHeartbeatTimeMs) > 2*60*1000) {
                    client.session.close()
                }
            }
        }
    }

    // API to get websocket client from principal name
    def getClientFromPrincipalName(name) {
        activeClients.find {it -> it.session.principal.name == name}
    }

    // API to get websocket client from user id
    def getClientFromUserId(Long id) {
        activeClients.find {it -> it.user.id == id}
    }

    // API to get websocket client from user id
    def getClientsFromRep(User rep) {
        activeClients.findAll {it -> it.hasRep(rep) && it.session.isOpen()}
    }

    // API to register heartbeat for user
    def registerHeartbeat(Principal principal) {
        // Find Client
        def client = getClientFromPrincipalName(principal.name)
        if (!client) {
            throw new navimateforbusiness.ApiException("Non existent client for heartbeat", navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
        }

        // Update last heartbeat
        client.lastHeartbeatTimeMs = System.currentTimeMillis()
    }

    // APi to send message to specific user on a destination
    def sendMessage(String destination, navimateforbusiness.WebsocketClient client, JSONObject message) {
        // Validate message
        if (!message) {
            message = new JSONObject()
        }

        // Send to client
        brokerMessagingTemplate.convertAndSendToUser(client.session.principal.name, destination, message)
    }
}
