package in.sammacwan.logger.core

import android.util.Log

internal object InternalLogger {
    private const val TAG = "LoggerOverlay"
    
    fun e(message: String, throwable: Throwable? = null) {
        Log.e(TAG, message, throwable)
    }
    
    fun w(message: String) {
        Log.w(TAG, message)
    }
    
    fun d(message: String) {
        Log.d(TAG, message)
    }
}
