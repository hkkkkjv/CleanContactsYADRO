package ru.kpfu.itis.cleancontacts.data.util

object ContactHasher {
    fun generateHash(displayName: String, phone: String?): String {
        val normalizedName = displayName.trim().lowercase()
        val normalizedPhone = phone?.replace(Regex("[^0-9+]"), "")?.ifEmpty { null }

        return listOfNotNull(normalizedName.ifEmpty { null }, normalizedPhone).sorted()
            .joinToString(separator = "|")
    }
}