package com.example.aquacontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.aquacontrol.ui.theme.AquaControlTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                PantallaInicio()
            }
        }
    }
}

@Composable
fun PantallaInicio() {
    Text("Hola Compose mundo")
}

@Preview(showBackground = true)
@Composable
fun PreviewPantallaInicio() {
    AquaControlTheme {
        PantallaInicio()
    }
}
