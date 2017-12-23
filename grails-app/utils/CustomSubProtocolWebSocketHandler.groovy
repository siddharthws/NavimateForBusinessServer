package navimateforbusiness

import org.springframework.messaging.MessageChannel
import org.springframework.messaging.SubscribableChannel
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.messaging.SubProtocolWebSocketHandler

/**
 * Created by Siddharth on 21-12-2017.
 */
class CustomSubProtocolWebSocketHandler extends SubProtocolWebSocketHandler {
    // Default Constructor
    CustomSubProtocolWebSocketHandler(MessageChannel clientInboundChannel,
                                      SubscribableChannel clientOutboundChannel) {
        super(clientInboundChannel, clientOutboundChannel)
    }

    @Override
    void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Save new WebSocketSession in service
        // Session object is needed to close connections from server
        StompSessionService.newSession(session)

        super.afterConnectionEstablished(session)
    }
}
