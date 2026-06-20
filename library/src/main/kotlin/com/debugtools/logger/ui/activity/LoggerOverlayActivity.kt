package com.debugtools.logger.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.debugtools.logger.LoggerOverlay
import com.debugtools.logger.ui.screens.LoggerOverlayScreen
import com.debugtools.logger.ui.theme.LoggerTheme

class LoggerOverlayActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Mark activity as visible to prevent duplicate launches
        LoggerOverlay.setActivityVisible(true)
        
        setContent {
            LoggerTheme(seedColor = LoggerOverlay.getConfig().seedColor) {
                LoggerOverlayScreen(
                    onClose = { finish() }
                )
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Mark activity as no longer visible
        LoggerOverlay.setActivityVisible(false)
    }
}
