package com.example.aquacontrol.iu.roles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.aquacontrol.viewmodel.perfil.PerfilViewModel
import com.example.aquacontrol.iu.navigation.Routes
import com.example.aquacontrol.ui.theme.AzulAriztia
import com.example.aquacontrol.ui.theme.GrisClaro
import com.example.aquacontrol.ui.theme.GrisTexto
import com.example.aquacontrol.ui.theme.RojoAriztia
import com.example.aquacontrol.ui.theme.AmarilloAdvertencia

@Composable
fun OperarioScreen(
    navController: NavController,
    perfilViewModel: PerfilViewModel
) {
    val nombre = perfilViewModel.nombreUsuario.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrisClaro)
            .padding(16.dp)
    ) {
        Text("Panel Operario", color = AzulAriztia)
        Text("Hola, $nombre", color = GrisTexto)

        Text(
            "Tu jornada comienza con el control de las líneas de agua asignadas.",
            color = GrisTexto
        )
        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Estado de la operación", color = GrisTexto)
                Text("2 acciones pendientes en Granja Santa Elena.", color = GrisTexto)
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate(Routes.GRANJAS) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AzulAriztia)
        ) {
            Text("Ver Granjas", color = Color.White)
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = { /* Navegar a alertas cuando exista */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AmarilloAdvertencia)
        ) {
            Text("Alertas", color = Color.Black)
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = { navController.navigate(Routes.FLUSHING) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = RojoAriztia)
        ) {
            Text("Registrar Flushing", color = Color.White)
        }

        Spacer(Modifier.height(24.dp))

        // Botón Volver
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = GrisTexto)
        ) {
            Text("Volver", color = Color.White)
        }
    }
}
