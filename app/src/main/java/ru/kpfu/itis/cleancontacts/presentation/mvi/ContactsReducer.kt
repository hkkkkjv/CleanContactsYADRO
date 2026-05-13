package ru.kpfu.itis.cleancontacts.presentation.mvi

import kotlinx.collections.immutable.persistentListOf
import ru.kpfu.itis.cleancontacts.R
import javax.inject.Inject

class ContactsReducer @Inject constructor() {
    fun reduce(current: ContactsState, intent: ContactsIntent): ContactsState = when (intent) {
        is ContactsIntent.LoadContacts -> current.copy(
            isLoading = true,
            errorResId = null,
            contacts = if (current.contacts.isEmpty()) current.contacts else current.contacts
        )

        is ContactsIntent.DeleteDuplicates -> current.copy(isDedupRunning = true, errorResId = null)
        is ContactsIntent.ContactsLoaded -> current.copy(
            isLoading = false,
            contacts = intent.contacts,
            errorResId = null
        )

        is ContactsIntent.DedupFinished -> current.copy(isDedupRunning = false)
        is ContactsIntent.ErrorOccurred -> current.copy(
            isLoading = false,
            isDedupRunning = false,
            errorResId = intent.errorResId,
            errorArgs = intent.args
        )
        is ContactsIntent.PermissionDenied -> current.copy(
            isLoading = false,
            isDedupRunning = false,
            errorResId = R.string.error_permission_denied,
            contacts = persistentListOf()
        )
    }
}