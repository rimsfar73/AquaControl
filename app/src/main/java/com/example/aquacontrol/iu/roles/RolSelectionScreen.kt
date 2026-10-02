package com.example.aquacontrol.iu.roles

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.aquacontrol.model.perfil.PerfilUsuario
import com.example.aquacontrol.viewmodel.perfil.PerfilViewModel
import com.example.aquacontrol.iu.navigation.Routes
import com.example.aquacontrol.ui.theme.AzulAriztia
import com.example.aquacontrol.ui.theme.GrisClaro
import com.example.aquacontrol.ui.theme.GrisTexto
import com.example.aquacontrol.ui.theme.RojoAriztia

@Composable
fun RolSelectionScreen(
    navController: NavController,
    perfilViewModel: PerfilViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrisClaro)
            .padding(16.dp)
    ) {
        Text("Ariztía", color = RojoAriztia)
        Text("AQUACONTROL 2.0", color = AzulAriztia)
        Spacer(Modifier.height(16.dp))

        Text("Seleccione su Rol", color = GrisTexto)
        Text(
            "Personaliza la vista de monitoreo según tus responsabilidades en terreno.",
            color = GrisTexto
        )
        Spacer(Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clickable {
                    perfilViewModel.seleccionarPerfil(PerfilUsuario.OPERARIO)
                    navController.navigate(Routes.OPERARIO_HOME)
                },
            colors = CardDefaults.cardColors(containerColor = AzulAriztia)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Operario", color = Color.White)
                Text("Control diario y flushing", color = Color.White)
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clickable {
                    perfilViewModel.seleccionarPerfil(PerfilUsuario.SUPERVISOR)
                    navController.navigate(Routes.SUPERVISOR_HOME)
                },
            colors = CardDefaults.cardColors(containerColor = RojoAriztia)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Supervisor", color = Color.White)
                Text("Análisis, historial y alertas", color = Color.White)
            }
        }
    }
}
