package com.example.aquacontrol.iu.lineas

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

@Composable
fun LineaScreen(navController: NavHostController, galponId: Int) {
    Text("Líneas del galpón $galponId")
}