import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.defaultScrollbarStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import cafe.adriel.lyricist.ProvideStrings
import cafe.adriel.lyricist.rememberStrings
import navigator.ProvideComponentContext
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import i18n.LocalStrings
import i18n.strings
import kotlinx.coroutines.launch
import logic.UciEngineChannel
import screens.MainContent
import java.awt.Dimension

@Composable
@Preview
fun App() {
    val lifecycle = LifecycleRegistry()
    val rootComponentContext = DefaultComponentContext(lifecycle = lifecycle)
    val lyricist = rememberStrings(strings)
    ProvideStrings(lyricist, LocalStrings) {
        MaterialTheme {
            CompositionLocalProvider(LocalScrollbarStyle provides defaultScrollbarStyle()) {
                ProvideComponentContext(rootComponentContext) {
                    MainContent()
                }
            }
        }
    }
}

fun main() = application {
    val coroutineScope = rememberCoroutineScope()
    Window(onCloseRequest = {
        coroutineScope.launch {
            UciEngineChannel.stopProcess()
            exitApplication()
        }
    }) {
        window.minimumSize = Dimension(650, 500)
        App()
    }
}
