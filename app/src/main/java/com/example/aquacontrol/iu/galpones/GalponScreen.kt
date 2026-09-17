package com.example.aquacontrol.iu.galpones

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

@Composable
fun GalponScreen(navController: NavHostController, granjaId: Int) {
    Text("Galpones de la granja $granjaId")
}
