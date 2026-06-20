package in.sammacwan.logger.framework

import android.app.Application
import in.sammacwan.logger.LoggerOverlay
import java.util.concurrent.ConcurrentHashMap

/**
 * Observes Hilt dependency injection events.
 * 
 * Usage: Call from Application.onCreate() after Hilt initialization:
 * ```kotlin
 * @HiltAndroidApp
 * class MyApp : Application() {
 *     override fun onCreate() {
 *         super.onCreate()  // Hilt initializes here
 *         HiltInjectionObserver.init(this)
 *     }
 * }
 * ```
 */
object HiltInjectionObserver {
    
    private val injectedComponents = ConcurrentHashMap<String, Long>()
    private var isInitialized = false
    
    /**
     * Initialize Hilt injection observer.
     * Must be called after super.onCreate() in HiltAndroidApp.
     */
    fun init(app: Application) {
        if (isInitialized) return
        isInitialized = true
        
        LoggerOverlay.i("Hilt", "🔌 Hilt injection observer initialized")
    }
    
    /**
     * Log a Hilt component injection.
     * Call from @Inject constructors or @AndroidEntryPoint onCreate.
     * 
     * @param componentName Fully qualified class name
     * @param dependencies List of injected dependency class names (optional)
     */
    fun logInjection(componentName: String, dependencies: List<String> = emptyList()) {
        val timestamp = System.currentTimeMillis()
        val previous = injectedComponents.put(componentName, timestamp)
        
        val message = if (dependencies.isNotEmpty()) {
            val depsStr = dependencies.joinToString(", ") { it.substringAfterLast('.') }
            "✅ Injected: ${componentName.substringAfterLast('.')} [deps: $depsStr]"
        } else {
            "✅ Injected: ${componentName.substringAfterLast('.')}"
        }
        
        if (previous != null) {
            // Re-injection (may indicate memory leak or singleton violation)
            LoggerOverlay.w("Hilt", message)
        } else {
            LoggerOverlay.d("Hilt", message)
        }
    }
    
    /**
     * Log when a Hilt module provides a dependency.
     * Call from @Provides methods.
     */
    fun logProvision(moduleName: String, providedType: String) {
        LoggerOverlay.d("Hilt", "📦 Provided: $providedType from ${moduleName.substringAfterLast('.')}")
    }
    
    /**
     * Get count of injected components.
     */
    fun getInjectionCount(): Int = injectedComponents.size
    
    /**
     * Get all injected component names.
     */
    fun getInjectedComponents(): Set<String> = injectedComponents.keys.toSet()
}
