package org.xfort.xrockdroid.helper

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import org.xfort.xrockdroid.utils.RockUtil

/**
 ** Created by ZhangHuaXin on 2021/7/21.
 **/
object DownloadHelper {

    fun downloadFile(
        context: Context, url: String, publicDirType: String, title: String, des: String
    ): Long {
        val obj = context.getSystemService(Context.DOWNLOAD_SERVICE)
        if (obj == null) {
            RockUtil.openBrowser(context, url)
            return 0
        }
        val downloadManager = obj as DownloadManager
        val request = DownloadManager.Request(Uri.parse(url))
            .setDestinationInExternalPublicDir(publicDirType, title).setTitle(title)
            .setDescription(des)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.allowScanningByMediaScanner()
        try {
            return downloadManager.enqueue(request)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return -1
    } //fun downloadSong(songInterface: SongInterface): Long {
    //    val name = java.lang.String.format(
    //        "%s_%s_%s.%s",
    //        songInterface.name(),
    //        songInterface.singer(),
    //        songInterface.platformCode(),
    //        songInterface.mimeType()
    //    )
    //    return DownloadHelper.download(
    //        songInterface.playURL(),
    //        Environment.DIRECTORY_MUSIC,
    //        name,
    //        name
    //    )
    //}
}