package ru.kpfu.itis.cleancontacts.data.usecase

import ru.kpfu.itis.cleancontacts.domain.model.DedupStatus
import ru.kpfu.itis.cleancontacts.domain.repository.ContactsRepository
import ru.kpfu.itis.cleancontacts.domain.usecase.DeleteDuplicatesUseCase
import javax.inject.Inject

class DeleteDuplicatesUseCaseImpl @Inject constructor(
    private val repository: ContactsRepository
) : DeleteDuplicatesUseCase {
    override suspend operator fun invoke(): DedupStatus {
        return repository.deleteDuplicates()
    }
}