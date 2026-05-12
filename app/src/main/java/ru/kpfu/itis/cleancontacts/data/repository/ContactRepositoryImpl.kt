package ru.kpfu.itis.cleancontacts.data.repository

import android.content.ContentResolver
import android.provider.ContactsContract
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import ru.kpfu.itis.cleancontacts.data.mapper.ContactMapper
import ru.kpfu.itis.cleancontacts.data.source.queryContactsSafe
import ru.kpfu.itis.cleancontacts.data.util.ContactHasher
import ru.kpfu.itis.cleancontacts.domain.model.Contact
import ru.kpfu.itis.cleancontacts.domain.model.DedupStatus
import ru.kpfu.itis.cleancontacts.domain.repository.ContactsRepository

@Singleton
class ContactsRepositoryImpl @Inject constructor(
    private val contentResolver: ContentResolver
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
        return DedupStatus.ERROR //ToDo
    }

    private fun loadContactsFromDevice(): Flow<List<Contact>> = flow {
        val contacts = mutableListOf<Contact>()

        try {
            contentResolver.queryContactsSafe(
                uri = ContactsContract.Contacts.CONTENT_URI,
                projection = ContactMapper.CONTACT_PROJECTION,
                selection = null,
                selectionArgs = null,
                sortOrder = "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} ASC"
            )?.use { cursor ->
                val hasPhoneIndex =
                    cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                while (cursor.moveToNext()) {
                    ContactMapper.mapCursorToContact(cursor)?.let { baseContact ->
                        val hasPhone = cursor.getInt(hasPhoneIndex) == 1
                        val phone = if (hasPhone) {
                            getPrimaryPhone(baseContact.lookupKey)
                        } else {
                            null
                        }

                        val finalHash = ContactHasher.generateHash(
                            displayName = baseContact.displayName,
                            phone = phone,
                            hasPhone = phone != null
                        )

                        contacts.add(baseContact.copy(primaryPhone = phone, fieldsHash = finalHash))
                    }
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        emit(contacts)
    }

    private fun getPrimaryPhone(lookupKey: String): String? {
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)

        val selectionPrimary = "${ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY} = ? AND ${ContactsContract.CommonDataKinds.Phone.IS_PRIMARY} = ?"
        val argsPrimary = arrayOf(lookupKey, "1")

        contentResolver.query(uri, projection, selectionPrimary, argsPrimary, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
            }
        }

        val selectionAny = "${ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY} = ?"
        val argsAny = arrayOf(lookupKey)

        return contentResolver.query(uri, projection, selectionAny, argsAny, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
            } else {
                null
            }
        }
    }
}