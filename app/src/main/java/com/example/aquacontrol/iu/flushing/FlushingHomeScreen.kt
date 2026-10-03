package com.example.aquacontrol.iu.flushing

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.aquacontrol.iu.navigation.Routes

@Composable
fun FlushingHomeScreen(navController: NavController) {

    Column {
        Text("Pantalla de Flushing (general)")

        Button(onClick = {
            navController.navigate(Routes.REGISTRAR_FLUSHING)
        }) {
            Text("Registrar Flushing")
        }

        Button(onClick = {
            navController.popBackStack()
        }) {
            Text("Volver")
        }
    }
}
