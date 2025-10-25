package com.abdullah.todoapp.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import com.abdullah.todoapp.model.Attachment
import com.abdullah.todoapp.model.AttachmentType
import java.io.File

object FileUtils {
    fun getUriForFile(context: Context, filePath: String): Uri {
        val file = File(filePath)
        return FileProvider.getUriForFile(
            context,
            context.packageName + ".provider",
            file
        )
    }

    fun getFileFromUri(context: Context, uri: Uri): File {
        val resolver = context.contentResolver
        val fileName = resolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex != -1) cursor.getString(nameIndex) else null
        } ?: uri.lastPathSegment ?: ("attachment_" + System.currentTimeMillis())

        val destFile = File(context.cacheDir, fileName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        // Medya tarayıcısına bildir
        context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(destFile)))
        return destFile
    }
} 