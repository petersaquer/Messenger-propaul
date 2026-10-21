package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.LocationData
import com.example.data.model.Message
import com.example.data.model.MessageStatus
import com.example.data.model.MessageType
import com.example.ui.theme.TelegramAccent
import com.example.ui.theme.TelegramBlue
import com.example.ui.theme.TelegramBubbleDark
import com.example.ui.theme.TelegramBubbleLight

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: Message,
    isDarkTheme: Boolean = false,
    onReply: ((Message) -> Unit)? = null,
    onReaction: ((Message, String) -> Unit)? = null,
    onVotePoll: ((String, Int) -> Unit)? = null,
    onOpenViewOnce: ((Message) -> Unit)? = null,
    onOpenMap: ((LocationData) -> Unit)? = null,
    onOpenMedia: ((String, Boolean, String) -> Unit)? = null,
    onOpenContact: ((String, String) -> Unit)? = null,
    onEditMessage: ((Message) -> Unit)? = null,
    onDeleteForMe: ((Message) -> Unit)? = null,
    onDeleteForEveryone: ((Message) -> Unit)? = null,
    onToggleStar: ((Message) -> Unit)? = null,
    onForwardMessage: ((Message) -> Unit)? = null,
    onReportMessage: ((Message) -> Unit)? = null
) {
    val isOut = message.isOutgoing
    var showMenu by remember { mutableStateOf(false) }

    val bubbleColor = if (isOut) {
        if (isDarkTheme) TelegramBubbleDark else TelegramBubbleLight
    } else {
        if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant else Color.White
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp),
        contentAlignment = if (isOut) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isOut) 16.dp else 2.dp,
                bottomEnd = if (isOut) 2.dp else 16.dp
            ),
            color = bubbleColor,
            shadowElevation = 1.dp,
            modifier = Modifier
                .widthIn(max = 320.dp)
                .testTag("message_bubble_${message.id}")
                .combinedClickable(
                    onClick = {
                        if (message.isViewOnce && !message.hasBeenViewed) {
                            onOpenViewOnce?.invoke(message)
                        }
                    },
                    onLongClick = { showMenu = true }
                )
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                // Sender name for incoming group messages
                if (!isOut && !message.senderName.isNullOrBlank()) {
                    Text(
                        text = message.senderName ?: "",
                        fontWeight = FontWeight.Bold,
                        color = TelegramBlue,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                // Reply preview header
                if (message.replyToText != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.05f))
                            .padding(6.dp)
                    ) {
                        Column {
                            Text(
                                text = message.replyToSender ?: "Mensaje",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = TelegramBlue
                            )
                            Text(
                                text = message.replyToText ?: "",
                                fontSize = 11.sp,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Deleted message state
                if (message.isDeletedForEveryone) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "🚫 Este mensaje fue eliminado",
                            fontStyle = FontStyle.Italic,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else if (message.isViewOnce) {
                    // View once message
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = TelegramBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (message.hasBeenViewed) "Foto abierta" else "Foto efímera (toca para ver)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (message.hasBeenViewed) MaterialTheme.colorScheme.onSurfaceVariant else TelegramBlue
                        )
                    }
                } else {
                    // Standard message types
                    when (message.type) {
                        MessageType.LOCATION, MessageType.LIVE_LOCATION -> {
                            val loc = message.location
                            if (loc != null) {
                                LocationMessageWidget(
                                    location = loc,
                                    isOutgoing = isOut,
                                    onClick = { onOpenMap?.invoke(loc) }
                                )
                            } else {
                                Text(text = message.text, fontSize = 15.sp)
                            }
                        }

                        MessageType.POLL -> {
                            val poll = message.poll
                            if (poll != null) {
                                PollWidget(
                                    poll = poll,
                                    onVote = { optId -> onVotePoll?.invoke(message.id, optId) }
                                )
                            } else {
                                Text(text = message.text, fontSize = 15.sp)
                            }
                        }

                        MessageType.GIF -> {
                            val gifUrl = message.gifUrl ?: message.mediaUrl
                            if (!gifUrl.isNullOrBlank()) {
                                GifMessageWidget(
                                    gifUrl = gifUrl,
                                    title = message.text.ifBlank { "GIF" }
                                )
                            } else {
                                Text(text = message.text, fontSize = 15.sp)
                            }
                        }

                        MessageType.PHOTO, MessageType.VIDEO -> {
                            val media = message.mediaUrl ?: message.photoUrl ?: message.videoUrl
                            if (!media.isNullOrBlank()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .combinedClickable(
                                            onClick = {
                                                onOpenMedia?.invoke(
                                                    media,
                                                    message.type == MessageType.VIDEO,
                                                    message.text
                                                )
                                            }
                                        )
                                ) {
                                    AsyncImage(
                                        model = media,
                                        contentDescription = "Multimedia",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                            if (message.text.isNotBlank() && message.text != "📷 Foto" && message.text != "🎥 Video") {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = message.text, fontSize = 15.sp)
                            }
                        }

                        MessageType.VOICE -> {
                            VoicePlayer(
                                durationSeconds = message.voiceDurationSeconds ?: message.durationSeconds ?: 0,
                                waveform = message.voiceWaveform,
                                isOutgoing = isOut,
                                voiceFilePath = message.voiceFilePath ?: message.mediaUrl
                            )
                        }

                        MessageType.CONTACT -> {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable {
                                        val name = message.contactName ?: message.text
                                        val phone = message.contactPhone ?: ""
                                        onOpenContact?.invoke(name, phone)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = TelegramBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = message.contactName ?: message.text,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        if (!message.contactPhone.isNullOrBlank()) {
                                            Text(
                                                text = message.contactPhone ?: "",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        else -> {
                            // Text
                            Text(
                                text = message.text,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Footer: reactions + timestamp + delivery ticks
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (message.isEdited) {
                        Text(
                            text = "editado",
                            fontSize = 10.sp,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    if (message.isStarred) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    Text(
                        text = message.timestamp,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (isOut) {
                        Spacer(modifier = Modifier.width(4.dp))
                        when (message.status) {
                            MessageStatus.READ -> {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = "Leído",
                                    modifier = Modifier.size(14.dp),
                                    tint = TelegramBlue
                                )
                            }
                            MessageStatus.DELIVERED -> {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = "Entregado",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            MessageStatus.SENT -> {
                                Icon(
                                    imageVector = Icons.Default.Done,
                                    contentDescription = "Enviado",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            MessageStatus.SENDING -> {
                                Text("⏱", fontSize = 10.sp)
                            }
                        }
                    }
                }

                // Quick Reactions Row if present
                if (message.reactions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        message.reactions.forEach { (emoji, count) ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.combinedClickable {
                                    onReaction?.invoke(message, emoji)
                                }
                            ) {
                                Text(
                                    text = "$emoji $count",
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dropdown Action Menu
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Responder") },
                leadingIcon = { Icon(Icons.Default.Reply, contentDescription = null) },
                onClick = {
                    showMenu = false
                    onReply?.invoke(message)
                }
            )

            DropdownMenuItem(
                text = { Text(if (message.isStarred) "Quitar de destacados" else "Destacar") },
                leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) },
                onClick = {
                    showMenu = false
                    onToggleStar?.invoke(message)
                }
            )

            DropdownMenuItem(
                text = { Text("Reaccionar ❤️") },
                onClick = {
                    showMenu = false
                    onReaction?.invoke(message, "❤️")
                }
            )

            DropdownMenuItem(
                text = { Text("Reaccionar 👍") },
                onClick = {
                    showMenu = false
                    onReaction?.invoke(message, "👍")
                }
            )

            DropdownMenuItem(
                text = { Text("Reaccionar 😂") },
                onClick = {
                    showMenu = false
                    onReaction?.invoke(message, "😂")
                }
            )

            DropdownMenuItem(
                text = { Text("Reenviar") },
                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                onClick = {
                    showMenu = false
                    onForwardMessage?.invoke(message)
                }
            )

            if (isOut && message.type == MessageType.TEXT && !message.isDeletedForEveryone) {
                DropdownMenuItem(
                    text = { Text("Editar") },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    onClick = {
                        showMenu = false
                        onEditMessage?.invoke(message)
                    }
                )
            }

            DropdownMenuItem(
                text = { Text("Eliminar para mí") },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                onClick = {
                    showMenu = false
                    onDeleteForMe?.invoke(message)
                }
            )

            if (isOut && !message.isDeletedForEveryone) {
                DropdownMenuItem(
                    text = { Text("Eliminar para todos") },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                    onClick = {
                        showMenu = false
                        onDeleteForEveryone?.invoke(message)
                    }
                )
            }
        }
    }
}
