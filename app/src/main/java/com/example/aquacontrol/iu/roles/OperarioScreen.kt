package com.example.aquacontrol.iu.roles

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.aquacontrol.iu.navigation.Routes

@Composable
fun OperarioScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Panel Operario")
        Spacer(modifier = Modifier.height(8.dp))
        Text("Acceso rápido a granjas, galpones y líneas.")

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate(Routes.GRANJAS) }) {
            Text("Ver granjas")
        }
    }
}
