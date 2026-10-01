package com.example.aquacontrol.iu.roles

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aquacontrol.viewmodel.perfil.PerfilViewModel
import com.example.aquacontrol.iu.navigation.Routes

@Composable
fun OperarioScreen(
    navController: NavController,
    perfilViewModel: PerfilViewModel = viewModel()
) {
    val perfil = perfilViewModel.perfilSeleccionado.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Panel Operario")
        Spacer(modifier = Modifier.height(8.dp))
        Text("Rol seleccionado: $perfil")

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = { navController.navigate(Routes.GRANJAS) }) {
            Text("Ver granjas")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = { navController.popBackStack() }) {
            Text("Volver")
        }
    }
}
