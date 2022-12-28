import androidx.compose.material.MaterialTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import cafe.adriel.lyricist.ProvideStrings
import cafe.adriel.lyricist.rememberStrings
import i18n.LocalStrings
import i18n.strings

@Composable
@Preview
fun App() {
    val lyricist = rememberStrings(strings)
    ProvideStrings(lyricist, LocalStrings) {
        val strings = LocalStrings.current
        var text by remember { mutableStateOf(strings.clickMe) }
        MaterialTheme {
            Button(onClick = {
                text = strings.hello
            }) {
                Text(text)
            }
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
