package com.example.aquacontrol.iu.granjas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.aquacontrol.iu.navigation.Routes
import com.example.aquacontrol.viewmodel.granjas.GranjaViewModel

@Composable
fun GranjaScreen(navController: NavController, viewModel: GranjaViewModel = viewModel()) {
    val granjas by viewModel.granjas.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Granjas")
        Spacer(modifier = Modifier.height(8.dp))

        granjas.forEach { granja ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        navController.navigate(Routes.GALPONES)
                    }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(granja.nombre)
                }
            }
        }
    }
}
