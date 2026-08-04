package com.screendimmer

import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.core.content.ContextCompat

class DimmerTileService : TileService() {

    private lateinit var prefs: DimmerPrefs

    override fun onCreate() {
        super.onCreate()
        prefs = DimmerPrefs(this)
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()

        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(MainActivity.EXTRA_REQUEST_PERMISSION, true)
            }
            startActivityAndCollapse(intent)
            return
        }

        if (prefs.isActive) {
            stopDimmer()
        } else {
            startDimmer()
        }

        updateTileState()
    }

    override fun onTileAdded() {
        super.onTileAdded()
        updateTileState()
    }

    private fun startDimmer() {
        val intent = Intent(this, DimmerService::class.java).apply {
            action = DimmerService.ACTION_SHOW
            putExtra(DimmerService.EXTRA_DIM_LEVEL, prefs.dimLevel)
        }
        ContextCompat.startForegroundService(this, intent)
    }

    private fun stopDimmer() {
        val intent = Intent(this, DimmerService::class.java).apply {
            action = DimmerService.ACTION_HIDE
        }
        startService(intent)
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        tile.state = if (prefs.isActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = getString(R.string.tile_label)
        tile.subtitle = if (prefs.isActive) {
            getString(R.string.tile_subtitle_active, prefs.dimLevel)
        } else {
            getString(R.string.tile_subtitle_inactive)
        }
        tile.updateTile()
    }
}
