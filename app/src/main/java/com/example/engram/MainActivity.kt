package com.example.engram

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.engram.ui.PresqueVuScreen
import com.example.engram.ui.theme.EngramTheme
import com.example.engram.viewmodel.PresqueVuViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EngramTheme {
                val viewModel: PresqueVuViewModel = viewModel()
                PresqueVuScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PresqueVuPreview() {
    EngramTheme {
        PresqueVuScreen(
            viewModel = PresqueVuViewModel(),
            modifier = Modifier.fillMaxSize()
        )
    }
}