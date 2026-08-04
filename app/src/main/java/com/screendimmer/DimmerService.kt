package com.screendimmer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager

class DimmerService : Service() {

    private var windowManager: WindowManager? = null
    private var overlay: DimmerOverlay? = null
    private lateinit var prefs: DimmerPrefs

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        prefs = DimmerPrefs(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW -> {
                val level = intent.getIntExtra(EXTRA_DIM_LEVEL, prefs.dimLevel)
                showOverlay(level)
                startForeground(NOTIFICATION_ID, createNotification(level))
            }
            ACTION_HIDE -> {
                hideOverlay()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_UPDATE -> {
                val level = intent.getIntExtra(EXTRA_DIM_LEVEL, prefs.dimLevel)
                updateDimLevel(level)
            }
            ACTION_DIM_MINUS -> {
                val newLevel = (prefs.dimLevel - 10).coerceIn(0, 100)
                updateDimLevel(newLevel)
            }
            ACTION_DIM_PLUS -> {
                val newLevel = (prefs.dimLevel + 10).coerceIn(0, 100)
                updateDimLevel(newLevel)
            }
        }
        return START_NOT_STICKY
    }

    private fun showOverlay(level: Int) {
        if (overlay != null) {
            updateDimLevel(level)
            return
        }

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 0
        }

        overlay = DimmerOverlay(this).apply {
            setDimLevel(level)
        }

        windowManager?.addView(overlay, params)

        overlay?.post {
            overlay?.setSystemBars(getStatusBarHeight(), getNavBarHeight())
        }

        prefs.isActive = true
    }

    private fun hideOverlay() {
        overlay?.let {
            windowManager?.removeView(it)
            overlay = null
        }
        prefs.isActive = false
    }

    private fun updateDimLevel(level: Int) {
        overlay?.setDimLevel(level)
        prefs.dimLevel = level
        updateNotification(level)
    }

    private fun getStatusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    }

    private fun getNavBarHeight(): Int {
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_channel_description)
            setShowBadge(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(level: Int): Notification {
        val minusIntent = Intent(this, DimmerService::class.java).apply {
            action = ACTION_DIM_MINUS
        }
        val minusPending = PendingIntent.getService(
            this, 0, minusIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val plusIntent = Intent(this, DimmerService::class.java).apply {
            action = ACTION_DIM_PLUS
        }
        val plusPending = PendingIntent.getService(
            this, 1, plusIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openAppIntent = Intent(this, MainActivity::class.java)
        val openAppPending = PendingIntent.getActivity(
            this, 2, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text, level))
            .setSmallIcon(R.drawable.ic_qs_dimmer)
            .setOngoing(true)
            .setContentIntent(openAppPending)
            .addAction(
                Notification.Action.Builder(
                    null,
                    getString(R.string.action_dim_minus),
                    minusPending
                ).build()
            )
            .addAction(
                Notification.Action.Builder(
                    null,
                    getString(R.string.action_dim_plus),
                    plusPending
                ).build()
            )
            .build()
    }

    private fun updateNotification(level: Int) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, createNotification(level))
    }

    override fun onDestroy() {
        hideOverlay()
        super.onDestroy()
    }

    companion object {
        const val ACTION_SHOW = "com.screendimmer.SHOW"
        const val ACTION_HIDE = "com.screendimmer.HIDE"
        const val ACTION_UPDATE = "com.screendimmer.UPDATE"
        const val ACTION_DIM_MINUS = "com.screendimmer.DIM_MINUS"
        const val ACTION_DIM_PLUS = "com.screendimmer.DIM_PLUS"
        const val EXTRA_DIM_LEVEL = "dim_level"
        private const val CHANNEL_ID = "dimmer_channel"
        private const val NOTIFICATION_ID = 1
    }
}
