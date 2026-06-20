package in.sammacwan.logger.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun LoggerTheme(
    seedColor: Color = Color(0xFF6200EE), // Material Purple - matches LoggerConfig default
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (useDarkTheme) {
        darkColorScheme(
            primary = seedColor,
            secondary = seedColor,
            tertiary = seedColor,
        )
    } else {
        lightColorScheme(
            primary = seedColor,
            secondary = seedColor,
            tertiary = seedColor,
        )
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
