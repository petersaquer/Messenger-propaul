package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SavedContact
import com.example.data.model.StatusItem
import com.example.data.model.StatusPrivacy
import com.example.data.model.StatusType
import com.example.data.model.User
import com.example.ui.theme.TelegramAccent
import com.example.ui.theme.TelegramBlue

@Composable
fun CreateStatusDialog(
    initialType: StatusType = StatusType.TEXT,
    availableContacts: List<SavedContact> = emptyList(),
    currentUser: User? = null,
    onDismiss: () -> Unit,
    onPublish: ((
        type: StatusType,
        text: String,
        mediaUrl: String,
        bgGradient: Long,
        fontStyle: String,
        caption: String,
        privacy: StatusPrivacy,
        excludedIds: List<String>,
        includedIds: List<String>
    ) -> Unit)? = null,
    onPublishStatus: ((StatusItem) -> Unit)? = null
) {
    var selectedTab by remember { mutableIntStateOf(if (initialType == StatusType.TEXT) 0 else 1) }
    var text by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf("") }
    var caption by remember { mutableStateOf("") }

    val colors = listOf(0xFF0D5C46, 0xFF1976D2, 0xFF7B1FA2, 0xFFC2185B, 0xFFE64A19, 0xFF263238)
    var selectedColor by remember { mutableLongStateOf(colors[0]) }

    var selectedPrivacy by remember { mutableStateOf(StatusPrivacy.MY_CONTACTS) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var excludedUserIds by remember { mutableStateOf(listOf<String>()) }
    var includedUserIds by remember { mutableStateOf(listOf<String>()) }

    val canPublish = if (selectedTab == 0) text.isNotBlank() else photoUrl.isNotBlank() || caption.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Estado (24h)", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TextFields, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Texto")
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Foto / Imagen")
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedTab == 0) {
                    // Text Status Form
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        label = { Text("¿Qué estás pensando?") },
                        placeholder = { Text("Escribe una actualización para tus contactos...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Color de fondo:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(colors) { colorLong ->
                            val isSelected = selectedColor == colorLong
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorLong.toInt()))
                                    .clickable { selectedColor = colorLong }
                                    .then(
                                        if (isSelected) Modifier.border(2.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                        else Modifier
                                    )
                            )
                        }
                    }
                } else {
                    // Photo Status Form
                    OutlinedTextField(
                        value = photoUrl,
                        onValueChange = { photoUrl = it },
                        label = { Text("URL de la Foto / Imagen") },
                        placeholder = { Text("https://ejemplo.com/foto.jpg") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = caption,
                        onValueChange = { caption = it },
                        label = { Text("Pie de foto (opcional)") },
                        placeholder = { Text("Añade un comentario...") },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Privacy selector button
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPrivacyDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = TelegramBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Privacidad: ${selectedPrivacy.label}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Visible durante 24 horas y luego se elimina automáticamente.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (canPublish) {
                        val isText = selectedTab == 0
                        val statusType = if (isText) StatusType.TEXT else StatusType.PHOTO
                        val publishedText = if (isText) text else caption

                        if (onPublish != null) {
                            onPublish(
                                statusType,
                                publishedText,
                                if (!isText) photoUrl else "",
                                selectedColor,
                                "NORMAL",
                                caption,
                                selectedPrivacy,
                                excludedUserIds,
                                includedUserIds
                            )
                        } else if (onPublishStatus != null) {
                            val item = StatusItem(
                                id = "status_${System.currentTimeMillis()}",
                                userId = currentUser?.userId ?: "me",
                                displayName = currentUser?.name ?: "Yo",
                                contentText = publishedText,
                                mediaUrl = if (!isText) photoUrl else "",
                                backgroundColor = selectedColor,
                                type = statusType.name,
                                privacy = selectedPrivacy.name
                            )
                            onPublishStatus(item)
                        }
                    }
                },
                enabled = canPublish,
                colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue)
            ) {
                Text("Publicar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )

    if (showPrivacyDialog) {
        StatusPrivacyDialog(
            currentPrivacy = selectedPrivacy,
            initialExcludedIds = excludedUserIds,
            initialIncludedIds = includedUserIds,
            availableContacts = availableContacts,
            onDismiss = { showPrivacyDialog = false },
            onSavePrivacy = { newPrivacy, newExcluded, newIncluded ->
                selectedPrivacy = newPrivacy
                excludedUserIds = newExcluded
                includedUserIds = newIncluded
                showPrivacyDialog = false
            }
        )
    }
}
