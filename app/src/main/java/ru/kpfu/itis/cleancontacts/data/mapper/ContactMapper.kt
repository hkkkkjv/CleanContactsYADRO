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

    fun mapRow(cursor: Cursor, indexes: ContactColumnIndexes, phone: String?): Contact? {
        val lookupKey = cursor.getString(indexes.lookupKey) ?: return null
        val displayName =
            cursor.getString(indexes.displayName)?.takeIf { it.isNotBlank() } ?: return null
        return Contact(
            lookupKey = lookupKey,
            displayName = displayName,
            primaryPhone = phone,
            fieldsHash = ContactHasher.generateHash(displayName, phone)
        )
    }

    fun readIndexes(cursor: Cursor): ContactColumnIndexes {
        return ContactColumnIndexes(
            id = cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID),
            lookupKey = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.LOOKUP_KEY),
            displayName = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY),
            hasPhone = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER)
        )
    }
}