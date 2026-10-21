package com.example.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class SavedContact(
    val contactUserId: String = "",
    val displayName: String = "",
    val username: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val bio: String = "",
    val avatarGradientStart: Long = 0xFF2AABEE,
    val avatarGradientEnd: Long = 0xFF1976D2,
    val role: String = "USER",
    val isOnline: Boolean = false,
    val addedAt: Long = System.currentTimeMillis(),
    val isFirebaseUser: Boolean = true
) {
    val name: String get() = displayName.ifBlank { username }
    val phone: String get() = phoneNumber
    val contactId: String get() = contactUserId
}
