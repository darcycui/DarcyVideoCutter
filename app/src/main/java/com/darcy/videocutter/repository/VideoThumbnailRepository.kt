package com.darcy.videocutter.repository

import android.R.attr.bitmap
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import androidx.core.net.toUri
import com.darcy.lib_log_toast.exts.logE
import com.darcy.lib_saf_select.utils.UriUtil
import com.darcy.videocutter.app.App
import com.darcy.videocutter.interface_dapters.IVideoThumbnailRepository
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files.exists

class VideoThumbnailRepository(
    private val bitmapSizes: MutableList<Pair<Int, Int>>,
    private val context: Context = App.getInstance(),
    private val fileRepository: FileRepository = FileRepository()
) : IVideoThumbnailRepository {

    override suspend fun generateVideoThumbnailByRetriever(videoUriStr: String?): Bitmap? {
        if (videoUriStr.isNullOrEmpty()) {
            return null
        }
        val videoUri = videoUriStr.toUri()
        var retriever: MediaMetadataRetriever? = null
        val bitmap = try {
            retriever = MediaMetadataRetriever().apply {
                context.contentResolver.openFileDescriptor(videoUri, "r")?.use { pfd ->
                    setDataSource(pfd.fileDescriptor)
                }
            }
            // 第一秒的帧作为缩略图
            retriever.getFrameAtTime(
                1000 * 1000,
                MediaMetadataRetriever.OPTION_CLOSEST_SYNC
            )?.apply {
                bitmapSizes.add(this.width to this.height)
            }
        } catch (e: Exception) {
            logE("生成缩略图失败 $e")
            null
        } finally {
            retriever?.release()
        }
        return bitmap
    }

    override suspend fun saveVideoThumbnail(videoUriStr: String?, bitmap: Bitmap?): File? {
        if (videoUriStr.isNullOrEmpty() || bitmap == null) {
            return null
        }
        val videoUri = videoUriStr.toUri()

        // 2. 创建目标目录
        val storageDir = fileRepository.getVideoThumbnailFile()?.apply {
            if (!exists()) mkdirs() // 确保目录存在
        }

        // 3. 生成唯一文件名（示例：使用 URI 哈希 + 时间戳）
        val fileName =
            "thumb_${UriUtil.getFileNameFromUri(context, videoUri)}_${videoUri.hashCode()}.jpg"
        val thumbnailFile = File(storageDir, fileName)
        if (thumbnailFile.exists()) {
            return thumbnailFile
        }
        // 4. 保存到文件
        return try {
            thumbnailFile.apply {
                FileOutputStream(this).use { fos ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos) // JPEG 质量 85%
                    fos.flush()
                }
                logE("保存缩略图成功: $absolutePath")
            }
        } catch (e: Exception) {
            logE("保存缩略图失败 $e")
            null
        }
    }

}