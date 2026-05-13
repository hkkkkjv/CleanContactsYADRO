package ru.kpfu.itis.cleancontacts.presentation.mvi

import androidx.annotation.StringRes
import kotlinx.collections.immutable.ImmutableList
import ru.kpfu.itis.cleancontacts.domain.model.Contact
import ru.kpfu.itis.cleancontacts.domain.model.DedupStatus


sealed interface ContactsIntent {
    object LoadContacts : ContactsIntent
    object DeleteDuplicates : ContactsIntent
    object PermissionDenied : ContactsIntent
    data class ContactsLoaded(val contacts: ImmutableList<Contact>) : ContactsIntent
    data class DedupFinished(val status: DedupStatus) : ContactsIntent
    data class ErrorOccurred(
        @param:StringRes val errorResId: Int,
        val args: List<Any> = emptyList()
    ) : ContactsIntent
}