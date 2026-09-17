package com.example.aquacontrol.iu.detalle

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun DetalleLineaScreen(galponId: Int, lineaId: Int) {
    Text("Detalle de la línea $lineaId del galpón $galponId")
}