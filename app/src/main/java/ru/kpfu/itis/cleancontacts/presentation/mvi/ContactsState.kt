package ru.kpfu.itis.cleancontacts.presentation.mvi

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import ru.kpfu.itis.cleancontacts.domain.model.Contact

@Stable
data class ContactsState(
    val contacts: ImmutableList<Contact> = persistentListOf(),
    val isLoading: Boolean = false,
    val isDedupRunning: Boolean = false,
    @param:StringRes val errorResId: Int? = null,
    val errorArgs: List<Any> = emptyList()
){
    fun hasError(): Boolean = errorResId != null
}