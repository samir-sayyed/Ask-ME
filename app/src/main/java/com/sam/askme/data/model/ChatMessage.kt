
package com.sam.askme.data.model

import java.util.UUID

enum class Participant {
    ME, ASKME, ERROR
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    var text: String = "",
    val participant: Participant = Participant.ME,
    var isPending: Boolean = false,
    var isMessageFinished: Boolean = false
)
