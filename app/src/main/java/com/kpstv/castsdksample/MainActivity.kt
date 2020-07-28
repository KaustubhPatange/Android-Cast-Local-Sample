package com.kpstv.castsdksample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaTrack
import com.google.android.gms.cast.framework.*
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import com.google.android.gms.common.images.WebImage
import com.kpstv.castsdksample.server.WebService
import com.kpstv.castsdksample.utils.Utils
import io.github.dkbai.tinyhttpd.nanohttpd.webserver.SimpleWebServer
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object {
        private const val PERMISSION_REQUEST_CODE = 112
        var deviceIpAddress: String? = null
    }

    lateinit var mCastContext: CastContext
    lateinit var mediaRouteMenuItem: MenuItem

    private var mCastSession: CastSession? = null
    lateinit var mSessionManagerListener: SessionManagerListener<CastSession>

    private var mIntroductoryOverlay: IntroductoryOverlay? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /** Required to initialize the server */
        SimpleWebServer.init(this, BuildConfig.DEBUG)

        /** Set up cast listener as suggested in official documentation */
        setUpCastListener()

        /** Set up cast context as suggested in official documentation */
        mCastContext = CastContext.getSharedInstance(this)

        /** Set a cast state listener */
        mCastContext.addCastStateListener { state ->
            /** Show an introductory overlay to notify user that
             *  there is a cast device available to connect.
             */
            if (state != CastState.NO_DEVICES_AVAILABLE)
                showIntroductoryOverlay()
        }

        /** Set session manager listener, this listener consist various methods
         *  which will be invoked when something changes in cast like
         *  Start, Resume, End, etc listener.
         */
        mCastContext.sessionManager.addSessionManagerListener(
            mSessionManagerListener, CastSession::class.java
        )
    }

    /**
     * OnClick listener for the Play button in activity_main.xml file.
     */
    fun playVideo(view: View? = null) {

        /** In order to start and display files on http server we
         *  require EXTERNAL_STORAGE permission. */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
            return
        }

        /** Find the IpAddress of the device and save it to [deviceIpAddress]
         *  so that Service class can pick it up to create a small http server */
        deviceIpAddress = Utils.findIPAddress(applicationContext)
        if (deviceIpAddress == null) {
            Toast.makeText(
                applicationContext,
                "Connect to a wifi device or hotspot",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        /** Start a http server. */
        startService(Intent(this, WebService::class.java))

        /** Play the file on the device. */
        val remoteMediaClient = mCastSession?.remoteMediaClient ?: return
        remoteMediaClient.registerCallback(object : RemoteMediaClient.Callback() {
            override fun onStatusUpdated() {
                /** When media loaded we will start the fullscreen player activity. */
                val intent =
                    Intent(this@MainActivity, ExpandedControlsActivity::class.java)
                startActivity(intent)
                remoteMediaClient.unregisterCallback(this)
            }
        })
        remoteMediaClient.load(
            MediaLoadRequestData.Builder()
                .setMediaInfo(buildMediaInfo())
                /** Use the [MediaInfo] generated from [buildMediaInfo]. */
                .setAutoplay(true)
                .setCurrentTime(0.toLong())
                .build()
        )
    }

    /** OnClick listener for Stop button in activity_main.xml */
    fun stopVideo(view: View) {
        mCastSession?.remoteMediaClient?.stop()
        SimpleWebServer.stopServer()
    }

    /**
     * This method will return a [MediaInfo] which contains information
     * about the media file and media subtitle which will be used for
     * playing in the cast device.
     */
    private fun buildMediaInfo(): MediaInfo? {

        /** Here we are setting the web server url for our
         *  media files.
         */
        val sampleVideoStream =
            "http://${deviceIpAddress}:8080/${edt_video.text}"
        val sampleVideoSubtitle =
            "http://${deviceIpAddress}:8080/${edt_subtitle.text}"

        val testImageUrl1 =
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/images/480x270/DesigningForGoogleCast2-480x270.jpg"
        val testImageUrl2 =
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/images/780x1200/DesigningForGoogleCast-887x1200.jpg"

        val movieMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE)
        movieMetadata.putString(MediaMetadata.KEY_TITLE, "Google Cast SDK") // Set title for video
        movieMetadata.putString(
            MediaMetadata.KEY_SUBTITLE,
            "Google developers"
        ) // Set sub-title for video
        movieMetadata.addImage(WebImage(Uri.parse(testImageUrl1))) // Required first image (low-res)
        movieMetadata.addImage(WebImage(Uri.parse(testImageUrl2))) // Required second image (high-res)

        /** (Optional) Setting a subtitle track, You can add more subtitle
         *  track by using this builder. */
        val mediaTrack = MediaTrack.Builder(1, MediaTrack.TYPE_TEXT)
            .setName("English")
            .setSubtype(MediaTrack.SUBTYPE_SUBTITLES)
            .setContentId(sampleVideoSubtitle)
            .setLanguage("en-US")
            .build()

        return MediaInfo.Builder(sampleVideoStream)
            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
            .setContentType("videos/mp4")
            .setMetadata(movieMetadata)
            .setStreamDuration(333 * 1000) // 5:33 means 333 seconds
            .setMediaTracks(listOf(mediaTrack)) // (Optional) Set list of subtitles.
            .build()
    }

    private fun setUpCastListener() {
        mSessionManagerListener = object : SessionManagerListener<CastSession> {
            override fun onSessionStarted(session: CastSession?, p1: String?) {
                onApplicationConnected(session)
            }

            override fun onSessionResumeFailed(p0: CastSession?, p1: Int) {
                onApplicationDisconnected()
            }

            override fun onSessionEnded(p0: CastSession?, p1: Int) {
                onApplicationDisconnected()
            }

            override fun onSessionResumed(session: CastSession?, p1: Boolean) {
                onApplicationConnected(session)
            }

            override fun onSessionStartFailed(p0: CastSession?, p1: Int) {
                onApplicationDisconnected()
            }

            override fun onSessionSuspended(p0: CastSession?, p1: Int) {}

            override fun onSessionStarting(p0: CastSession?) {}

            override fun onSessionResuming(p0: CastSession?, p1: String?) {}

            override fun onSessionEnding(p0: CastSession?) {}

            private fun onApplicationConnected(castSession: CastSession?) {
                mCastSession = castSession
                invalidateOptionsMenu() // This is required to refresh the activity toolbar to display cast connect button.
            }

            private fun onApplicationDisconnected() {
                invalidateOptionsMenu() // This is required to refresh the toolbar after disconnect.
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.browse, menu)

        /** Set up the cast button to automatically respond to cast UI */
        mediaRouteMenuItem = CastButtonFactory.setUpMediaRouteButton(
            applicationContext,
            menu,
            R.id.media_route_menu_item
        )
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.isNotEmpty())
            playVideo(null)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onDestroy() {
        /** Stop http server in any case user directly close the app from recents. */
        SimpleWebServer.stopServer()
        super.onDestroy()
    }

    /** This function will show an overlay to make user aware that there are
     *  devices available to connect using cast.
     *
     *  @see <a href="https://developers.google.com/cast/docs/android_sender/customize_ui#customize-theme-introductory-overlay">Customize this UI</a>
     */
    private fun showIntroductoryOverlay() {
        mIntroductoryOverlay?.remove()

        if (::mediaRouteMenuItem.isInitialized && mediaRouteMenuItem.isVisible) {
            Handler().post {
                mIntroductoryOverlay = IntroductoryOverlay.Builder(
                    this, mediaRouteMenuItem
                )
                    .setTitleText("Cast media to device")
                    .setSingleTime()
                    .setOnOverlayDismissedListener { mIntroductoryOverlay = null }
                    .build()
                mIntroductoryOverlay?.show()
            }
        }
    }
}