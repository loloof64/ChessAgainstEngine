package screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import i18n.LocalStrings
import java.awt.KeyboardFocusManager
import javax.swing.JFileChooser

@Composable
fun OptionsPage(
    onBack: () -> Unit,
) {
    val strings = LocalStrings.current
    var enginePath by rememberSaveable{ mutableStateOf("") }

    fun purposeSelectEnginePath() {
        val fileChooser = JFileChooser().apply {
            dialogTitle = strings.selectEnginePathDialogTitle
            approveButtonText = strings.validate
        }
        val currentWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().activeWindow
        val actionResult = fileChooser.showOpenDialog(currentWindow)
        if (actionResult == JFileChooser.APPROVE_OPTION) {
            val result = fileChooser.selectedFile
            enginePath = result.absolutePath
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(strings.optionsPageTitle) }, navigationIcon = {
                IconButton(onBack) {
                    Icon(Icons.Default.ArrowBack, strings.goBack)
                }
            })
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically) {
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