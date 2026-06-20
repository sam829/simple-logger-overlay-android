# Export Logs

The library can export all current logs — both application logs and network logs — to a JSON file and return a shareable `Uri`.

---

## Export and share

```kotlin
import com.debugtools.logger.LoggerOverlay
import android.content.Intent

// Call from a coroutine scope
scope.launch {
    val uri = LoggerOverlay.exportLogs(context) ?: return@launch

    val share = Intent(Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    startActivity(Intent.createChooser(share, "Share logs"))
}
```

---

## Export file format

The exported JSON contains:

```json
{
  "timestamp": 1718880000000,
  "appVersion": "1.2.3",
  "device": {
    "manufacturer": "Google",
    "model": "Pixel 8",
    "sdk": 34
  },
  "logs": [
    {
      "timestamp": 1718880000000,
      "level": "DEBUG",
      "tag": "Auth",
      "message": "Login attempt",
      "data": "{\"email\":\"user@example.com\"}",
      "throwable": null
    }
  ],
  "networkLogs": [
    {
      "id": "uuid",
      "timestamp": 1718880000000,
      "method": "GET",
      "url": "https://api.example.com/user",
      "responseCode": 200,
      "duration": 142
    }
  ]
}
```

---

## FileProvider

The library uses its own `FileProvider` authority (`com.debugtools.logger.provider`) so it never conflicts with your app's existing `FileProvider` declaration.

The exported file is written to `context.cacheDir` and is automatically cleaned up by the OS when cache is cleared.

---

## Tips

- Export just before filing a bug report — include the JSON as an attachment.
- Use `LoggerOverlay.clearLogs()` at the start of a new flow so exports are focused:
  ```kotlin
  fun startCheckout() {
      LoggerOverlay.clearLogs()
      LoggerOverlay.i("Checkout", "Flow started")
  }
  ```
