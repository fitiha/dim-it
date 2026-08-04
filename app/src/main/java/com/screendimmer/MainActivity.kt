package com.screendimmer

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: DimmerPrefs
    private lateinit var seekBar: SeekBar
    private lateinit var levelText: TextView
    private lateinit var toggleButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = DimmerPrefs(this)
        seekBar = findViewById(R.id.seekBar)
        levelText = findViewById(R.id.levelText)
        toggleButton = findViewById(R.id.toggleButton)

        seekBar.max = 100
        seekBar.progress = prefs.dimLevel
        updateLevelText(prefs.dimLevel)
        updateToggleButton()

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateLevelText(progress)
                if (prefs.isActive) {
                    updateDimmer(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                prefs.dimLevel = seekBar?.progress ?: prefs.dimLevel
            }
        })

        toggleButton.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                requestOverlayPermission()
                return@setOnClickListener
            }
            toggleDimmer()
        }

        if (intent.getBooleanExtra(EXTRA_REQUEST_PERMISSION, false)) {
            if (!Settings.canDrawOverlays(this)) {
                requestOverlayPermission()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateToggleButton()
    }

    private fun toggleDimmer() {
        if (prefs.isActive) {
            stopDimmer()
        } else {
            startDimmer()
        }
        updateToggleButton()
    }

    private fun startDimmer() {
        val intent = Intent(this, DimmerService::class.java).apply {
            action = DimmerService.ACTION_SHOW
            putExtra(DimmerService.EXTRA_DIM_LEVEL, prefs.dimLevel)
        }
        ContextCompat.startForegroundService(this, intent)
        Toast.makeText(this, R.string.toast_dimmer_on, Toast.LENGTH_SHORT).show()
    }

    private fun stopDimmer() {
        val intent = Intent(this, DimmerService::class.java).apply {
            action = DimmerService.ACTION_HIDE
        }
        startService(intent)
        Toast.makeText(this, R.string.toast_dimmer_off, Toast.LENGTH_SHORT).show()
    }

    private fun updateDimmer(level: Int) {
        val intent = Intent(this, DimmerService::class.java).apply {
            action = DimmerService.ACTION_UPDATE
            putExtra(DimmerService.EXTRA_DIM_LEVEL, level)
        }
        startService(intent)
    }

    private fun updateLevelText(level: Int) {
        levelText.text = getString(R.string.level_text, level)
    }

    private fun updateToggleButton() {
        toggleButton.text = if (prefs.isActive) {
            getString(R.string.button_stop)
        } else {
            getString(R.string.button_start)
        }
    }

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            android.net.Uri.parse("package:$packageName")
        )
        startActivity(intent)
        Toast.makeText(this, R.string.toast_permission_needed, Toast.LENGTH_LONG).show()
    }

    companion object {
        const val EXTRA_REQUEST_PERMISSION = "request_permission"
    }
}
