package screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import i18n.LocalStrings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.KeyboardFocusManager
import javax.swing.JFileChooser

@Composable
fun OptionsPage(
    onBack: () -> Unit,
) {
    val strings = LocalStrings.current
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()
    var enginePath by rememberSaveable { mutableStateOf("") }

    suspend fun testUCIEngine(enginePath: String): Boolean {
        delay(200)
        return false
    }

    fun purposeSelectEnginePath() {
        val fileChooser = JFileChooser().apply {
            dialogTitle = strings.selectEnginePathDialogTitle
            approveButtonText = strings.validate
        }
        val currentWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().activeWindow
        val actionResult = fileChooser.showOpenDialog(currentWindow)
        if (actionResult == JFileChooser.APPROVE_OPTION) {
            val result = fileChooser.selectedFile

            coroutineScope.launch(Dispatchers.Default) {
                val isReallyEngine = testUCIEngine(result.absolutePath)
                with(Dispatchers.Main) {
                    if (isReallyEngine) {
                        enginePath = result.absolutePath
                    } else {
                        scaffoldState.snackbarHostState.showSnackbar(
                            strings.notChessUCIEngineError,
                            actionLabel = strings.close,
                            duration = SnackbarDuration.Long
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(title = { Text(strings.optionsPageTitle) }, navigationIcon = {
                IconButton(onBack) {
                    Icon(Icons.Default.ArrowBack, strings.goBack)
                }
            })
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(strings.enginePath)
                TextField(
                    modifier = Modifier.width(250.dp),
                    value = enginePath,
                    readOnly = true,
                    onValueChange = {})
                Button(::purposeSelectEnginePath) {
                    Text(strings.chooseUciEngine)
                }
            }
        }
    }
}