package ru.kpfu.itis.cleancontacts.data.mapper

import android.database.Cursor
import android.provider.ContactsContract
import ru.kpfu.itis.cleancontacts.data.util.ContactHasher
import ru.kpfu.itis.cleancontacts.domain.model.Contact

object ContactMapper {

    val CONTACT_PROJECTION = arrayOf(
        ContactsContract.Contacts._ID,
        ContactsContract.Contacts.LOOKUP_KEY,
        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
        ContactsContract.Contacts.HAS_PHONE_NUMBER
    )

    fun mapCursorToContact(cursor: Cursor): Contact? {
        return try {
            val lookupKeyIndex = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.LOOKUP_KEY)
            val nameIndex = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
            val hasPhoneIndex = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER)

            val lookupKey = cursor.getString(lookupKeyIndex) ?: return null
            val displayName = cursor.getString(nameIndex)?.takeIf { it.isNotBlank() } ?: "Без имени"
            val hasPhone = cursor.getInt(hasPhoneIndex) == 1

            val fieldsHash = ContactHasher.generateHash(displayName, null, hasPhone)

            Contact(
                lookupKey = lookupKey,
                displayName = displayName,
                primaryPhone = null,
                fieldsHash = fieldsHash
            )
        } catch (e: Exception) {
            null
        }
    }
}