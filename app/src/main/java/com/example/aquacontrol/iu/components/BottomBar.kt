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

@Composable
fun BottomBar(navController: NavController) {

    val currentRoute = navController.currentBackStackEntry?.destination?.route

    NavigationBar {

        NavigationBarItem(
            selected = currentRoute == Routes.ROLE_SELECTION,
            onClick = { navController.navigate(Routes.ROLE_SELECTION) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
            label = { Text("Inicio") }
        )

        NavigationBarItem(
            selected = currentRoute == Routes.GRANJAS,
            onClick = { navController.navigate(Routes.GRANJAS) },
            icon = { Icon(Icons.Default.Map, contentDescription = "Granjas") },
            label = { Text("Granjas") }
        )
    }
}
