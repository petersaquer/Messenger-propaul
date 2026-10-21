package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.data.model.UserStatusGroup
import com.example.ui.theme.TelegramAccent
import com.example.ui.theme.TelegramBlue
import com.example.ui.theme.TelegramDarkBlue

@Composable
fun StoriesBar(
    currentUser: User?,
    statusGroups: List<UserStatusGroup>,
    onOpenStatusGroup: (UserStatusGroup) -> Unit,
    onCreateStatus: () -> Unit
) {
    val myStatusGroup = statusGroups.firstOrNull { it.isCurrentUser }
    val hasMyStatus = myStatusGroup != null && myStatusGroup.statuses.isNotEmpty()
    val otherStatusGroups = statusGroups.filterNot { it.isCurrentUser }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. My Status Item
        item(key = "my_status") {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .clickable {
                        if (hasMyStatus && myStatusGroup != null) {
                            onOpenStatusGroup(myStatusGroup)
                        } else {
                            onCreateStatus()
                        }
                    }
                    .testTag("create_status_story_button")
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .then(
                                if (hasMyStatus) {
                                    Modifier.border(2.5.dp, TelegramAccent, CircleShape)
                                } else {
                                    Modifier
                                }
                            )
                            .padding(if (hasMyStatus) 3.dp else 0.dp)
                            .clip(CircleShape)
                            .background(Brush.verticalGradient(listOf(TelegramBlue, TelegramDarkBlue))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentUser?.name?.firstOrNull()?.uppercaseChar()?.toString() ?: "P",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }

                    // Add badge button
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(if (hasMyStatus) TelegramBlue else TelegramAccent)
                            .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                            .clickable { onCreateStatus() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Añadir estado",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (hasMyStatus) "Mi estado" else "Añadir estado",
                    fontSize = 11.sp,
                    fontWeight = if (hasMyStatus) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1
                )
            }
        }

        // 2. Other Users' Stories
        items(otherStatusGroups, key = { it.userId }) { group ->
            val ringColor = if (group.hasUnseen) TelegramAccent else Color.LightGray

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .clickable { onOpenStatusGroup(group) }
                    .testTag("status_group_${group.userId}")
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .border(2.5.dp, ringColor, CircleShape)
                        .padding(3.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(group.userAvatarStart), Color(group.userAvatarEnd))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = group.displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = group.displayName,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = if (group.hasUnseen) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.width(62.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
