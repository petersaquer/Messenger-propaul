package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeviceContact
import com.example.data.model.SavedContact
import com.example.data.model.User
import com.example.ui.components.AvatarView
import com.example.ui.theme.TelegramBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchContactsScreen(
    currentUserId: String = "",
    searchQuery: String = "",
    searchResults: List<User> = emptyList(),
    isSearching: Boolean = false,
    savedContacts: List<SavedContact> = emptyList(),
    deviceContacts: List<DeviceContact> = emptyList(),
    onQueryChange: (String) -> Unit = {},
    onExecuteSearch: (String) -> Unit = {},
    onSaveContact: (User) -> Unit = {},
    onDeleteSavedContact: (String) -> Unit = {},
    onOpenChat: (User) -> Unit = {},
    onOpenChatWithContact: (title: String, username: String, phone: String, isFirebase: Boolean) -> Unit = { _, _, _, _ -> },
    onCreateGroup: ((title: String, description: String) -> Unit)? = null,
    onCreateGroupWithMembers: ((title: String, description: String, members: List<User>) -> Unit)? = null,
    onSelectUser: ((User) -> Unit)? = null,
    onSelectSavedContact: ((SavedContact) -> Unit)? = null,
    onBack: () -> Unit = {}
) {
    // Mode: Normal single contact selection vs Group multi-contact selection
    var isGroupMode by remember { mutableStateOf(false) }
    val selectedGroupMembers = remember { mutableStateListOf<User>() }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Mis Contactos (${savedContacts.size})", "Agenda (${deviceContacts.size})", "Buscar en la Red")

    var showAddContactDialog by remember { mutableStateOf(false) }
    var showGroupDetailsDialog by remember { mutableStateOf(false) }

    // Dialog state for new contact
    var newContactName by remember { mutableStateOf("") }
    var newContactPhoneOrUser by remember { mutableStateOf("") }
    var addContactError by remember { mutableStateOf<String?>(null) }

    // Dialog state for group naming
    var groupTitleInput by remember { mutableStateOf("") }
    var groupDescInput by remember { mutableStateOf("") }

    BackHandler {
        if (isGroupMode) {
            isGroupMode = false
            selectedGroupMembers.clear()
        } else {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        if (isGroupMode) {
                            Text("Nuevo Grupo", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(
                                if (selectedGroupMembers.isEmpty()) "Selecciona participantes"
                                else "${selectedGroupMembers.size} seleccionado${if (selectedGroupMembers.size > 1) "s" else ""}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text("Seleccionar Contacto", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            val total = savedContacts.size + deviceContacts.size
                            Text(
                                "$total contactos disponibles",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isGroupMode) {
                            isGroupMode = false
                            selectedGroupMembers.clear()
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            if (isGroupMode && selectedGroupMembers.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { showGroupDetailsDialog = true },
                    containerColor = TelegramBlue,
                    contentColor = Color.White
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Continuar")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Selected members chip row in group mode
            if (isGroupMode && selectedGroupMembers.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(selectedGroupMembers, key = { it.userId.ifBlank { it.username } }) { member ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.clickable { selectedGroupMembers.remove(member) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 8.dp, end = 6.dp, top = 4.dp, bottom = 4.dp)
                            ) {
                                AvatarView(name = member.name, size = 24.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(member.name, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Quitar",
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
                HorizontalDivider()
            }

            // Quick Actions (Only in normal mode)
            if (!isGroupMode) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        // Action 1: Create Group
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    isGroupMode = true
                                    selectedGroupMembers.clear()
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(TelegramBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Group, contentDescription = null, tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Nuevo grupo",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Elige contactos para crear un chat grupal",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Action 2: Add New Contact
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    addContactError = null
                                    showAddContactDialog = true
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00C853)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Añadir contacto",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Vincular por número de teléfono o @usuario",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }

            // Tabs for switching contact sources
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    // Saved Contacts List
                    if (savedContacts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "No hay contactos guardados todavía.",
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { showAddContactDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue)
                                ) {
                                    Text("Añadir un contacto para chatear")
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        ) {
                            items(savedContacts, key = { it.contactUserId }) { contact ->
                                val userObj = User(
                                    userId = contact.contactUserId.ifBlank { contact.username.ifBlank { contact.phoneNumber } },
                                    displayName = contact.name,
                                    username = contact.username,
                                    phoneNumber = contact.phoneNumber,
                                    avatarGradientStart = contact.avatarGradientStart,
                                    avatarGradientEnd = contact.avatarGradientEnd
                                )
                                val isSelected = selectedGroupMembers.any { it.userId == userObj.userId }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isGroupMode) {
                                                if (isSelected) {
                                                    selectedGroupMembers.removeAll { it.userId == userObj.userId }
                                                } else {
                                                    selectedGroupMembers.add(userObj)
                                                }
                                            } else {
                                                // Strictly open chat with this contact
                                                onSelectSavedContact?.invoke(contact)
                                                onOpenChatWithContact(
                                                    contact.name,
                                                    contact.username,
                                                    contact.phoneNumber,
                                                    contact.isFirebaseUser
                                                )
                                            }
                                        }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isGroupMode) {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = { checked ->
                                                if (checked) selectedGroupMembers.add(userObj)
                                                else selectedGroupMembers.removeAll { it.userId == userObj.userId }
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }

                                    AvatarView(
                                        name = contact.name,
                                        gradientStart = contact.avatarGradientStart,
                                        gradientEnd = contact.avatarGradientEnd,
                                        size = 46.dp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(contact.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text(
                                            contact.username.ifBlank { contact.phoneNumber },
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    if (!isGroupMode) {
                                        IconButton(onClick = { onDeleteSavedContact(contact.contactUserId) }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Eliminar contacto",
                                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                }

                1 -> {
                    // Device Contacts List
                    val context = androidx.compose.ui.platform.LocalContext.current
                    var hasPermission by remember {
                        mutableStateOf(
                            androidx.core.content.ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.READ_CONTACTS
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        )
                    }

                    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        hasPermission = isGranted
                    }

                    if (!hasPermission) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = TelegramBlue,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "Para sincronizar y ver los contactos de tu agenda, otorga el permiso de lectura de contactos.",
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 20.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { permissionLauncher.launch(android.Manifest.permission.READ_CONTACTS) },
                                    colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue)
                                ) {
                                    Text("Sincronizar contactos del dispositivo")
                                }
                            }
                        }
                    } else if (deviceContacts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No se encontraron contactos en la agenda local del dispositivo.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        ) {
                            items(deviceContacts, key = { it.name + it.phoneNumber }) { dev ->
                                val userObj = User(
                                    userId = dev.phoneNumber.filter { it.isDigit() }.ifBlank { dev.name },
                                    displayName = dev.name,
                                    phoneNumber = dev.phoneNumber
                                )
                                val isSelected = selectedGroupMembers.any { it.userId == userObj.userId }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isGroupMode) {
                                                if (isSelected) {
                                                    selectedGroupMembers.removeAll { it.userId == userObj.userId }
                                                } else {
                                                    selectedGroupMembers.add(userObj)
                                                }
                                            } else {
                                                onOpenChatWithContact(
                                                    dev.name,
                                                    "",
                                                    dev.phoneNumber,
                                                    dev.isRegisteredInApp
                                                )
                                            }
                                        }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isGroupMode) {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = { checked ->
                                                if (checked) selectedGroupMembers.add(userObj)
                                                else selectedGroupMembers.removeAll { it.userId == userObj.userId }
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }

                                    AvatarView(name = dev.name, size = 46.dp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(dev.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text(
                                            dev.phoneNumber,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                }

                2 -> {
                    // Global Network Search (Firestore)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                onQueryChange(it)
                                onExecuteSearch(it)
                            },
                            label = { Text("Buscar usuario por @nombre o nombre") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (isSearching) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_contacts_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (searchResults.isEmpty() && searchQuery.isNotBlank() && !isSearching) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    "No se encontraron usuarios para \"$searchQuery\"",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(searchResults, key = { it.userId }) { user ->
                                    val isSelected = selectedGroupMembers.any { it.userId == user.userId }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                if (isGroupMode) {
                                                    if (isSelected) {
                                                        selectedGroupMembers.removeAll { it.userId == user.userId }
                                                    } else {
                                                        selectedGroupMembers.add(user)
                                                    }
                                                } else {
                                                    onSelectUser?.invoke(user)
                                                    onOpenChat(user)
                                                }
                                            }
                                            .padding(vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isGroupMode) {
                                            Checkbox(
                                                checked = isSelected,
                                                onCheckedChange = { checked ->
                                                    if (checked) selectedGroupMembers.add(user)
                                                    else selectedGroupMembers.removeAll { it.userId == user.userId }
                                                }
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }

                                        AvatarView(
                                            name = user.name,
                                            gradientStart = user.avatarGradientStart,
                                            gradientEnd = user.avatarGradientEnd,
                                            size = 46.dp
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(user.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            Text(
                                                user.username,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        if (!isGroupMode && user.userId != currentUserId) {
                                            IconButton(onClick = { onSaveContact(user) }) {
                                                Icon(
                                                    imageVector = Icons.Default.PersonAdd,
                                                    contentDescription = "Guardar",
                                                    tint = TelegramBlue
                                                )
                                            }
                                        }
                                    }
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Validated Add Contact & Chat Dialog
    if (showAddContactDialog) {
        AlertDialog(
            onDismissRequest = { showAddContactDialog = false },
            title = { Text("Añadir Contacto y Chatear", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Para garantizar que el chat quede vinculado a una persona real, ingresa su nombre y número o @usuario:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = newContactName,
                        onValueChange = {
                            newContactName = it
                            addContactError = null
                        },
                        label = { Text("Nombre Completo *") },
                        placeholder = { Text("Ej: Andrea Morales") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newContactPhoneOrUser,
                        onValueChange = {
                            newContactPhoneOrUser = it
                            addContactError = null
                        },
                        label = { Text("Teléfono o @usuario *") },
                        placeholder = { Text("Ej: +34 600... o @andrea_m") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    addContactError?.let { err ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(err, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanName = newContactName.trim()
                        val cleanPhoneOrUser = newContactPhoneOrUser.trim()

                        if (cleanName.isBlank()) {
                            addContactError = "Por favor ingresa un nombre para el contacto."
                            return@Button
                        }
                        if (cleanPhoneOrUser.isBlank()) {
                            addContactError = "Por favor ingresa un teléfono o @usuario válido."
                            return@Button
                        }

                        val isUser = cleanPhoneOrUser.startsWith("@")
                        onOpenChatWithContact(
                            cleanName,
                            if (isUser) cleanPhoneOrUser else "",
                            if (!isUser) cleanPhoneOrUser else "",
                            true
                        )
                        showAddContactDialog = false
                        newContactName = ""
                        newContactPhoneOrUser = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue)
                ) {
                    Text("Guardar y Chatear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddContactDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal: Group Details (Naming) with Selected Contacts
    if (showGroupDetailsDialog) {
        AlertDialog(
            onDismissRequest = { showGroupDetailsDialog = false },
            title = { Text("Crear Nuevo Grupo", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Miembros seleccionados: ${selectedGroupMembers.size}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TelegramBlue
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = groupTitleInput,
                        onValueChange = { groupTitleInput = it },
                        label = { Text("Nombre del Grupo *") },
                        placeholder = { Text("Ej. Viaje a la Playa") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = groupDescInput,
                        onValueChange = { groupDescInput = it },
                        label = { Text("Descripción (opcional)") },
                        placeholder = { Text("Objetivo del grupo...") },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val title = groupTitleInput.trim()
                        if (title.isNotBlank() && selectedGroupMembers.isNotEmpty()) {
                            if (onCreateGroupWithMembers != null) {
                                onCreateGroupWithMembers(title, groupDescInput.trim(), selectedGroupMembers.toList())
                            } else if (onCreateGroup != null) {
                                onCreateGroup(title, groupDescInput.trim())
                            }
                            showGroupDetailsDialog = false
                            isGroupMode = false
                            selectedGroupMembers.clear()
                            groupTitleInput = ""
                            groupDescInput = ""
                        }
                    },
                    enabled = groupTitleInput.trim().isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue)
                ) {
                    Text("Crear Grupo")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGroupDetailsDialog = false }) {
                    Text("Atrás")
                }
            }
        )
    }
}
