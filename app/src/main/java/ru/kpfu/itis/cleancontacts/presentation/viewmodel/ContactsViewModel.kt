package ru.kpfu.itis.cleancontacts.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.kpfu.itis.cleancontacts.R
import ru.kpfu.itis.cleancontacts.domain.model.DedupStatus
import ru.kpfu.itis.cleancontacts.domain.usecase.DeleteDuplicatesUseCase
import ru.kpfu.itis.cleancontacts.domain.usecase.GetContactsUseCase
import ru.kpfu.itis.cleancontacts.presentation.mvi.ContactsEffect
import ru.kpfu.itis.cleancontacts.presentation.mvi.ContactsIntent
import ru.kpfu.itis.cleancontacts.presentation.mvi.ContactsReducer
import ru.kpfu.itis.cleancontacts.presentation.mvi.ContactsState
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val getContactsUseCase: GetContactsUseCase,
    private val deleteDuplicatesUseCase: DeleteDuplicatesUseCase,
    private val reducer: ContactsReducer
) : ViewModel() {

    private val _state = MutableStateFlow(ContactsState())
    val state: StateFlow<ContactsState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<ContactsEffect>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effect: SharedFlow<ContactsEffect> = _effect.asSharedFlow()

    init {
        handleIntent(ContactsIntent.LoadContacts)
    }

    fun handleIntent(intent: ContactsIntent) {
        viewModelScope.launch {
            _state.update { currentState -> reducer.reduce(currentState, intent) }

            when (intent) {
                is ContactsIntent.LoadContacts -> loadContacts()
                is ContactsIntent.DeleteDuplicates -> startDeduplication()
                else -> Unit
            }
        }
    }

    private fun loadContacts() {
        viewModelScope.launch {
            getContactsUseCase()
                .catch { e ->
                    _effect.emit(ContactsEffect.ShowSnackbar(R.string.error_loading_contacts))
                    handleIntent(ContactsIntent.ErrorOccurred(
                        R.string.error_loading_contacts,
                    ))
                }
                .collect { contacts ->
                    handleIntent(ContactsIntent.ContactsLoaded(contacts.toImmutableList()))
                }
        }
    }

    private fun startDeduplication() {
        viewModelScope.launch {
            try {
                val status = deleteDuplicatesUseCase()
                handleIntent(ContactsIntent.DedupFinished(status))
                val effect = when (status) {
                    DedupStatus.SUCCESS -> ContactsEffect.ShowSnackbar(R.string.dedup_success)
                    DedupStatus.NO_DUPLICATES -> ContactsEffect.ShowSnackbar(R.string.dedup_no_duplicates)
                    DedupStatus.ERROR -> ContactsEffect.ShowSnackbar(R.string.dedup_error)
                }
                _effect.emit(effect)
            } catch (e: Exception) {
                _effect.emit(ContactsEffect.ShowSnackbar(R.string.error_service_unavailable))
                handleIntent(ContactsIntent.ErrorOccurred(R.string.error_service_unavailable))
            }
        }
    }
}