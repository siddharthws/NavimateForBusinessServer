package navimateforbusiness.api

import grails.gorm.transactions.Transactional
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessageHeaderAccessor

@Transactional
class StompController {
    // Service injection
    def stompSessionService

    @MessageMapping('/heart-beat')
    protected void heartbeat(SimpMessageHeaderAccessor headerAccessor) {
        // Register Heart beat with session service
        stompSessionService.registerHeartbeat(headerAccessor.user)
    }
}
