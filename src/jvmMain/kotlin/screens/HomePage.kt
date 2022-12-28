package screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import i18n.LocalStrings

@Composable
fun HomePage(
    onGoGamePageClick: () -> Unit,
) {
    val strings = LocalStrings.current
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(strings.homePageTitle) }
        )
    }) {
        Column(modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(onGoGamePageClick) {
                Text(strings.goToGamePage)
            }
        }
    }
}