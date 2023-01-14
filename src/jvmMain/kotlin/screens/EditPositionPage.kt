package screens

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pop
import i18n.LocalStrings

@Composable
fun EditPositionPage(
    navigation: StackNavigation<Screen>
) {
    val strings = LocalStrings.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(strings.editPositionPageTitle)
                },
                navigationIcon = {
                    IconButton({ navigation.pop() }) {
                        Icon(Icons.Default.ArrowBack, strings.goBack)
                    }
                },
            )
        }
    ) {

    }
}