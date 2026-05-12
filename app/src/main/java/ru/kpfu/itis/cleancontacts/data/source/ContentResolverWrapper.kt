package ru.kpfu.itis.cleancontacts.data.source

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri

internal fun ContentResolver.queryContactsSafe(
    uri: Uri,
    projection: Array<String>?,
    selection: String? = null,
    selectionArgs: Array<String>? = null,
    sortOrder: String? = null
): Cursor? {
    return try {
        query(uri, projection, selection, selectionArgs, sortOrder)
    } catch (e: SecurityException) {
        null
    } catch (e: IllegalArgumentException) {
        null
    }
}