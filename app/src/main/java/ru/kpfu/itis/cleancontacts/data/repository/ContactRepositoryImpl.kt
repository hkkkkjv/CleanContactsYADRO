package ru.kpfu.itis.cleancontacts.data.repository

import android.content.ContentResolver
import android.provider.ContactsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import ru.kpfu.itis.cleancontacts.data.bridge.AidlServiceBridge
import ru.kpfu.itis.cleancontacts.data.mapper.ContactMapper
import ru.kpfu.itis.cleancontacts.data.source.queryContactsSafe
import ru.kpfu.itis.cleancontacts.domain.model.Contact
import ru.kpfu.itis.cleancontacts.domain.model.DedupStatus
import ru.kpfu.itis.cleancontacts.domain.repository.ContactsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactsRepositoryImpl @Inject constructor(
    private val contentResolver: ContentResolver,
    private val aidlBridge: AidlServiceBridge
) : ContactsRepository {

    private val refreshTrigger = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getContacts(): Flow<List<Contact>> {
        return refreshTrigger
            .flatMapLatest { loadContactsFromDevice() }
            .flowOn(Dispatchers.IO)
    }

    override fun invalidateCache() {
        refreshTrigger.value++
    }

    override suspend fun deleteDuplicates(): DedupStatus {
        val status = aidlBridge.removeDuplicates()
        if (status == DedupStatus.SUCCESS) {
            invalidateCache()
        }
        return status
    }

    private fun loadContactsFromDevice(): Flow<List<Contact>> = flow {
        val cursor = contentResolver.queryContactsSafe(
            uri = ContactsContract.Contacts.CONTENT_URI,
            projection = ContactMapper.CONTACT_PROJECTION,
            sortOrder = "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} ASC"
        )

        if (cursor == null) {
            emit(emptyList())
            return@flow
        }

        val contacts = mutableListOf<Contact>()

        cursor.use {
            val indexes = ContactMapper.readIndexes(it)

            while (it.moveToNext()) {
                val hasPhone = it.getInt(indexes.hasPhone) == 1
                val phone = if (hasPhone) {
                    fetchPrimaryPhone(it.getString(indexes.lookupKey))
                } else {
                    null
                }

                ContactMapper.mapRow(it, indexes, phone)?.let { contact ->
                    contacts.add(contact)
                }
            }
        }
        emit(contacts)
    }

    private fun fetchPrimaryPhone(lookupKey: String): String? {
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val lookupCol = ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY

        contentResolver.queryContactsSafe(
            uri = uri,
            projection = projection,
            selection = "$lookupCol = ? AND ${ContactsContract.CommonDataKinds.Phone.IS_PRIMARY} = ?",
            selectionArgs = arrayOf(lookupKey, "1")
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(
                    cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                )
            }
        }

        return contentResolver.queryContactsSafe(
            uri = uri,
            projection = projection,
            selection = "$lookupCol = ?",
            selectionArgs = arrayOf(lookupKey)
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getString(
                    cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                )
            } else null
        }
    }

    companion object {
        private const val TAG = "ContactsRepository"
    }
}