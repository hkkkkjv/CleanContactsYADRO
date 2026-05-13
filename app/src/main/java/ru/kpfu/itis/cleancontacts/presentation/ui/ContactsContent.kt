package ru.kpfu.itis.cleancontacts.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.kpfu.itis.cleancontacts.R
import ru.kpfu.itis.cleancontacts.domain.model.Contact
import ru.kpfu.itis.cleancontacts.presentation.mvi.ContactsIntent
import ru.kpfu.itis.cleancontacts.presentation.mvi.ContactsState
import ru.kpfu.itis.cleancontacts.presentation.utils.ContactGrouper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsContent(
    state: ContactsState,
    snackbarHostState: SnackbarHostState,
    onIntent: (ContactsIntent) -> Unit,
    modifier: Modifier = Modifier
) {

    val groupedContacts = remember(state.contacts) {
        ContactGrouper.groupAndSort(state.contacts.toList())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.screen_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = { onIntent(ContactsIntent.DeleteDuplicates) },
                    enabled = !state.isDedupRunning && state.contacts.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.isDedupRunning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(stringResource(R.string.dedup_button))
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                state.isLoading && state.contacts.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                state.errorResId != null && state.contacts.isEmpty() -> {
                    val errorMessage = if (state.errorArgs.isEmpty()) {
                        stringResource(state.errorResId)
                    } else {
                        stringResource(state.errorResId, *state.errorArgs.toTypedArray())
                    }
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.Center)
                    )
                }

                state.contacts.isEmpty() -> {
                    Text(
                        text = stringResource(R.string.contacts_empty),
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            bottom = 80.dp,
                            start = 12.dp,
                            end = 12.dp,
                            top = 8.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        groupedContacts.forEach { section ->
                            stickyHeader {
                                Surface(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = section.header,
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(
                                            horizontal = 12.dp,
                                            vertical = 8.dp
                                        )
                                    )
                                }
                            }
                            items(section.contacts, key = { it.lookupKey }) { contact ->
                                ContactRow(contact = contact)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactRow(contact: Contact) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = contact.displayName,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
            Text(
                text = contact.primaryPhone ?: stringResource(R.string.contact_no_phone),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
    }
}