package com.igorunderplayer.untext

import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.igorunderplayer.untext.ui.theme.UnTextTheme
import java.io.FileInputStream
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var currentFileTitle by remember {
                mutableStateOf("")
            }

            var currentText by remember {
                mutableStateOf("")
            }

            val filePickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocument(),
                onResult = { uri ->
                    Log.d("Arquivo aberto", "URI: $uri")

                    if (uri == null) return@rememberLauncherForActivityResult

                    contentResolver.openFileDescriptor(uri, "r")?.use {
                        FileInputStream(it.fileDescriptor).use {
                            val text = it.reader().readText()
                            currentText = text
                            contentResolver.query(uri, null, null, null, null).use { cursor ->
                                if (cursor == null) {
                                    currentFileTitle = "MyFile"
                                    return@rememberLauncherForActivityResult
                                }
                                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                                cursor.moveToFirst()

                                currentFileTitle = cursor.getString(nameIndex).toString()
                            }
                        }
                    }
                }
            )
            val createFilelauncher = rememberLauncherForActivityResult(contract = CreateDocument("*/*"), onResult = { uri ->
                Log.d("Arquivo salvo", "URI: $uri")

                if (uri == null) return@rememberLauncherForActivityResult

                contentResolver.openFileDescriptor(uri,"wt")?.use {
                    FileOutputStream(it.fileDescriptor).use {
                        it.write(currentText.toByteArray())
                        Toast.makeText(applicationContext, "Arquivo salvo!", Toast.LENGTH_SHORT).show()
                    }
                }
            })

            fun openFile() {
                filePickerLauncher.launch(
                    arrayOf("*/*")
                )
            }

            fun saveFile() {
                createFilelauncher.launch(currentFileTitle)
            }

            UnTextTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        Card (
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            TextField(
                                textStyle = MaterialTheme.typography.titleLarge,
                                value = currentFileTitle,
                                onValueChange = { currentFileTitle = it })
                        }

                        Card (
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.9f)
                                .padding(8.dp)
                        ) {
                            Column (
                                modifier = Modifier.padding(8.dp)
                            ) {

                                LazyColumn (
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    item {
                                        LazyRow (
                                            modifier = Modifier.fillMaxSize()
                                        ){
                                            item {
                                                Column (modifier = Modifier.padding(vertical = 16.dp)) {
                                                    for (i in 1..currentText.lines().count()) {
                                                        Text(
                                                            modifier = Modifier.fillMaxHeight().padding(2.dp),
                                                            style = MaterialTheme.typography.labelMedium,
                                                            text = i.toString()
                                                        )
                                                    }
                                                }

                                            }
                                            item {
                                                TextField(
                                                    value = currentText,
                                                    onValueChange = { currentText = it },
                                                    singleLine = false,
                                                    modifier = Modifier.fillMaxSize(),
                                                    textStyle = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                        }
                                    }
                                }

                            }
                        }

                        Row {
                            Button (
                                modifier = Modifier
                                    .padding(8.dp),
                                onClick = { openFile() }
                            ) {
                                Text("Abrir")
                            }

                            Button (
                                modifier = Modifier
                                    .padding(8.dp),
                                onClick = { saveFile() }
                            ) {
                                Text("Salvar")
                            }
                        }

                    }
                }
            }
        }
    }
}
