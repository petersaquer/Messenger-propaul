package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.StatusItem
import com.example.data.model.StatusType
import com.example.data.model.UserStatusGroup
import com.example.ui.components.AvatarView
import com.example.ui.theme.TelegramBlue

@Composable
fun StatusViewerScreen(
    group: UserStatusGroup,
    currentIndex: Int = 0,
    onClose: () -> Unit,
    onNext: () -> Unit = {},
    onPrevious: () -> Unit = {},
    onRecordView: ((String) -> Unit)? = null,
    onReact: ((String, String) -> Unit)? = null,
    onDeleteStatus: ((String) -> Unit)? = null,
    onReply: ((StatusItem, String) -> Unit)? = null
) {
    BackHandler { onClose() }

    val totalItems = group.statuses.size
    val safeIndex = currentIndex.coerceIn(0, (totalItems - 1).coerceAtLeast(0))
    val currentStatus = group.statuses.getOrNull(safeIndex)

    var replyText by remember { mutableStateOf("") }
    val progress = remember(safeIndex) { Animatable(0f) }

    LaunchedEffect(currentStatus?.id) {
        if (currentStatus != null) {
            onRecordView?.invoke(currentStatus.id)
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 5000, easing = LinearEasing)
            )
            onNext()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(safeIndex) {
                detectTapGestures(
                    onTap = { offset ->
                        if (offset.x < size.width * 0.35f) {
                            onPrevious()
                        } else {
                            onNext()
                        }
                    }
                )
            }
    ) {
        // Main Content (Text / Photo / Video)
        if (currentStatus != null) {
            if (currentStatus.type == StatusType.TEXT.name) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(currentStatus.backgroundColor.toInt()))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentStatus.contentText,
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 36.sp
                    )
                }
            } else {
                // Photo or Video
                AsyncImage(
                    model = currentStatus.mediaUrl,
                    contentDescription = currentStatus.caption,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                if (currentStatus.caption.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 90.dp, start = 20.dp, end = 20.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = currentStatus.caption,
                            color = Color.White,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Top Status Bars and User Info
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(Color.Black.copy(alpha = 0.4f))
                .padding(top = 40.dp, start = 16.dp, end = 16.dp, bottom = 12.dp)
        ) {
            // Segmented Progress Bars
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (i in 0 until totalItems) {
                    val barProgress = when {
                        i < safeIndex -> 1f
                        i == safeIndex -> progress.value
                        else -> 0f
                    }
                    LinearProgressIndicator(
                        progress = { barProgress },
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // User Info Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AvatarView(
                    name = group.displayName,
                    gradientStart = group.userAvatarStart,
                    gradientEnd = group.userAvatarEnd,
                    size = 38.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = group.displayName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = group.username,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }

                // Delete status button if current user owns it
                if (group.isCurrentUser && currentStatus != null) {
                    IconButton(onClick = { onDeleteStatus?.invoke(currentStatus.id) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar estado",
                            tint = Color.White
                        )
                    }
                }

                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color.White
                    )
                }
            }
        }

        // Bottom Bar
        if (group.isCurrentUser && currentStatus != null) {
            // My status: show view count
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = "Vistas",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                val count = currentStatus.viewers.size
                Text(
                    text = if (count == 0) "Sin visualizaciones aún" else "$count vista${if (count > 1) "s" else ""}",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        } else if (!group.isCurrentUser && currentStatus != null) {
            // Other user's status: quick reactions + reply bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Quick emoji reactions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("❤️", "👍", "😂", "🔥", "👏", "🎉").forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 24.sp,
                            modifier = Modifier
                                .clickable {
                                    onReact?.invoke(currentStatus.id, emoji)
                                }
                                .padding(4.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        placeholder = { Text("Responder a ${group.displayName}...", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (replyText.isNotBlank()) {
                                onReply?.invoke(currentStatus, replyText)
                                replyText = ""
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Enviar respuesta",
                            tint = TelegramBlue
                        )
                    }
                }
            }
        }
    }
}
