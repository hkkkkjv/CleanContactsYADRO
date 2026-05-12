package ru.kpfu.itis.cleancontacts

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import dagger.hilt.android.AndroidEntryPoint
import ru.kpfu.itis.cleancontacts.presentation.ui.ContactsScreen
import ru.kpfu.itis.cleancontacts.presentation.ui.theme.CleanContactsYADROTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestPermission.launch(Manifest.permission.READ_CONTACTS)
        setContent {
            CleanContactsYADROTheme {
                ContactsScreen()
            }
        }
    }
}
