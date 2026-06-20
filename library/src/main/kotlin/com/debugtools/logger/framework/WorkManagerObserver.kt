package com.debugtools.logger.framework

import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import com.debugtools.logger.LoggerOverlay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Observes WorkManager task lifecycle events.
 * 
 * Usage: Call from Application.onCreate():
 * ```kotlin
 * class MyApp : Application() {
 *     override fun onCreate() {
 *         super.onCreate()
 *         WorkManagerObserver.init(this)
 *     }
 * }
 * ```
 */
object WorkManagerObserver {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isInitialized = false
    
    /**
     * Initialize WorkManager observer to watch all queued work.
     */
    fun init(context: Context) {
        if (isInitialized) return
        isInitialized = true
        
        val workManager = WorkManager.getInstance(context.applicationContext)
        
        scope.launch {
            workManager.getWorkInfosFlow(
                WorkQuery.fromStates(
                    WorkInfo.State.ENQUEUED,
                    WorkInfo.State.RUNNING,
                    WorkInfo.State.BLOCKED,
                    WorkInfo.State.SUCCEEDED,
                    WorkInfo.State.FAILED,
                    WorkInfo.State.CANCELLED
                )
            ).collectLatest { workInfoList: List<WorkInfo> ->
                workInfoList.forEach { workInfo: WorkInfo ->
                    logWorkState(
                        workId = workInfo.id.toString(),
                        workerName = workInfo.tags.firstOrNull { tag: String -> 
                            !tag.startsWith("androidx.work")
                        } ?: "Worker",
                        state = workInfo.state,
                        runAttemptCount = workInfo.runAttemptCount
                    )
                }
            }
        }
        
        LoggerOverlay.i("WorkManager", "⚙️ WorkManager observer initialized")
    }
    
    /**
     * Log when a worker is scheduled.
     * Call when enqueuing work.
     */
    fun logWorkScheduled(workerName: String, isPeriodic: Boolean = false) {
        val type = if (isPeriodic) "Periodic" else "OneTime"
        LoggerOverlay.d("WorkManager", "📅 Scheduled $type: $workerName")
    }
    
    /**
     * Log work state change.
     * Automatically called by observer, but can also be called manually.
     */
    fun logWorkState(
        workId: String,
        workerName: String,
        state: WorkInfo.State,
        runAttemptCount: Int = 0
    ) {
        val emoji = when (state) {
            WorkInfo.State.ENQUEUED -> "⏱️"
            WorkInfo.State.RUNNING -> "▶️"
            WorkInfo.State.SUCCEEDED -> "✅"
            WorkInfo.State.FAILED -> "❌"
            WorkInfo.State.BLOCKED -> "⏸️"
            WorkInfo.State.CANCELLED -> "🚫"
        }
        
        val message = "$emoji $workerName | ${state.name} | Attempt: $runAttemptCount"
        
        when (state) {
            WorkInfo.State.FAILED -> LoggerOverlay.e("WorkManager", message)
            WorkInfo.State.CANCELLED -> LoggerOverlay.w("WorkManager", message)
            WorkInfo.State.SUCCEEDED -> LoggerOverlay.i("WorkManager", message)
            else -> LoggerOverlay.d("WorkManager", message)
        }
    }
}
