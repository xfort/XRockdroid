package org.xfort.xrockdroid.utils

import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import java.io.*
import java.nio.ByteBuffer
import java.nio.channels.FileLock

/**
 ** Created by ZhangHuaXin on 2021/7/5.
 *
 * 文件读写
 **/
object XRockFileUtil {

    /**
     * app的内部File存储路径
     */
    fun writeAppFile(context: Context, name: String, data: ByteArray?): Boolean {
        val dir = context.applicationContext?.filesDir ?: return false
        if (!dir.exists() || !dir.isDirectory) {
            dir.mkdirs()
        }
        val file = File(dir, name)
        if (data == null) {
            if (file.exists() && file.isFile) {
                file.delete()
            }
            return true
        }
        val channel = FileOutputStream(file).channel

        channel?.let { it ->
            var filelock: FileLock? = null
            try {
                filelock = it.tryLock()
                val bytesBuffer = ByteBuffer.allocate(data.size)
                bytesBuffer.put(data)
                bytesBuffer.flip()
                it.write(bytesBuffer)
                bytesBuffer.clear()
                it.close()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                filelock?.let { it.close() }
            }
        }
        return false
    }

    /**
     * app的内部File存储路径
     */
    fun readAppFile(context: Context, name: String): ByteArray? {
        val dir = context.applicationContext?.filesDir ?: return null
        val file = File(dir, name)
        if (!file.exists() || !file.isFile) {
            return null
        }
        var dataBytes = ByteArrayOutputStream()
        try {
            val fileChannel = FileInputStream(file).channel
            val buffer = ByteBuffer.allocateDirect(4 * 1024)
            while (fileChannel.read(buffer) != -1) {
                buffer.flip()
                dataBytes.write(buffer.array())
                buffer.clear()
            }
            fileChannel.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return null
        } finally {
            dataBytes.close()
        }
        return dataBytes.toByteArray()
    }

    /**
     * 读取asset文件
     */
    fun readAssetsFile(context: Context, path: String): ByteArray? {
        var dataBytes = ByteArrayOutputStream()

        try {
            val assetFile = context.assets.openFd(path)
            val filechannel = assetFile.createInputStream().channel
            val buffer = ByteBuffer.allocateDirect(4 * 1024)
            while (filechannel.read(buffer) != -1) {
                buffer.flip()
                dataBytes.write(buffer.array())
                buffer.clear()
            }
            filechannel.close()
            assetFile.close()
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } finally {
            dataBytes.close()
        }
        return dataBytes.toByteArray()
    }

    fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    fun isExternalStorageReadable(): Boolean {

        return Environment.getExternalStorageState() in setOf(
            Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY
        )
    }

    fun mediaContentUri(obj: Any): Uri? {
        if (obj is MediaStore.Images) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
        } else if (obj is MediaStore.Video) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }
        } else if (obj is MediaStore.Audio) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (obj is MediaStore.Downloads) {
                    MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL)
                }
            }
        }
        return null
    }

    /**
     * 读取 Uri格式的文件
     */
    suspend fun readContentFile(resolver: ContentResolver, contentUri: Uri): ByteArray? {
        var dataBytes: ByteArrayOutputStream? = null
        resolver.openInputStream(contentUri)?.use { input ->
            dataBytes = ByteArrayOutputStream()
            try {
                val dataInput = BufferedInputStream(input)
                val buffer = ByteArray(4 * 1024)
                var readLen = 0
                while (((dataInput.read(buffer, 0, buffer.size)).also { readLen = it }) != -1) {
                    dataBytes!!.write(buffer, 0, readLen)
                }
                input.close()
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            } finally {
                dataBytes?.close()
            }
        }
        return dataBytes?.toByteArray()
    }

    /**
     * 是否有 外部存储的全部文件读写权限
     */
    fun hasAllStoragepermission(ctx: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            var ok = true
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ).forEach {
                ok = ContextCompat.checkSelfPermission(ctx, it) != PackageManager.PERMISSION_GRANTED
                if (ok) {
                    return ok
                }
            }
            ok
        }
    }

    /**
     * 创建共享的媒体文件，仅支持 图片、视频、音频、还有Download
     */
    fun createExternalMediaFile(resolver: ContentResolver, obj: Any, name: String): Uri? {
        val mediaUri = mediaContentUri(obj)
        mediaUri?.let {
            try {
                return resolver.insert(mediaUri,
                    ContentValues(4).apply { put(MediaStore.MediaColumns.DISPLAY_NAME, name) })
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        return null
    }

    /**
     * 共享媒体文件,把文件新增到系统媒体库
     */
    fun shareMediaFile(file: File, resolver: ContentResolver, obj: Any): Uri? {
        val mediaTabUri = mediaContentUri(obj)
        if (mediaTabUri == null) {
            return null
        }
        var contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        var fileContentUri = resolver.insert(mediaTabUri, contentValues)
        if (fileContentUri == null) {
            return null
        }

        var writeRes = true
        resolver.openFileDescriptor(fileContentUri, "w", null)?.use { emptyFile ->
            val fileChannel = FileOutputStream(emptyFile.fileDescriptor).channel
            val data = FileInputStream(file).channel
            val readBuffer = ByteBuffer.allocateDirect(4 * 1024)
            var readLen = 0
            try {
                while ((data.read(readBuffer).also { readLen = it }) != -1) {
                    fileChannel.write(readBuffer)
                    readBuffer.flip()
                    readBuffer.clear()
                }
                fileChannel.close()
                data.close()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                writeRes = false
            } finally {
                fileChannel.close()
                data.close()
            }
        }
        contentValues.clear()
        if (writeRes) {
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(fileContentUri, contentValues, null, null)
        } else {
            resolver.delete(fileContentUri, "", null)
        }
        return fileContentUri
    }
}
