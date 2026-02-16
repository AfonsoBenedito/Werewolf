package com.afonsobenedito.werewolf.web.service

import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

@Service
class GameNotificationService(
    private val messagingTemplate: SimpMessagingTemplate
) {

    fun notifyGameUpdate(gameId: String) {
        messagingTemplate.convertAndSend("/topic/game/$gameId", "UPDATE")
    }

    fun notifyGameEnded(gameId: String) {
        messagingTemplate.convertAndSend("/topic/game/$gameId", "ENDED")
    }
}
