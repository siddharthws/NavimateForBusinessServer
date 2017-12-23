/**
 * Created by Siddharth on 21-12-2017.
 */
package navimateforbusiness

import grails.plugin.springwebsocket.GrailsSimpAnnotationMethodMessageHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry

@Configuration
@EnableWebSocketMessageBroker
class DefaultWebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {
    @Override
    void configureMessageBroker(MessageBrokerRegistry messageBrokerRegistry) {
        // Set outgoing message channel prefix
        messageBrokerRegistry.enableSimpleBroker( "/txc")

        // Set incoming message channel prefix
        messageBrokerRegistry.setApplicationDestinationPrefixes "/rxc"
    }

    @Override
    void registerStompEndpoints(StompEndpointRegistry stompEndpointRegistry) {
        stompEndpointRegistry.addEndpoint("/ws-endpoint") // Set websocket endpoint to connect to
                            .setHandshakeHandler(new navimateforbusiness.CustomHandshakeHandler()) // Set custom handshake handler
                            .withSockJS() // Add Sock JS support for frontend
    }

    // Bean to set annotation mappings
    @Bean
    GrailsSimpAnnotationMethodMessageHandler grailsSimpAnnotationMethodMessageHandler(
            MessageChannel clientInboundChannel,
            MessageChannel clientOutboundChannel,
            SimpMessagingTemplate brokerMessagingTemplate
    ) {
        def handler = new GrailsSimpAnnotationMethodMessageHandler(clientInboundChannel, clientOutboundChannel, brokerMessagingTemplate)
        handler.destinationPrefixes = ["/rxc"]
        return handler
    }

    // Bean to set custom websocket handler protocol handler
    @Bean
    WebSocketHandler subProtocolWebSocketHandler(
            MessageChannel clientInboundChannel,
            MessageChannel clientOutboundChannel) {
        return new navimateforbusiness.CustomSubProtocolWebSocketHandler(clientInboundChannel, clientOutboundChannel)
    }
}
