package ru.kpfu.itis.cleancontacts.presentation.mvi

import javax.inject.Inject

class ContactsReducer @Inject constructor() {
    fun reduce(current: ContactsState, intent: ContactsIntent): ContactsState = when (intent) {
        is ContactsIntent.LoadContacts -> current.copy(isLoading = true, errorResId = null)
        is ContactsIntent.DeleteDuplicates -> current.copy(isDedupRunning = true, errorResId = null)
        is ContactsIntent.ContactsLoaded -> current.copy(
            isLoading = false,
            contacts = intent.contacts
        )

        is ContactsIntent.DedupFinished -> current.copy(isDedupRunning = false)
        is ContactsIntent.ErrorOccurred -> current.copy(
            isLoading = false,
            isDedupRunning = false,
            errorResId = intent.errorResId,
            errorArgs = intent.args
        )
    }
}