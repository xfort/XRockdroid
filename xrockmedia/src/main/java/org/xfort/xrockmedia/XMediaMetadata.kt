package org.xfort.xrockmedia

import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat

/**
 ** Created by ZhangHuaXin on 2021/7/21.
 **/
 val METADATA_KEY_MIME_TYPE: String
    get() = "org.xfort.xmedia.metadata.MIME_TYPE"

inline val MediaMetadataCompat.mediaID: String?
    get() = getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)

inline val MediaMetadataCompat.mediaUri: String?
    get() = getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)

inline val MediaMetadataCompat.duration: Long
    get() = getLong(MediaMetadataCompat.METADATA_KEY_DURATION)


inline val MediaMetadataCompat.title: String?
    get() = getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE)

inline val MediaMetadataCompat.subTitle: String?
    get() = getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE)

inline val MediaMetadataCompat.artist: String?
    get() = getString(MediaMetadataCompat.METADATA_KEY_ARTIST)


inline var MediaMetadataCompat.Builder.mediaID: String?
    get() = throw IllegalAccessException("cannot read builder")
    set(value) {
        putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, value)
    }

inline var MediaMetadataCompat.Builder.mediaUri: String?
    get() = throw IllegalAccessException("cannot read builder")
    set(value) {
        putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, value)
    }

inline var MediaMetadataCompat.Builder.duration: Long
    get() = throw IllegalAccessException("cannot read builder")
    set(value) {
        putLong(MediaMetadataCompat.METADATA_KEY_DURATION, value)
    }


inline var MediaMetadataCompat.Builder.title: String
    get() = throw IllegalAccessException("cannot read builder")
    set(value) {
        putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, value)
    }
inline var MediaMetadataCompat.Builder.subTitle: String
    get() = throw IllegalAccessException("cannot read builder")
    set(value) {
        putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, value)
    }

inline var MediaMetadataCompat.Builder.artist: String
    get() = throw IllegalAccessException("cannot read builder")
    set(value) {
        putString(MediaMetadataCompat.METADATA_KEY_ARTIST, value)
    }
inline var MediaMetadataCompat.Builder.extras: Bundle
    get() = throw IllegalAccessException("cannot read builder")
    set(value) {
        value.keySet().forEach { key ->
            val itemData = value[key]
            if (itemData is String) {
                putString(key, itemData)
            } else if (itemData is Long) {
                putLong(key, itemData)
            }
        }
    }