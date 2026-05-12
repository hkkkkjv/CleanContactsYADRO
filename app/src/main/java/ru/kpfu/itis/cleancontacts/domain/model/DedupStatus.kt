package ru.kpfu.itis.cleancontacts.domain.model

enum class DedupStatus {
    SUCCESS,
    NO_DUPLICATES,
    ERROR;

    companion object {
        const val CODE_SUCCESS = 0
        const val CODE_NO_DUPLICATES = 1
        const val CODE_ERROR = 2

        fun statusFromCode(code: Int): DedupStatus = when (code) {
            CODE_SUCCESS -> SUCCESS
            CODE_NO_DUPLICATES -> NO_DUPLICATES
            else -> ERROR
        }
    }
}