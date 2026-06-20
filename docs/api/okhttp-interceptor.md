# OkHttpLoggerInterceptor

`class OkHttpLoggerInterceptor : Interceptor` — automatically captures every HTTP request and response and forwards them to the overlay's **Network** tab.

> **OkHttp is a `compileOnly` dependency** — your app must provide it. The interceptor is a no-op if `LoggerOverlay` is disabled.

> Full KDoc: [sam829.github.io/simple-logger-overlay-android](https://sam829.github.io/simple-logger-overlay-android/)

---

## Setup

### OkHttpClient

```kotlin
import com.debugtools.logger.network.OkHttpLoggerInterceptor
import okhttp3.OkHttpClient

val client = OkHttpClient.Builder()
    .addInterceptor(OkHttpLoggerInterceptor())
    .build()
```

### Retrofit

```kotlin
val retrofit = Retrofit.Builder()
    .baseUrl("https://api.example.com/")
    .client(
        OkHttpClient.Builder()
            .addInterceptor(OkHttpLoggerInterceptor())
            .build()
    )
    .addConverterFactory(/* your converter */)
    .build()
```

### Hilt / DI module

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(OkHttpLoggerInterceptor())
            .build()
}
```

---

## What gets captured

| Field | Details |
|---|---|
| Method | `GET`, `POST`, `PUT`, `DELETE`, etc. |
| URL | Full request URL |
| Request headers | All headers as key-value pairs |
| Request body | Text body up to 1 MB |
| Status code | HTTP response code, or `-1` on network error |
| Response headers | All headers |
| Response body | Text body up to 1 MB (truncated with notice if larger) |
| Duration | Round-trip time in milliseconds |

---

## Memory safety

Response bodies larger than **1 MB** are not buffered — a truncation notice is logged instead. This prevents `OutOfMemoryError` on large binary or streaming responses.
