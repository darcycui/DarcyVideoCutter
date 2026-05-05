package com.darcy.videocutter.utils

import android.content.Context
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import androidx.core.net.toUri
import java.io.IOException
import java.util.Locale

/**
 * 获取uri对应文件的格式
 */
object VideoFormatUtils {
    private const val MP4_FORMAT = "mp4"
    fun getVideoFormat(context: Context, uri: Uri?): String {
        if (uri == null) return MP4_FORMAT
        // 方法1：尝试从文件名获取
        var format = getFormatFromUriPath(uri)
        // 方法2：如果获取失败，尝试从 ContentResolver 获取
        if (format == null && uri.scheme != null && uri.scheme == "content") {
            format = getFormatFromContentProvider(context, uri)
        }
        // 方法3：最后使用 MediaMetadataRetriever
        if (format == null) {
            format = getVideoFormatWithMetadataRetriever(context, uri)
        }
        return format ?: MP4_FORMAT
    }

    private fun getFormatFromUriPath(uri: Uri): String? {
        var path: String? = null

        path = if ("file" == uri.scheme) {
            uri.path
        } else {
            uri.toString()
        }

        if (path != null) {
            val lastDot = path.lastIndexOf('.')
            val lastSlash = path.lastIndexOf('/')

            if (lastDot != -1 && lastDot > lastSlash && lastDot < path.length - 1) {
                val extension = path.substring(lastDot + 1).lowercase(Locale.getDefault())


                // 常见的视频格式
                val videoFormats: MutableSet<String?> = HashSet<String?>(
                    mutableListOf<String?>(
                        "mp4", "avi", "mkv", "mov", "wmv", "flv", "webm",
                        "mpeg", "mpg", "3gp", "3g2", "m4v", "ts", "vob"
                    )
                )

                if (videoFormats.contains(extension)) {
                    return extension
                }
            }
        }

        return null
    }

    // 从 Content Provider 获取格式
    private fun getFormatFromContentProvider(context: Context, uri: Uri): String? {
        var cursor: Cursor? = null

        try {
            val projection = arrayOf<String?>(
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.MediaColumns.DISPLAY_NAME
            )

            cursor = context.contentResolver.query(
                uri, projection, null, null, null
            )

            if (cursor != null && cursor.moveToFirst()) {
                // 1. 从 MIME_TYPE
                val mimeTypeIndex = cursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE)
                if (mimeTypeIndex != -1) {
                    val mimeType = cursor.getString(mimeTypeIndex)
                    if (mimeType != null) {
                        val format = mimeTypeToFormat(mimeType)
                        if (format != null) {
                            return format
                        }
                    }
                }


                // 2. 从文件名
                val nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    val fileName = cursor.getString(nameIndex)
                    return getFormatFromUriPath(("file:///$fileName").toUri())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }

        return null
    }

    // 从 MIME 类型获取格式
    private fun mimeTypeToFormat(mimeType: String?): String? {
        if (mimeType == null) return null

        val mimeTypeMap: MutableMap<String?, String?> = HashMap<String?, String?>()
        mimeTypeMap["video/mp4"] = "mp4"
        mimeTypeMap["video/3gpp"] = "3gp"
        mimeTypeMap["video/avi"] = "avi"
        mimeTypeMap["video/x-msvideo"] = "avi"
        mimeTypeMap["video/x-ms-wmv"] = "wmv"
        mimeTypeMap["video/mpeg"] = "mpeg"
        mimeTypeMap["video/mp2t"] = "ts"
        mimeTypeMap["video/webm"] = "webm"
        mimeTypeMap["video/quicktime"] = "mov"
        mimeTypeMap["video/x-matroska"] = "mkv"
        mimeTypeMap["video/x-flv"] = "flv"
        mimeTypeMap["video/x-m4v"] = "m4v"

        return mimeTypeMap[mimeType.lowercase(Locale.getDefault())]
    }

    fun getVideoFormatWithMetadataRetriever(context: Context?, uri: Uri): String? {
        var retriever: MediaMetadataRetriever? = null

        try {
            retriever = MediaMetadataRetriever()

            // 根据 URI 类型设置数据源
            if (uri.scheme != null && uri.scheme!!.startsWith("http")) {
                retriever.setDataSource(uri.toString(), HashMap<String?, String?>())
            } else {
                // 对于本地文件
                retriever.setDataSource(context, uri)
            }
            // 获取 MIME 类型
            val mimeType = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_MIMETYPE
            )

            return mimeTypeToFormat(mimeType)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return null
        } finally {
            if (retriever != null) {
                try {
                    retriever.release()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}