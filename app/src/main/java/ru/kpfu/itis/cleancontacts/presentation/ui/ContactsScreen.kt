package ru.kpfu.itis.cleancontacts.presentation.ui


import android.annotation.SuppressLint
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import ru.kpfu.itis.cleancontacts.presentation.mvi.ContactsEffect
import ru.kpfu.itis.cleancontacts.presentation.viewmodel.ContactsViewModel

@SuppressLint("LocalContextGetResourceValueCall")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    viewModel: ContactsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is ContactsEffect.ShowSnackbar -> {
                    val message = if (effect.formatArgs.isEmpty()) {
                        context.getString(effect.messageResId)
                    } else {
                        context.getString(effect.messageResId, *effect.formatArgs.toTypedArray())
                    }
                    snackbarHostState.showSnackbar(message)
                }
            }
        }
    }

    ContactsContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = { intent -> viewModel.handleIntent(intent) }
    )
}