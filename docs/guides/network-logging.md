# Network Logging

The library captures all OkHttp traffic automatically with zero extra code beyond adding the interceptor.

---

## Setup

Add `OkHttpLoggerInterceptor` to your `OkHttpClient`:

```kotlin
import com.debugtools.logger.network.OkHttpLoggerInterceptor

val client = OkHttpClient.Builder()
    .addInterceptor(OkHttpLoggerInterceptor())
    .build()
```

See the [full interceptor docs](../api/okhttp-interceptor.md) for Retrofit and Hilt setup.

---

## Viewing network logs

Open the overlay and tap the **Network** tab. Each row shows:

- HTTP method badge (`GET`, `POST`, …)
- URL
- Status code with colour (green = 2xx, red = error)
- Round-trip duration

Tap any row to see the full request and response detail, including headers and body.

---

## Filtering

In the Network tab you can filter by:

- **Method** — `GET`, `POST`, `PUT`, `DELETE`, etc.
- **Search** — type any text to match against the URL

---

## What is and isn't captured

| | Captured |
|---|---|
| Request URL, method | ✅ |
| Request headers | ✅ |
| Request body (text, ≤ 1 MB) | ✅ |
| Response status code | ✅ |
| Response headers | ✅ |
| Response body (text, ≤ 1 MB) | ✅ |
| Binary response bodies | ❌ Skipped (size guard) |
| Responses > 1 MB | ❌ Truncation notice logged |
| HTTPS certificate details | ❌ Not captured |

---

## Tips

- Add the interceptor with `addNetworkInterceptor()` instead of `addInterceptor()` if you need to see responses after cache (e.g. `304 Not Modified`).
- Chain multiple interceptors — the logger interceptor works alongside `HttpLoggingInterceptor`.
