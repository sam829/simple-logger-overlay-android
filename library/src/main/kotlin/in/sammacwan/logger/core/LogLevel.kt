package in.sammacwan.logger.core

/**
 * Severity levels for log entries, ordered from least to most severe.
 *
 * Use these to categorise log output and to filter the overlay's log list.
 */
enum class LogLevel {
    /** Granular detail — noise you only need when hunting a specific bug. */
    VERBOSE,

    /** General debugging information useful during development. */
    DEBUG,

    /** Important events and milestones (user actions, navigation, API success). */
    INFO,

    /** Potentially problematic situations that don't stop the app. */
    WARN,

    /** Error events that may impair functionality but allow the app to continue. */
    ERROR,
}
