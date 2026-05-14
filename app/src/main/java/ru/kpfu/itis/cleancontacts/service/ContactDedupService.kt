package ru.kpfu.itis.cleancontacts.service

import android.app.Service
import android.content.ContentResolver
import android.content.Intent
import android.os.IBinder
import android.provider.ContactsContract
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import ru.kpfu.itis.cleancontacts.data.util.ContactCanonicalizer
import ru.kpfu.itis.cleancontacts.domain.model.DedupStatus
import ru.kpfu.itis.cleancontacts.service.aidl.IContactService
import ru.kpfu.itis.cleancontacts.service.aidl.IStatusCallback

class ContactDedupService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val contactsContentResolver: ContentResolver by lazy { applicationContext.contentResolver }

    private val binder = object : IContactService.Stub() {
        override fun removeDuplicates(callback: IStatusCallback?) {
            serviceScope.launch {
                try {
                    val status = performDeduplication()
                    callback?.onResult(status.code, status.name)
                } catch (e: Exception) {
                    Log.e("ContactDedupService", "Deduplication failed", e)
                    callback?.onResult(
                        DedupStatus.ERROR.code,
                        e.localizedMessage ?: "Internal error"
                    )
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder = binder
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun performDeduplication(): DedupStatus {
        val seenHashes = mutableMapOf<String, Long>()
        val duplicatesToDelete = mutableListOf<Long>()

        val mimeTypes = listOf(
            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.Relation.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE
        )

        val selection = "${ContactsContract.Data.MIMETYPE} IN ('${mimeTypes.joinToString("','")}')"
        val projection = arrayOf(
            ContactsContract.Data.CONTACT_ID,
            ContactsContract.Data.LOOKUP_KEY,
            ContactsContract.Data.MIMETYPE,
            ContactsContract.Data.DATA1, ContactsContract.Data.DATA2, ContactsContract.Data.DATA3,
            ContactsContract.Data.DATA4, ContactsContract.Data.DATA5, ContactsContract.Data.DATA6,
            ContactsContract.Data.DATA7, ContactsContract.Data.DATA8, ContactsContract.Data.DATA9
        )

        val sortOrder = "${ContactsContract.Data.CONTACT_ID} ASC"

        contactsContentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            var currentContactId: Long? = null
            var currentLookupKey: String? = null
            val currentRows = mutableListOf<ContactCanonicalizer.DataRow>()

            fun flushGroup() {
                val id = currentContactId ?: return
                val lk = currentLookupKey ?: return
                if (currentRows.isEmpty()) return

                val canonical = ContactCanonicalizer.buildCanonicalString(currentRows)
                seenHashes[canonical]?.let { firstId ->
                    duplicatesToDelete.add(id)
                } ?: run {
                    seenHashes[canonical] = id
                }
                currentRows.clear()
            }

            while (cursor.moveToNext()) {
                val contactId =
                    cursor.getLong(cursor.getColumnIndexOrThrow(ContactsContract.Data.CONTACT_ID))
                val lookupKey =
                    cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Data.LOOKUP_KEY))

                if (contactId != currentContactId) {
                    flushGroup()
                    currentContactId = contactId
                    currentLookupKey = lookupKey
                }

                currentRows.add(
                    ContactCanonicalizer.DataRow(
                        contactId = contactId,
                        lookupKey = lookupKey,
                        mimeType = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Data.MIMETYPE)),
                        data1 = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Data.DATA1)),
                        data2 = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Data.DATA2)),
                        data3 = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Data.DATA3)),
                        data4 = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Data.DATA4)),
                        data5 = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Data.DATA5)),
                        data6 = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Data.DATA6)),
                        data7 = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Data.DATA7)),
                        data8 = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Data.DATA8)),
                        data9 = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Data.DATA9))
                    )
                )
            }
            flushGroup()
        }

        var deletedCount = 0
        for (id in duplicatesToDelete) {
            val count = contactsContentResolver.delete(
                ContactsContract.RawContacts.CONTENT_URI,
                "${ContactsContract.RawContacts.CONTACT_ID} = ?",
                arrayOf(id.toString())
            )
            if (count > 0) deletedCount++
            else Log.w("ContactDedupService", "Failed to delete contact id=$id")
        }

        Log.d(
            "ContactDedupService",
            "Deduplication finished. Found ${duplicatesToDelete.size} duplicates, deleted $deletedCount"
        )
        return if (deletedCount > 0) DedupStatus.SUCCESS else DedupStatus.NO_DUPLICATES
    }
}