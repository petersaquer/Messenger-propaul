package com.example.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.data.model.DeviceContact

class RealContactsHelper(private val context: Context) {

    fun hasContactsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("Range")
    fun readDeviceContacts(): List<DeviceContact> {
        val contactList = mutableListOf<DeviceContact>()
        val seenPhones = mutableSetOf<String>()

        if (!hasContactsPermission()) {
            Log.d("RealContactsHelper", "READ_CONTACTS permission not granted yet, skipping provider query")
            return emptyList()
        }

        try {
            val cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                null,
                null,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
            )

            cursor?.use {
                while (it.moveToNext()) {
                    val name = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)) ?: "Sin nombre"
                    val number = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)) ?: ""
                    val cleanNumber = number.replace(" ", "").replace("-", "").replace("(", "").replace(")", "")

                    if (cleanNumber.isNotBlank() && !seenPhones.contains(cleanNumber)) {
                        seenPhones.add(cleanNumber)
                        contactList.add(DeviceContact(name = name, phoneNumber = number))
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("RealContactsHelper", "Notice reading contacts: ${e.message}")
        }

        return contactList
    }
}
