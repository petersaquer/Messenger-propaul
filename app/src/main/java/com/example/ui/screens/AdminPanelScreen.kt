package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppUpdateInfo
import com.example.data.model.User
import com.example.data.model.UserRoles
import com.example.ui.components.AvatarView
import com.example.ui.theme.TelegramAccent
import com.example.ui.theme.TelegramBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class UserStatusFilter {
    ALL,
    ACTIVE,
    BLOCKED,
    SUSPENDED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    currentUser: User,
    allUsers: List<User>,
    latestAppUpdate: AppUpdateInfo?,
    onUpdateRole: (userId: String, newRole: String) -> Unit,
    onToggleBan: ((userId: String, isBanned: Boolean) -> Unit)? = null,
    onSuspendUser: ((userId: String, durationDays: Int, reason: String) -> Unit)? = null,
    onDeleteUser: ((userId: String) -> Unit)? = null,
    onPublishUpdate: ((versionName: String, versionCode: Int, apkUrl: String, changelog: String, isMandatory: Boolean) -> Unit)? = null,
    onBack: () -> Unit = {}
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf(UserStatusFilter.ALL) }

    // Dialog states
    var userForSuspendDialog by remember { mutableStateOf<User?>(null) }
    var userForRoleDialog by remember { mutableStateOf<User?>(null) }
    var userForDeleteDialog by remember { mutableStateOf<User?>(null) }
    var userForBlockConfirmDialog by remember { mutableStateOf<User?>(null) }
    var showUpdateDialog by remember { mutableStateOf(false) }

    // Filter list of users
    val filteredUsers = remember(allUsers, searchQuery, statusFilter) {
        allUsers.filter { user ->
            val matchesSearch = searchQuery.isBlank() ||
                    user.displayName.contains(searchQuery, ignoreCase = true) ||
                    user.username.contains(searchQuery, ignoreCase = true) ||
                    user.email.contains(searchQuery, ignoreCase = true) ||
                    user.phoneNumber.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (statusFilter) {
                UserStatusFilter.ALL -> true
                UserStatusFilter.ACTIVE -> !user.isCurrentlyBanned
                UserStatusFilter.BLOCKED -> user.isPermanentlyBanned
                UserStatusFilter.SUSPENDED -> user.isTempBanned
            }

            matchesSearch && matchesFilter
        }
    }

    // Statistics counts
    val totalCount = allUsers.size
    val activeCount = remember(allUsers) { allUsers.count { !it.isCurrentlyBanned } }
    val blockedCount = remember(allUsers) { allUsers.count { it.isPermanentlyBanned } }
    val suspendedCount = remember(allUsers) { allUsers.count { it.isTempBanned } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Panel de Administración",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "$totalCount usuarios • $activeCount activos • $blockedCount bloqueados",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("admin_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    if (onPublishUpdate != null) {
                        IconButton(
                            onClick = { showUpdateDialog = true },
                            modifier = Modifier.testTag("admin_publish_update_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = "Publicar Actualización",
                                tint = TelegramBlue
                            )
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar usuario por nombre, @usuario o email...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = TelegramBlue
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("admin_user_search_input")
            )

            // Status Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = statusFilter == UserStatusFilter.ALL,
                        onClick = { statusFilter = UserStatusFilter.ALL },
                        label = { Text("Todos ($totalCount)", fontSize = 12.sp) },
                        leadingIcon = {
                            if (statusFilter == UserStatusFilter.ALL) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        }
                    )
                }
                item {
                    FilterChip(
                        selected = statusFilter == UserStatusFilter.ACTIVE,
                        onClick = { statusFilter = UserStatusFilter.ACTIVE },
                        label = { Text("Activos ($activeCount)", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF2E7D32).copy(alpha = 0.2f),
                            selectedLabelColor = Color(0xFF2E7D32)
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = statusFilter == UserStatusFilter.BLOCKED,
                        onClick = { statusFilter = UserStatusFilter.BLOCKED },
                        label = { Text("Bloqueados ($blockedCount)", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.error
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = statusFilter == UserStatusFilter.SUSPENDED,
                        onClick = { statusFilter = UserStatusFilter.SUSPENDED },
                        label = { Text("Suspendidos ($suspendedCount)", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFE65100).copy(alpha = 0.2f),
                            selectedLabelColor = Color(0xFFE65100)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // User List
            if (filteredUsers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No se encontraron usuarios",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Intenta con otro término de búsqueda o cambia el filtro",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize().testTag("admin_users_list")
                ) {
                    items(filteredUsers, key = { it.userId }) { user ->
                        val isSelf = user.userId == currentUser.userId
                        AdminUserItem(
                            user = user,
                            isSelf = isSelf,
                            onQuickBlock = {
                                userForBlockConfirmDialog = user
                            },
                            onQuickUnblock = {
                                onToggleBan?.invoke(user.userId, false)
                                Toast.makeText(context, "Usuario ${user.name} desbloqueado exitosamente", Toast.LENGTH_SHORT).show()
                            },
                            onQuickSuspend = {
                                userForSuspendDialog = user
                            },
                            onQuickRole = {
                                userForRoleDialog = user
                            },
                            onQuickDelete = {
                                userForDeleteDialog = user
                            }
                        )
                    }
                }
            }
        }
    }

    // DIÁLOGO: Confirmar Bloqueo Rápido
    userForBlockConfirmDialog?.let { targetUser ->
        AlertDialog(
            onDismissRequest = { userForBlockConfirmDialog = null },
            icon = { Icon(Icons.Default.Block, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("¿Bloquear permanentemente?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "El usuario '${targetUser.name}' (@${targetUser.username}) no podrá enviar mensajes ni iniciar sesión hasta que sea desbloqueado.",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onToggleBan?.invoke(targetUser.userId, true)
                        Toast.makeText(context, "Usuario ${targetUser.name} bloqueado", Toast.LENGTH_SHORT).show()
                        userForBlockConfirmDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Bloquear Usuario")
                }
            },
            dismissButton = {
                TextButton(onClick = { userForBlockConfirmDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // DIÁLOGO: Suspender por Tiempo
    userForSuspendDialog?.let { targetUser ->
        QuickSuspendDialog(
            user = targetUser,
            onApply = { durationDays, reason ->
                if (onSuspendUser != null) {
                    onSuspendUser(targetUser.userId, durationDays, reason)
                } else {
                    onToggleBan?.invoke(targetUser.userId, true)
                }
                val label = if (durationDays == 0) "Baneo permanente" else "Suspensión por $durationDays días"
                Toast.makeText(context, "$label aplicada a ${targetUser.name}", Toast.LENGTH_SHORT).show()
                userForSuspendDialog = null
            },
            onDismiss = { userForSuspendDialog = null }
        )
    }

    // DIÁLOGO: Cambiar Rol
    userForRoleDialog?.let { targetUser ->
        QuickRoleDialog(
            user = targetUser,
            onSelect = { newRole ->
                onUpdateRole(targetUser.userId, newRole)
                Toast.makeText(context, "Rol de ${targetUser.name} actualizado a $newRole", Toast.LENGTH_SHORT).show()
                userForRoleDialog = null
            },
            onDismiss = { userForRoleDialog = null }
        )
    }

    // DIÁLOGO: Eliminar Usuario
    userForDeleteDialog?.let { targetUser ->
        AlertDialog(
            onDismissRequest = { userForDeleteDialog = null },
            icon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Eliminar cuenta de usuario", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
            text = {
                Text(
                    "¿Deseas eliminar definitivamente la cuenta de '${targetUser.name}' (@${targetUser.username}) de la base de datos?\n\nEsta acción es irreversible.",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteUser?.invoke(targetUser.userId)
                        Toast.makeText(context, "Cuenta de ${targetUser.name} eliminada", Toast.LENGTH_SHORT).show()
                        userForDeleteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sí, Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { userForDeleteDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // DIÁLOGO: Publicar Actualización
    if (showUpdateDialog && onPublishUpdate != null) {
        PublishUpdateDialog(
            latestAppUpdate = latestAppUpdate,
            onPublish = { vName, vCode, url, notes, mandatory ->
                onPublishUpdate(vName, vCode, url, notes, mandatory)
                showUpdateDialog = false
            },
            onDismiss = { showUpdateDialog = false }
        )
    }
}

/**
 * Tarjeta individual de usuario con botones rápidos de gestión de estados.
 */
@Composable
private fun AdminUserItem(
    user: User,
    isSelf: Boolean,
    onQuickBlock: () -> Unit,
    onQuickUnblock: () -> Unit,
    onQuickSuspend: () -> Unit,
    onQuickRole: () -> Unit,
    onQuickDelete: () -> Unit
) {
    val isBanned = user.isCurrentlyBanned
    val isTemp = user.isTempBanned

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isBanned && !isTemp -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                isTemp -> Color(0xFFFFF3E0)
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_user_card_${user.userId}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // User Basic Info Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                AvatarView(name = user.name, size = 46.dp)

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (user.isVip) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "VIP",
                                tint = TelegramBlue,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        if (isSelf) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = TelegramBlue.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Tú",
                                    color = TelegramBlue,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = if (user.username.startsWith("@")) user.username else "@${user.username}",
                        fontSize = 12.sp,
                        color = TelegramBlue
                    )

                    if (user.email.isNotBlank()) {
                        Text(
                            text = user.email,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Role Badge
                QuickRoleBadge(role = user.role)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // State Badge Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = when {
                            isBanned && !isTemp -> MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                            isTemp -> Color(0xFFE65100).copy(alpha = 0.12f)
                            else -> Color(0xFF2E7D32).copy(alpha = 0.12f)
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = when {
                        isBanned && !isTemp -> Icons.Default.Block
                        isTemp -> Icons.Default.Timer
                        else -> Icons.Default.CheckCircle
                    },
                    contentDescription = null,
                    tint = when {
                        isBanned && !isTemp -> MaterialTheme.colorScheme.error
                        isTemp -> Color(0xFFE65100)
                        else -> Color(0xFF2E7D32)
                    },
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                val statusLabel = when {
                    isBanned && !isTemp -> "BLOQUEADO PERMANENTEMENTE"
                    isTemp -> {
                        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        "SUSPENDIDO HASTA: ${sdf.format(Date(user.banExpiresAt))}"
                    }
                    else -> "ACTIVO • Sin sanciones"
                }

                Text(
                    text = statusLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isBanned && !isTemp -> MaterialTheme.colorScheme.error
                        isTemp -> Color(0xFFE65100)
                        else -> Color(0xFF2E7D32)
                    }
                )
            }

            // Quick Action Buttons
            if (!isSelf) {
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quick Unblock / Block button
                    if (isBanned) {
                        Button(
                            onClick = onQuickUnblock,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("admin_unblock_btn_${user.userId}")
                        ) {
                            Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Desbloquear", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onQuickBlock,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("admin_block_btn_${user.userId}")
                        ) {
                            Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Bloquear", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onQuickSuspend,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE65100)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("admin_suspend_btn_${user.userId}")
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Suspender", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Quick Role button
                    OutlinedButton(
                        onClick = onQuickRole,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("admin_role_btn_${user.userId}")
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rol", fontSize = 11.sp)
                    }

                    // Quick Delete button
                    IconButton(
                        onClick = onQuickDelete,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("admin_delete_btn_${user.userId}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickRoleBadge(role: String) {
    val (bgColor, textColor, label) = when (role.uppercase()) {
        UserRoles.ADMIN -> Triple(Color(0xFFD32F2F), Color.White, "ADMIN")
        UserRoles.MODERATOR -> Triple(Color(0xFFF57C00), Color.White, "MOD")
        UserRoles.VIP -> Triple(TelegramBlue, Color.White, "VIP")
        UserRoles.RESTRICTED -> Triple(Color.Gray, Color.White, "RESTRINGIDO")
        else -> Triple(Color(0xFF388E3C), Color.White, "USUARIO")
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

/**
 * Diálogo modal para suspender usuario por tiempo determinado (24h, 3d, 7d, 30d, 90d, permanente).
 */
@Composable
private fun QuickSuspendDialog(
    user: User,
    onApply: (durationDays: Int, reason: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedDays by remember { mutableIntStateOf(1) } // 1 día por defecto
    var reason by remember { mutableStateOf("Suspensión por incumplimiento de normas") }
    var isCustom by remember { mutableStateOf(false) }
    var customDaysText by remember { mutableStateOf("15") }

    val presets = listOf(
        1 to "24 Horas",
        3 to "3 Días",
        7 to "7 Días (1 Sem.)",
        30 to "30 Días (1 Mes)",
        90 to "90 Días (3 Meses)",
        0 to "Permanente"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Timer, contentDescription = null, tint = Color(0xFFE65100)) },
        title = {
            Text("Suspender a ${user.name}", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = "Selecciona la duración de la suspensión temporal:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Presets Buttons
                presets.chunked(3).forEach { rowPresets ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rowPresets.forEach { (days, label) ->
                            val isSelected = !isCustom && selectedDays == days
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    isCustom = false
                                    selectedDays = days
                                },
                                label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Custom option
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isCustom = true }
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = isCustom,
                        onClick = { isCustom = true }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Duración personalizada en días", fontSize = 13.sp)
                }

                if (isCustom) {
                    OutlinedTextField(
                        value = customDaysText,
                        onValueChange = { customDaysText = it },
                        label = { Text("Número de días") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Motivo de la sanción") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalDays = if (isCustom) (customDaysText.toIntOrNull() ?: 1) else selectedDays
                    onApply(finalDays, reason.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
            ) {
                Text("Aplicar Suspensión")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Diálogo rápido para cambiar el rol de un usuario.
 */
@Composable
private fun QuickRoleDialog(
    user: User,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val roles = listOf(
        UserRoles.ADMIN to "Administrador (Control total)",
        UserRoles.MODERATOR to "Moderador (Bloqueos y reportes)",
        UserRoles.VIP to "Usuario VIP / Verificado",
        UserRoles.USER to "Usuario Estándar",
        UserRoles.RESTRICTED to "Restringido (Sin grupos)"
    )

    var selectedRole by remember { mutableStateOf(user.role) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Shield, contentDescription = null, tint = TelegramBlue) },
        title = { Text("Cambiar Rol a ${user.name}", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                roles.forEach { (roleKey, desc) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedRole = roleKey }
                            .padding(vertical = 6.dp)
                    ) {
                        RadioButton(
                            selected = selectedRole.equals(roleKey, ignoreCase = true),
                            onClick = { selectedRole = roleKey }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(roleKey, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSelect(selectedRole) },
                colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue)
            ) {
                Text("Guardar Rol")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Diálogo para publicar actualización de la aplicación.
 */
@Composable
private fun PublishUpdateDialog(
    latestAppUpdate: AppUpdateInfo?,
    onPublish: (versionName: String, versionCode: Int, apkUrl: String, changelog: String, isMandatory: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var versionName by remember { mutableStateOf(latestAppUpdate?.versionName ?: "1.1.0") }
    var versionCode by remember { mutableStateOf((latestAppUpdate?.versionCode ?: 1) + 1) }
    var apkUrl by remember { mutableStateOf(latestAppUpdate?.apkDownloadUrl ?: "") }
    var changelog by remember { mutableStateOf(latestAppUpdate?.changelog ?: "• Mejoras de rendimiento y estabilidad\n• Corrección de errores") }
    var isMandatory by remember { mutableStateOf(latestAppUpdate?.isMandatory ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Publish, contentDescription = null, tint = TelegramBlue) },
        title = { Text("Publicar Actualización", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = versionName,
                    onValueChange = { versionName = it },
                    label = { Text("Nombre de Versión (ej: 1.2.0)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = versionCode.toString(),
                    onValueChange = { versionCode = it.toIntOrNull() ?: 1 },
                    label = { Text("Código de Versión (ej: 2)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = apkUrl,
                    onValueChange = { apkUrl = it },
                    label = { Text("URL de descarga APK") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = changelog,
                    onValueChange = { changelog = it },
                    label = { Text("Notas de la versión") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isMandatory = !isMandatory }
                ) {
                    Checkbox(checked = isMandatory, onCheckedChange = { isMandatory = it })
                    Text("Actualización obligatoria", fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (versionName.isBlank()) {
                        Toast.makeText(context, "Ingresa el nombre de versión", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    onPublish(versionName.trim(), versionCode, apkUrl.trim(), changelog.trim(), isMandatory)
                    Toast.makeText(context, "Actualización publicada", Toast.LENGTH_SHORT).show()
                },
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
}
