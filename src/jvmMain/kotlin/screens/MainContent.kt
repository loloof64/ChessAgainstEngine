package screens

import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import navigator.ChildStack
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.stackAnimation
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import logic.*

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun MainContent() {
    val navigation = remember { StackNavigation<Screen>() }
    val scaffoldState = rememberScaffoldState()

    ChildStack(
        source = navigation,
        initialStack = { listOf(Screen.Home) },
        animation = stackAnimation(fade() + scale()),
    ) { screen ->
        when (screen) {
            is Screen.Home -> HomePage(
                scaffoldState = scaffoldState,
                navigation = navigation,
            )

            is Screen.EditPosition -> EditPositionPage(
                navigation = navigation,
            )

            is Screen.Game -> {
                return@ChildStack GamePage(
                    navigation = navigation,
                )
            }

            is Screen.Options -> OptionsPage(onBack = navigation::pop)
        }
    }
}

sealed class Screen : Parcelable {

    @Parcelize
    object Home : Screen()

    @Parcelize
    data class Game(val startPosition: String = defaultPosition) : Screen()

    @Parcelize
    object EditPosition : Screen()

    @Parcelize
    object Options : Screen()
}