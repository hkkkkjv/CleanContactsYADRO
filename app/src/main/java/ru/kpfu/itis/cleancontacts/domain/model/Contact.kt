package ru.kpfu.itis.cleancontacts.domain.model

data class Contact(
    val lookupKey: String,
    val displayName: String,
    val primaryPhone: String?,
    //хэш всех полей через разделитель
    val fieldsHash: String,
)