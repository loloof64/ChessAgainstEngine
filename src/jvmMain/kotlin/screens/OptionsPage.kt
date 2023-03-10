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
import kotlinx.coroutines.*
import logic.PreferencesManager
import java.awt.KeyboardFocusManager
import java.io.IOException
import java.io.PrintWriter
import java.util.*
import javax.swing.JFileChooser

@Composable
fun OptionsPage(
    onBack: () -> Unit,
) {
    val strings = LocalStrings.current
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()
    var enginePath by rememberSaveable { mutableStateOf(PreferencesManager.getEnginePath()) }
    var thinkingTime by rememberSaveable { mutableStateOf(PreferencesManager.getEngineThinkingTime()) }

    suspend fun testUCIEngine(enginePath: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val process =
                    ProcessBuilder(enginePath).start()
                val processIn = Scanner(process.inputStream)
                val outputTask = async {
                    while (isActive) {
                        try {
                            val currentLine = processIn.nextLine()
                            if (currentLine == "readyok") {
                                return@async true
                            }

                        } catch (ex: IllegalStateException) {
                            return@async false
                        } catch (ex: NoSuchElementException) {
                            return@async false
                        }
                    }
                    return@async false
                }
                val inputTask = launch {
                    val processOut = PrintWriter(process.outputStream)
                    processOut.write("uci\n")
                    processOut.flush()

                    processOut.write("isready\n")
                    processOut.flush()
                }
                inputTask.join()
                val deferredOutputTaskResult = async {
                    val loopResult = outputTask.await()
                    loopResult
                }
                launch {
                    delay(500)
                    process.destroy()
                }
                deferredOutputTaskResult.await()
            } catch (ex: IOException) {
                false
            }
        }
    }

    fun purposeSelectEnginePath() {
        val folder = PreferencesManager.loadEngineSelectionFolder()
        val fileChooser = if (folder.isNotEmpty()) JFileChooser(folder) else JFileChooser()
        fileChooser.dialogTitle = strings.selectEnginePathDialogTitle
        fileChooser.approveButtonText = strings.validate
        val currentWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().activeWindow
        val actionResult = fileChooser.showOpenDialog(currentWindow)
        if (actionResult == JFileChooser.APPROVE_OPTION) {
            val result = fileChooser.selectedFile

            coroutineScope.launch(Dispatchers.Default) {
                val isReallyEngine = testUCIEngine(result.absolutePath)
                if (isReallyEngine) {
                    PreferencesManager.saveEngineSelectionFolder(fileChooser.selectedFile.absolutePath)
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

    fun saveParameters() {
        PreferencesManager.saveEnginePath(enginePath)
        PreferencesManager.saveEngineThinkingTime(thinkingTime)
        coroutineScope.launch {
            scaffoldState.snackbarHostState.showSnackbar(
                strings.savedPreferences,
                duration = SnackbarDuration.Short
            )
            delay(300)
            onBack()
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
        Column(modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxHeight(0.4f),
                verticalArrangement = Arrangement.Top
            ) {
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
                    Button({ enginePath = "" }) {
                        Text(strings.clearEnginePath)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                )  {
                    val thinkingTimeInSeconds = String.format("%.1f", thinkingTime.toFloat() / 1000)
                    Text(strings.engineThinkingTime)
                    Slider(
                        modifier = Modifier.size(400.dp),
                        value = thinkingTime.toFloat(),
                        onValueChange = {thinkingTime = it.toInt()},
                        valueRange = 500f..10_000f,
                    )
                    Text("$thinkingTimeInSeconds s")
                }
            }

            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(::saveParameters) {
                    Text(strings.save)
                }
                Button(onBack) {
                    Text(strings.cancel)
                }
            }
        }
    }
}