package ru.kpfu.itis.cleancontacts.domain.model

enum class DedupStatus(val code: Int) {
    SUCCESS(0),
    NO_DUPLICATES(1),
    ERROR(2);

    companion object {
        fun fromCode(code: Int): DedupStatus = when (code) {
            SUCCESS.code -> SUCCESS
            NO_DUPLICATES.code -> NO_DUPLICATES
            ERROR.code -> ERROR
            else -> ERROR
        }
    }
}