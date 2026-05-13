package ru.kpfu.itis.cleancontacts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import ru.kpfu.itis.cleancontacts.presentation.mvi.ContactsIntent
import ru.kpfu.itis.cleancontacts.presentation.permission.ContactsPermissionManager
import ru.kpfu.itis.cleancontacts.presentation.ui.ContactsScreen
import ru.kpfu.itis.cleancontacts.presentation.ui.theme.CleanContactsYADROTheme
import ru.kpfu.itis.cleancontacts.presentation.viewmodel.ContactsViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: ContactsViewModel by viewModels()
    private lateinit var permissionManager: ContactsPermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        permissionManager = ContactsPermissionManager(
            activity = this,
            onGranted = { viewModel.handleIntent(ContactsIntent.LoadContacts) },
            onDenied = { viewModel.handleIntent(ContactsIntent.PermissionDenied) }
        )
        permissionManager.requestIfNeeded()
        setContent {
            CleanContactsYADROTheme {
                ContactsScreen(
                    onOpenSettings = { permissionManager.openAppSettings() }
                )
            }
        }
    }
}
