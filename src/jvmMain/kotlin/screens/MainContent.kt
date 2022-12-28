package screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.arkivanov.composenavigatorexample.navigator.ChildStack
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.stackAnimation
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize

const val defaultStartPosition = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

@Composable
fun MainContent() {
    val navigation = remember { StackNavigation<Screen>() }
    ChildStack(
        source = navigation,
        initialStack = { listOf(Screen.Home) },
        animation = stackAnimation(fade() + scale()),
    ) { screen ->
        when (screen) {
            is Screen.Home -> HomePage(onGoGamePageClick = { navigation.push(Screen.Game()) })
            is Screen.Game -> GamePage(onBack = navigation::pop)
        }
    }
}

sealed class Screen : Parcelable {

    @Parcelize
    object Home : Screen()

    @Parcelize
    data class Game(val startPosition: String = defaultStartPosition) : Screen()
}