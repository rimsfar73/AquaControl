package com.example.aquacontrol.iu.flushing

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun FlushingDetalleScreen(navController: NavController, lineaId: Int) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Detalle de flushing para línea $lineaId")
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = { navController.popBackStack() }) {
            Text("Volver")
        }
    }
}
