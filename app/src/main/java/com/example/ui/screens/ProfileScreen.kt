package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.User
import com.example.data.model.UserRoles
import com.example.ui.components.AvatarView
import com.example.ui.theme.TelegramAccent
import com.example.ui.theme.TelegramBlue
import com.example.ui.theme.TelegramDarkBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    currentUser: User?,
    privacyPhone: String = "Mis contactos",
    privacyLastSeen: String = "Todos",
    privacyProfilePhoto: String = "Todos",
    privacyForwardProtect: Boolean = true,
    onUpdateProfile: (displayName: String, bio: String, photoUrl: String?, username: String?, phone: String?) -> Unit,
    onUpdatePrivacyPhone: (String) -> Unit = {},
    onUpdatePrivacyLastSeen: (String) -> Unit = {},
    onUpdatePrivacyProfilePhoto: (String) -> Unit = {},
    onToggleForwardProtect: (Boolean) -> Unit = {},
    onSignOutGoogle: () -> Unit = {},
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val user = currentUser
    var displayNameInput by remember(user) { mutableStateOf(user?.displayName ?: user?.name ?: "") }
    var usernameInput by remember(user) { mutableStateOf(user?.username ?: "") }
    var bioInput by remember(user) { mutableStateOf(user?.bio ?: "¡Hola! Estoy usando Paul Messenger 🚀") }
    var phoneInput by remember(user) { mutableStateOf(user?.phoneNumber ?: "") }
    var profilePhotoUrlInput by remember(user) { mutableStateOf(user?.profilePictureUrl ?: "") }

    var showPhotoUrlDialog by remember { mutableStateOf(false) }
    var showQrDialog by remember { mutableStateOf(false) }
    var tempPhotoUrl by remember { mutableStateOf(profilePhotoUrlInput) }

    val profilePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val uriStr = it.toString()
            profilePhotoUrlInput = uriStr
            tempPhotoUrl = uriStr
            Toast.makeText(context, "Foto seleccionada de la galería de Android", Toast.LENGTH_SHORT).show()
        }
    }

    val hasChanges = displayNameInput != (user?.displayName ?: "") ||
            usernameInput != (user?.username ?: "") ||
            bioInput != (user?.bio ?: "") ||
            phoneInput != (user?.phoneNumber ?: "") ||
            profilePhotoUrlInput != (user?.profilePictureUrl ?: "")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil Completo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("profile_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val cleanUser = usernameInput.ifBlank { "@paul_user" }
                            clipboardManager.setText(AnnotatedString("https://paul.app/$cleanUser"))
                            Toast.makeText(context, "Enlace de perfil copiado al portapapeles", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir perfil", tint = TelegramBlue)
                    }

                    if (hasChanges) {
                        IconButton(
                            onClick = {
                                onUpdateProfile(displayNameInput, bioInput, profilePhotoUrlInput.ifBlank { null }, usernameInput, phoneInput)
                                Toast.makeText(context, "¡Perfil guardado con éxito!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.Save, contentDescription = "Guardar", tint = TelegramAccent)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Hero Box with Large Avatar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(TelegramBlue, TelegramDarkBlue)
                        )
                    )
                    .padding(vertical = 28.dp, horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Profile Photo with Camera Edit Badge
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .border(3.dp, Color.White, CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!profilePhotoUrlInput.isNullOrBlank()) {
                                AsyncImage(
                                    model = profilePhotoUrlInput,
                                    contentDescription = "Foto de perfil",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = displayNameInput.firstOrNull()?.uppercaseChar()?.toString() ?: "P",
                                    color = Color.White,
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Edit camera badge
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(TelegramAccent)
                                .border(2.dp, Color.White, CircleShape)
                                .clickable {
                                    tempPhotoUrl = profilePhotoUrlInput
                                    showPhotoUrlDialog = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Cambiar foto de perfil",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = displayNameInput.ifBlank { "Usuario sin nombre" },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (usernameInput.startsWith("@")) usernameInput else "@${usernameInput.ifBlank { "paul_user" }}",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Role Badge & Online Status
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (user?.isAdmin == true) Color(0xFFD32F2F) else if (user?.isModerator == true) Color(0xFFF57C00) else Color(0xFF0288D1)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (user?.isAdmin == true) Icons.Default.Shield else Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = user?.role ?: UserRoles.USER,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF00C853).copy(alpha = 0.3f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00C853))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "En línea",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card 1: Información Personal Editables
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "INFORMACIÓN PERSONAL",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TelegramBlue,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = displayNameInput,
                            onValueChange = { displayNameInput = it },
                            label = { Text("Nombre Completo") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TelegramBlue) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = usernameInput,
                            onValueChange = { usernameInput = it },
                            label = { Text("Nombre de Usuario (@username)") },
                            leadingIcon = { Icon(Icons.Default.AlternateEmail, contentDescription = null, tint = TelegramBlue) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = bioInput,
                            onValueChange = { bioInput = it },
                            label = { Text("Biografía / Info") },
                            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = TelegramBlue) },
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            label = { Text("Teléfono de Contacto") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = TelegramBlue) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = TelegramBlue)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Correo Vinculado", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = user?.email?.ifBlank { "Sin correo registrado" } ?: "Sin correo registrado",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Card 2: Privacidad del Perfil
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "PRIVACIDAD DEL PERFIL",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TelegramBlue,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        PrivacySelectorRow(
                            label = "¿Quién puede ver mi foto de perfil?",
                            selectedOption = privacyProfilePhoto,
                            onOptionSelected = onUpdatePrivacyProfilePhoto
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        PrivacySelectorRow(
                            label = "¿Quién puede ver mi estado en línea / última vez?",
                            selectedOption = privacyLastSeen,
                            onOptionSelected = onUpdatePrivacyLastSeen
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        PrivacySelectorRow(
                            label = "¿Quién puede ver mi número de teléfono?",
                            selectedOption = privacyPhone,
                            onOptionSelected = onUpdatePrivacyPhone
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Proteger reenvío de mensajes", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("Oculta el enlace a tu perfil al reenviar tus mensajes", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = privacyForwardProtect, onCheckedChange = onToggleForwardProtect)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Card 3: Mi Código QR de Perfil
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = TelegramBlue.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TelegramBlue.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showQrDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "Código QR",
                            tint = TelegramBlue,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Mi Código QR de Perfil", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Toca para abrir y permitir que otros te escaneen", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Card 4: Botón Guardar Cambios / Cerrar Sesión
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Button(
                    onClick = {
                        onUpdateProfile(displayNameInput, bioInput, profilePhotoUrlInput.ifBlank { null }, usernameInput, phoneInput)
                        Toast.makeText(context, "¡Perfil actualizado correctamente!", Toast.LENGTH_SHORT).show()
                    },
                    enabled = hasChanges,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TelegramBlue,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar Cambios del Perfil", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onSignOutGoogle,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar Sesión")
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Modal: Cambiar URL de Foto de Perfil
    if (showPhotoUrlDialog) {
        val presetAvatars = listOf(
            "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400",
            "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400",
            "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400",
            "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400",
            "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=400"
        )

        AlertDialog(
            onDismissRequest = { showPhotoUrlDialog = false },
            title = { Text("Foto de Perfil", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Button(
                        onClick = {
                            profilePhotoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("📱 Elegir de Galería de Android", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("O elige un avatar de muestra / URL:", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        presetAvatars.forEach { url ->
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = if (tempPhotoUrl == url) 2.dp else 0.dp,
                                        color = if (tempPhotoUrl == url) TelegramBlue else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { tempPhotoUrl = url }
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = "Avatar de muestra",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = tempPhotoUrl,
                        onValueChange = { tempPhotoUrl = it },
                        label = { Text("URL de la imagen") },
                        placeholder = { Text("https://ejemplo.com/mifoto.jpg") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        profilePhotoUrlInput = tempPhotoUrl.trim()
                        showPhotoUrlDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue)
                ) {
                    Text("Aplicar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPhotoUrlDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal: Código QR del Perfil
    if (showQrDialog) {
        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            title = { Text("Tarjeta de Perfil QR", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 4.dp,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            AvatarView(name = displayNameInput, size = 64.dp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(displayNameInput, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text(usernameInput.ifBlank { "@paul_user" }, fontSize = 12.sp, color = TelegramBlue)
                            Spacer(modifier = Modifier.height(16.dp))
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(140.dp)
                            )
                        }
                    }
                    Text("Escanea para chatear en Paul Messenger", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                Button(onClick = { showQrDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Composable
private fun PrivacySelectorRow(
    label: String,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    val options = listOf("Todos", "Mis contactos", "Nadie")

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            options.forEach { option ->
                val isSelected = selectedOption.equals(option, ignoreCase = true)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onOptionSelected(option) }
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onOptionSelected(option) }
                    )
                    Text(option, fontSize = 12.sp)
                }
            }
        }
    }
}
