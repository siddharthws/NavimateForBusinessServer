package navimateforbusiness

import org.springframework.http.server.ServerHttpRequest
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.support.DefaultHandshakeHandler

import java.security.Principal

/**
 * Created by Siddharth on 22-12-2017.
 */
class CustomHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(ServerHttpRequest request,
                                      WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
        // Generate principal with UUID as name
        return new navimateforbusiness.StompPrincipal(UUID.randomUUID().toString())
    }
}
