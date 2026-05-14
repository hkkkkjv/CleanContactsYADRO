package ru.kpfu.itis.cleancontacts.data.util

import android.provider.ContactsContract

object ContactCanonicalizer {

    data class DataRow(
        val contactId: Long,
        val lookupKey: String,
        val mimeType: String,
        val data1: String?, val data2: String?, val data3: String?,
        val data4: String?, val data5: String?, val data6: String?,
        val data7: String?, val data8: String?, val data9: String?
    )

    fun buildCanonicalString(rows: List<DataRow>): String {
        val sb = StringBuilder()
        val grouped = rows.groupBy { it.mimeType }

        appendBlock(
            sb,
            "N",
            grouped,
            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
        ) { r ->
            listOfNotNull(
                r.data4?.trim()?.lowercase(),
                r.data2?.trim()?.lowercase(),
                r.data5?.trim()?.lowercase(),
                r.data3?.trim()?.lowercase(),
                r.data6?.trim()?.lowercase(),
                r.data7?.trim()?.lowercase(),
                r.data8?.trim()?.lowercase(),
                r.data9?.trim()?.lowercase(),
                r.data1?.trim()?.lowercase()
            ).filter { it.isNotEmpty() }.sorted().joinToString(",")
        }

        appendListBlock(
            sb,
            "NN",
            grouped,
            ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE
        ) { r ->
            r.data1?.trim()?.lowercase()?.takeIf { it.isNotEmpty() }
        }
        appendListBlock(
            sb,
            "PH",
            grouped,
            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
        ) { r ->
            r.data1?.replace(Regex("[^0-9+]"), "")?.takeIf { it.isNotEmpty() }
        }
        appendListBlock(
            sb,
            "EM",
            grouped,
            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
        ) { r ->
            r.data1?.trim()?.lowercase()?.takeIf { it.isNotEmpty() }
        }
        appendBlock(
            sb,
            "ORG",
            grouped,
            ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
        ) { r ->
            listOfNotNull(
                r.data1?.trim()?.lowercase(),
                r.data4?.trim()?.lowercase(),
                r.data5?.trim()?.lowercase()
            ).filter { it.isNotEmpty() }.sorted().joinToString(",")
        }
        appendListBlock(
            sb,
            "ADDR",
            grouped,
            ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE
        ) { r ->
            r.data1?.trim()?.lowercase()?.takeIf { it.isNotEmpty() }
        }
        appendListBlock(
            sb,
            "WEB",
            grouped,
            ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE
        ) { r ->
            r.data1?.trim()?.lowercase()?.takeIf { it.isNotEmpty() }
        }
        appendListBlock(
            sb,
            "EVT",
            grouped,
            ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE
        ) { r ->
            "${r.data1}|${r.data2}".trim().lowercase().takeIf { it.isNotEmpty() }
        }
        appendListBlock(
            sb,
            "NOTE",
            grouped,
            ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE
        ) { r ->
            r.data1?.trim()?.takeIf { it.isNotEmpty() }
        }
        appendListBlock(
            sb,
            "REL",
            grouped,
            ContactsContract.CommonDataKinds.Relation.CONTENT_ITEM_TYPE
        ) { r ->
            listOfNotNull(
                r.data1?.trim()?.lowercase(),
                r.data2?.trim()?.lowercase()
            ).filter { it.isNotEmpty() }.joinToString(",")
        }
        appendListBlock(
            sb,
            "SIP",
            grouped,
            ContactsContract.CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE
        ) { r ->
            r.data1?.trim()?.lowercase()?.takeIf { it.isNotEmpty() }
        }
        return sb.toString()
    }

    private fun appendBlock(
        sb: StringBuilder,
        prefix: String,
        grouped: Map<String, List<DataRow>>,
        mimeType: String,
        extractor: (DataRow) -> String?
    ) {
        grouped[mimeType]?.mapNotNull(extractor)?.filter { it.isNotEmpty() }?.sorted()?.let {
            if (it.isNotEmpty()) sb.append("$prefix:").append(it.joinToString(",")).append("|")
        }
    }

    private fun appendListBlock(
        sb: StringBuilder,
        prefix: String,
        grouped: Map<String, List<DataRow>>,
        mimeType: String,
        extractor: (DataRow) -> String?
    ) {
        grouped[mimeType]?.mapNotNull(extractor)?.filter { it.isNotEmpty() }?.sorted()?.let {
            if (it.isNotEmpty()) sb.append("$prefix:").append(it.joinToString(";")).append("|")
        }
    }
}