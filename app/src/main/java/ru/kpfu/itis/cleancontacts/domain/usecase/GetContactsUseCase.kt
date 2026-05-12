package ru.kpfu.itis.cleancontacts.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.kpfu.itis.cleancontacts.domain.model.Contact

interface GetContactsUseCase {
    operator fun invoke(): Flow<List<Contact>>
}