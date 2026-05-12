package ru.kpfu.itis.cleancontacts.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.kpfu.itis.cleancontacts.domain.model.Contact
import ru.kpfu.itis.cleancontacts.domain.model.DedupStatus

interface ContactsRepository {
    fun getContacts(): Flow<List<Contact>>
    suspend fun deleteDuplicates(): DedupStatus
}