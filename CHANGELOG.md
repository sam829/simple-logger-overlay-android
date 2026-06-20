# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.0] — 2026-06-20

### Added
- `LoggerOverlay` — main entry point with `v/d/i/w/e/logJson/telemetry` logging API
- `LoggerConfig` — configuration with enabled, storage, retention, shake-to-open, and seed colour options
- `OkHttpLoggerInterceptor` — automatic HTTP request/response capture (compileOnly OkHttp)
- `SessionTelemetry` — timed session tracking with milestones and arbitrary metadata
- `HiltInjectionObserver` — optional Hilt DI event logging (compileOnly Hilt)
- `WorkManagerObserver` — optional WorkManager lifecycle logging (compileOnly WorkManager)
- In-memory and Room-backed `LogStorage` implementations
- Draggable FAB overlay UI — 100% Jetpack Compose, Material 3
- **Logs** tab — filterable by level, searchable by tag/message
- **Network** tab — filterable by method, searchable by URL
- Log detail screen with JSON pretty-printing and stack trace display
- Export logs to JSON via Android share sheet
- Shake-to-open via accelerometer (`ShakeDetector`)
- KDoc API reference published to GitHub Pages on each release
- CI workflow (build + lint + unit tests on all PRs)
- Auto-release workflow (patch version bump + tag + Maven Central publish on merge to main)
