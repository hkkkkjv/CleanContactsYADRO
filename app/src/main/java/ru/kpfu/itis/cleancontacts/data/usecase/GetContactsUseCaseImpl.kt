package ru.kpfu.itis.cleancontacts.data.usecase

import kotlinx.coroutines.flow.Flow
import ru.kpfu.itis.cleancontacts.domain.model.Contact
import ru.kpfu.itis.cleancontacts.domain.repository.ContactsRepository
import ru.kpfu.itis.cleancontacts.domain.usecase.GetContactsUseCase
import javax.inject.Inject

class GetContactsUseCaseImpl @Inject constructor(
    private val repository: ContactsRepository
) : GetContactsUseCase {
    override operator fun invoke(): Flow<List<Contact>> {
        return repository.getContacts()
    }
}