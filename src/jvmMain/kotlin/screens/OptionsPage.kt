package screens

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import i18n.LocalStrings

@Composable
fun OptionsPage(
    onBack: () -> Unit,
) {
    val strings = LocalStrings.current

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(strings.optionsPageTitle) }, navigationIcon = {
                IconButton(onBack) {
                    Icon(Icons.Default.ArrowBack, strings.goBack)
                }
            })
        }
    ) {

    }
}