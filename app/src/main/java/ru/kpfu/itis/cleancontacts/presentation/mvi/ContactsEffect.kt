package ru.kpfu.itis.cleancontacts.presentation.mvi

import androidx.annotation.StringRes

sealed class ContactsEffect {
    data class ShowSnackbar(
        @param:StringRes val messageResId: Int,
        val formatArgs: List<Any> = emptyList()
    ) : ContactsEffect()

    data object NavigateToSettings : ContactsEffect()
}