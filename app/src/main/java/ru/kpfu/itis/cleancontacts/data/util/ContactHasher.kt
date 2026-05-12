package ru.kpfu.itis.cleancontacts.data.util

object ContactHasher {
    fun generateHash(displayName: String, phone: String?, hasPhone: Boolean): String {
        val normalizedNames = displayName.trim().lowercase()
        val normalizedPhone = phone?.trim()?.replace(Regex("[^0-9+]"), "")

        val fields = listOfNotNull(
            normalizedNames.takeIf { it.isNotEmpty() },
            normalizedPhone?.takeIf { it.isNotEmpty() },
            if (hasPhone) "has_phone" else null
        )

        return fields.sorted().joinToString(separator = "|")
    }
}