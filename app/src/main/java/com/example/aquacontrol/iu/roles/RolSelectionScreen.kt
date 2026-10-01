package com.example.aquacontrol.iu.roles

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aquacontrol.model.perfil.PerfilUsuario
import com.example.aquacontrol.viewmodel.perfil.PerfilViewModel
import com.example.aquacontrol.iu.navigation.Routes

@Composable
fun RolSelectionScreen(
    navController: NavController,
    perfilViewModel: PerfilViewModel = viewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Selecciona tu rol")
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            onClick = {
                perfilViewModel.seleccionarPerfil(PerfilUsuario.OPERARIO)
                navController.navigate(Routes.OPERARIO_HOME)
            }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Operario")
                Text("Acceso a panel de operación")
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            onClick = {
                perfilViewModel.seleccionarPerfil(PerfilUsuario.SUPERVISOR)
                navController.navigate(Routes.SUPERVISOR_HOME)
            }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Supervisor")
                Text("Acceso a panel de supervisión")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = { navController.popBackStack() }) {
            Text("Volver")
        }
    }
}
