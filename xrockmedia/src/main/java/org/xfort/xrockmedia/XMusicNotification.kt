package org.xfort.xrockmedia

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat //import com.google.android.exoplayer2.DefaultControlDispatcher
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager

/**
 ** Created by ZhangHuaXin on 2021/7/16.
 * 音乐播放
 **/
const val NOW_PLAYING_CHANNEL_ID = "org.xfort.xrockmedia.NOW_PLAYING"
const val NOW_PLAYING_NOTIFICATION_ID = 0xb339

class XMusicNotification(
    private val context: Context,
    sessionToken: MediaSessionCompat.Token,
    notificationListener: PlayerNotificationManager.NotificationListener
) {
    private val notificationManager: PlayerNotificationManager

    init {
        val mediaController = MediaControllerCompat(context, sessionToken)

        notificationManager = PlayerNotificationManager.Builder(
            context, NOW_PLAYING_NOTIFICATION_ID, NOW_PLAYING_CHANNEL_ID
        ).setChannelNameResourceId(R.string.notification_channel)
            .setChannelDescriptionResourceId(R.string.notification_channel_description)
            .setMediaDescriptionAdapter(DescriptionAdapter(mediaController))
            .setNotificationListener(notificationListener).build().apply {
                setMediaSessionToken(sessionToken)
                setSmallIcon(R.drawable.ic_notification) // Don't display the rewind or fast-forward buttons.
                //setControlDispatcher(DefaultControlDispatcher(0, 0))
            }
    }

    fun hideNotification() {
        notificationManager.setPlayer(null)
    }

    fun showNotificationForPlayer(player: Player) {
        notificationManager.setPlayer(player)
    }

    private inner class DescriptionAdapter(private val controller: MediaControllerCompat) :
        PlayerNotificationManager.MediaDescriptionAdapter {

        var currentIconUri: Uri? = null
        var currentBitmap: Bitmap? = null

        override fun createCurrentContentIntent(player: Player): PendingIntent? =
            controller.sessionActivity

        override fun getCurrentContentText(player: Player) = //player.currentStaticMetadata.
            controller.metadata.description.subtitle.toString()

        override fun getCurrentContentTitle(player: Player) =
            controller.metadata.description.title.toString()

        override fun getCurrentLargeIcon(
            player: Player, callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            return null
        }
    }
}


const val NOTIFICATION_LARGE_ICON_SIZE = 144 // px


private const val MODE_READ_ONLY = "r"