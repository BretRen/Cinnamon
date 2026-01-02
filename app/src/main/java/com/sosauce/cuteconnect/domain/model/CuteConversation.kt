package com.sosauce.cuteconnect.domain.model

/**
 * A conversation.
 * @param recipients list of all people in this conversation (as phone numbers, excluding ourselves) only multiple if is a group chat
 * @param contacts contacts associated to this thread
 */
data class CuteConversation(
    val threadId: Long = 0,
    val recipients: List<String> = emptyList(),
    val snippet: String = "",
    val date: Long = 0,
    val read: Boolean = true,
    val isSenderBlocked: Boolean = false,
    val isGroupChat: Boolean = false
)