package com.example.aquacontrol.iu.galpones

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.aquacontrol.ui.navigation.Routes
import com.example.aquacontrol.viewmodel.galpones.GalponViewModel

@Composable
fun GalponScreen(
    navController: NavController,
    viewModel: GalponViewModel = viewModel()
) {

    // Cargar galpones de la granja seleccionada (por ahora fijo)
    LaunchedEffect(Unit) {
        viewModel.cargarGalpones(granjaId = 1)
    }

    // Observar lista de galpones
    val galpones = viewModel.galpones.collectAsState().value

    Column(modifier = Modifier.padding(16.dp)) {

        Text("Galpones")
        Spacer(modifier = Modifier.height(12.dp))

        galpones.forEach { galpon ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable {
                        navController.navigate(Routes.LINEAS)
                    }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Galpón: ${galpon.nombre}")
                    Text("ID: ${galpon.id}")
                }
            }
        }
    }
}
