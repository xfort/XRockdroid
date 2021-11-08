package org.xfort.xrockmedia

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ui.PlayerNotificationManager


/**
 ** Created by ZhangHuaXin on 2021/7/16.
 * 音乐播放服务
 **/
const val UAMP_BROWSABLE_ROOT = "/"
const val UAMP_EMPTY_ROOT = "@empty@"
const val UAMP_RECOMMENDED_ROOT = "__RECOMMENDED__"
const val UAMP_ALBUMS_ROOT = "__ALBUMS__"
const val UAMP_RECENT_ROOT = "__RECENT__"

const val MEDIA_SEARCH_SUPPORTED = "android.media.browse.SEARCH_SUPPORTED"

const val RESOURCE_ROOT_URI = "android.resource://com.example.android.uamp.next/drawable/"

const val NETWORK_FAILURE = "com.example.android.uamp.media.session.NETWORK_FAILURE"

/** Content styling constants */
private const val CONTENT_STYLE_BROWSABLE_HINT = "android.media.browse.CONTENT_STYLE_BROWSABLE_HINT"
private const val CONTENT_STYLE_PLAYABLE_HINT = "android.media.browse.CONTENT_STYLE_PLAYABLE_HINT"
private const val CONTENT_STYLE_SUPPORTED = "android.media.browse.CONTENT_STYLE_SUPPORTED"
private const val CONTENT_STYLE_LIST = 1
private const val CONTENT_STYLE_GRID = 2

private const val UAMP_USER_AGENT = "uamp.next"

val MEDIA_DESCRIPTION_EXTRAS_START_PLAYBACK_POSITION_MS = "playback_start_position_ms"

class XMediaPlayService : MediaBrowserServiceCompat() {
    val TAG = javaClass.name

    lateinit var mediaSession: MediaSessionCompat
    lateinit var musicNotify: XMusicNotification
    protected lateinit var mediaSessionConnector: MediaSessionConnector
    lateinit var playerEventListener: PlayerEventListener
    private var isForegroundService = false


    private val musicPlayer: ExoPlayer by lazy {
        SimpleExoPlayer.Builder(this).build().apply {
            setAudioAttributes(uAmpAudioAttributes, true)
            playWhenReady = true

            setHandleAudioBecomingNoisy(true)

        }
    }

    private val uAmpAudioAttributes =
        AudioAttributes.Builder().setContentType(C.CONTENT_TYPE_MUSIC).setUsage(C.USAGE_MEDIA)
            .build()

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate()")
        initMediaSession()
        sessionToken = mediaSession.sessionToken
        musicNotify =
            XMusicNotification(this, mediaSession.sessionToken, PlayerNotificationListener())

        mediaSessionConnector = MediaSessionConnector(mediaSession).apply {
            setPlayer(musicPlayer)
            val playbackPrepare = MusicPlayBackPreparer(musicPlayer)
            setPlaybackPreparer(playbackPrepare)
            setQueueNavigator(MusicQueueNavigator(mediaSession, playbackPrepare))
        }

        playerEventListener = PlayerEventListener(musicNotify, musicPlayer, this)
        musicPlayer.addListener(playerEventListener)

        musicNotify.showNotificationForPlayer(musicPlayer)
    }

    fun initMediaSession() {
        val sessionActivityPendingIntent =
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                PendingIntent.getActivity(this, 0, sessionIntent, 0)
            }

        mediaSession = MediaSessionCompat(this, "XMediaPlayService").apply {
            setSessionActivity(sessionActivityPendingIntent) //val stateBuilder = PlaybackStateCompat.Builder()
            //    .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PLAY_PAUSE)

            //setPlaybackState(stateBuilder.build()) //setCallback(Callback)
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d(TAG, "onTaskRemoved()")

        musicPlayer.stop()
        musicPlayer.clearMediaItems()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy()")

        mediaSession.run {
            isActive = false
            release()
        }

        // Cancel coroutines when the service is going away.
        //serviceJob.cancel()

        // Free ExoPlayer resources.
        musicPlayer.removeListener(playerEventListener)
        musicPlayer.release()
    }


    override fun onGetRoot(
        clientPackageName: String, clientUid: Int, rootHints: Bundle?
    ): BrowserRoot {
        Log.d(TAG, "onGetRoot()")

        val rootExtras = Bundle().apply {
            putBoolean(MEDIA_SEARCH_SUPPORTED, true)
            putBoolean(CONTENT_STYLE_SUPPORTED, true)
            putInt(CONTENT_STYLE_BROWSABLE_HINT, CONTENT_STYLE_GRID)
            putInt(CONTENT_STYLE_PLAYABLE_HINT, CONTENT_STYLE_LIST)
        }
        val isRecentRequest = rootHints?.getBoolean(BrowserRoot.EXTRA_RECENT) ?: false
        val browserRootPath = if (isRecentRequest) UAMP_RECENT_ROOT else UAMP_BROWSABLE_ROOT
        return BrowserRoot(UAMP_BROWSABLE_ROOT, rootExtras)
    }

    override fun onLoadChildren(
        parentMediaId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        Log.d(TAG, "onLoadChildren()_" + parentMediaId)

        if (parentMediaId == UAMP_RECENT_ROOT) { //result.sendResult(storage.loadRecentSong()?.let { song -> listOf(song) })
        } else { // If the media source is ready, the results will be set synchronously here.
            //val resultsSent = mediaSource.whenReady { successfullyInitialized ->
            //    if (successfullyInitialized) {
            //        val children = browseTree[parentMediaId]?.map { item ->
            //            MediaBrowserCompat.MediaItem(item.description, item.flag)
            //        }
            //        result.sendResult(children)
            //    } else {
            //        mediaSession.sendSessionEvent(NETWORK_FAILURE, null)
            //        result.sendResult(null)
            //    }
            //}

            // If the results are not ready, the service must "detach" the results before
            // the method returns. After the source is ready, the lambda above will run,
            // and the caller will be notified that the results are ready.
            //
            // See [MediaItemFragmentViewModel.subscriptionCallback] for how this is passed to the
            // UI/displayed in the [RecyclerView].
            //if (!resultsSent) {
            //   result.detach()
            //}
        }
        result.sendResult(null)
    }

    override fun onSearch(
        query: String, extras: Bundle?, result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        super.onSearch(query, extras, result)
        Log.d(TAG, "onSearch()_$query")

        //result.detach()
    }


    inner class PlayerNotificationListener : PlayerNotificationManager.NotificationListener {
        override fun onNotificationPosted(
            notificationId: Int, notification: Notification, ongoing: Boolean
        ) {
            Log.d(TAG, "onNotificationPosted()_$ongoing")

            if (ongoing && !isForegroundService) {
                ContextCompat.startForegroundService(
                    applicationContext, Intent(applicationContext, this@XMediaPlayService.javaClass)
                )

                startForeground(notificationId, notification)
                isForegroundService = true
            }
        }

        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            Log.d(TAG, "onNotificationCancelled()")

            stopForeground(true)
            isForegroundService = false
            stopSelf()
        }
    }

}