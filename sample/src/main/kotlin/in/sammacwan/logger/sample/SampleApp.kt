package in.sammacwan.logger.sample

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import in.sammacwan.logger.LoggerOverlay
import in.sammacwan.logger.config.LoggerConfig
import in.sammacwan.logger.framework.HiltInjectionObserver
import in.sammacwan.logger.network.OkHttpLoggerInterceptor
import okhttp3.OkHttpClient

@HiltAndroidApp
class SampleApp : Application() {

    lateinit var httpClient: OkHttpClient
        private set

    override fun onCreate() {
        super.onCreate()

        // Initialise the library — debug builds only
        if (BuildConfig.DEBUG) {
            LoggerOverlay.init(
                context = this,
                config = LoggerConfig(
                    enabled = true,
                    persistLogs = false,
                    retentionDays = 0,
                    maxLogsInMemory = 500,
                    enableShakeToOpen = true,
                    logToConsole = true,
                ),
            )
        }

        // Optional: observe Hilt injections (requires @HiltAndroidApp)
        HiltInjectionObserver.init(this)

        // OkHttpClient wired with the network interceptor
        httpClient = OkHttpClient.Builder()
            .addInterceptor(OkHttpLoggerInterceptor())
            .build()

        LoggerOverlay.i("SampleApp", "Application started")
    }
}
