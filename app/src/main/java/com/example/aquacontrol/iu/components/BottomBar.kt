package com.example.aquacontrol.iu.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.aquacontrol.iu.navigation.Routes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications

@Composable
fun BottomBar(navController: NavController) {
    NavigationBar {
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(Routes.ROLE_SELECTION) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
            label = { Text("Inicio") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(Routes.GRANJAS) },
            icon = { Icon(Icons.Default.Map, contentDescription = "Granjas") },
            label = { Text("Granjas") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(Routes.LINEAS) },
            icon = { Icon(Icons.Default.Notifications, contentDescription = "Alertas") },
            label = { Text("Alertas") }
        )
    }
}
