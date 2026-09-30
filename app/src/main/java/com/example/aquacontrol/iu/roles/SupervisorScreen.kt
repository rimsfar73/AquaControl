package com.example.aquacontrol.iu.roles

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.aquacontrol.ui.navigation.Routes

@Composable
fun SupervisorScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Panel Supervisor")
        Spacer(modifier = Modifier.height(8.dp))
        Text("Historial, estados críticos y detalle de líneas.")

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate(Routes.GRANJAS) }) {
            Text("Ver granjas")
        }
    }
}
