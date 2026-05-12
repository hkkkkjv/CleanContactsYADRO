package ru.kpfu.itis.cleancontacts.domain.usecase

import ru.kpfu.itis.cleancontacts.domain.model.DedupStatus

interface DeleteDuplicatesUseCase {
    suspend operator fun invoke(): DedupStatus
}