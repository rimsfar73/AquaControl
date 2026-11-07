package com.example.aquacontrol.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.aquacontrol.model.perfil.PerfilUsuario

@Composable
fun LoginScreen(onLogin: (String, PerfilUsuario) -> Unit) {
    var usuario by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("AquaControl · Acceso experimental", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        Text("Demostración local: no protege la API ni verifica identidades reales.")
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(value = usuario, onValueChange = { usuario = it; error = false },
            label = { Text("Usuario") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = clave, onValueChange = { clave = it; error = false },
            label = { Text("Contraseña") }, singleLine = true,
            visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        if (error) Text("Credenciales incorrectas", color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(20.dp))
        Button(onClick = {
            val cuenta = DemoAuthRepository.ingresar(usuario, clave)
            if (cuenta == null) error = true else onLogin(cuenta.nombre, cuenta.rol)
        }, modifier = Modifier.fillMaxWidth()) { Text("Ingresar") }
        Spacer(Modifier.height(20.dp))
        Text("Prueba: operario / demo1234")
        Text("Prueba: supervisor / demo5678")
    }
}
