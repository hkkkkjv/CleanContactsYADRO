package ru.kpfu.itis.cleancontacts.presentation.utils

import ru.kpfu.itis.cleancontacts.domain.model.Contact
import java.text.Collator
import java.util.Locale

object ContactGrouper {
    data class Section(val header: String, val contacts: List<Contact>)

    fun groupAndSort(contacts: List<Contact>): List<Section> {
        return contacts
            .groupBy { contact ->
                val firstChar = contact.displayName.trim().firstOrNull()
                firstChar?.uppercaseChar()?.toString() ?: "#"
            }
            .map { (header, list) ->
                Section(header, list.sortedBy { it.displayName })
            }
            .sortedWith { a, b ->
                Collator.getInstance(Locale.getDefault()).compare(a.header, b.header)
            }
    }
}