package com.kpstv.castsdksample.provider

import android.content.Context
import com.google.android.gms.cast.CastMediaControlIntent
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import com.google.android.gms.cast.framework.media.CastMediaOptions
import com.google.android.gms.cast.framework.media.NotificationOptions
import com.kpstv.castsdksample.ExpandedControlsActivity

@Suppress("UNUSED")
class CastOptionsProvider : OptionsProvider {

    /** Sample uses default receiver application Id.
     *
     *  If you want to customize receiver device you need to register at Developer Console and
     *  replace the following [CastOptions.Builder.setReceiverApplicationId] with yours.
     *
     *  @see <a href="http://cast.google.com/publish">Cast Developers Console</a> to register for custom receiver Id.
     */
    override fun getCastOptions(context: Context?): CastOptions {
        /**
         * This will also show a notification in device.
         */
        val notificationOptions = NotificationOptions.Builder()
            .setTargetActivityClassName(ExpandedControlsActivity::class.java.name)
            .build()

        val mediaOptions = CastMediaOptions.Builder()
            .setNotificationOptions(notificationOptions)
            .setExpandedControllerActivityClassName(ExpandedControlsActivity::class.java.name)
            .build()

        return CastOptions.Builder()
            .setReceiverApplicationId(CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID)
            .setCastMediaOptions(mediaOptions)
            .build()
    }

    override fun getAdditionalSessionProviders(p0: Context?): MutableList<SessionProvider>? {
        return null
    }
}