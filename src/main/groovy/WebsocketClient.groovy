package navimateforbusiness

import org.springframework.web.socket.WebSocketSession

/**
 * Created by Siddharth on 22-12-2017.
 */
// class to store websocket client information
class WebsocketClient {
    // Inner class for temporary unmapped sessions
    static class Unmapped {
        WebSocketSession session
        long createTimeMs
    }

    WebSocketSession session
    User user
    long lastHeartbeatTimeMs = 0L
}
