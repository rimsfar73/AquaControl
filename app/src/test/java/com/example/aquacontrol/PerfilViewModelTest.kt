package com.example.aquacontrol

import com.example.aquacontrol.model.perfil.PerfilUsuario
import com.example.aquacontrol.viewmodel.perfil.PerfilViewModel
import org.junit.Assert.*
import org.junit.Test

class PerfilViewModelTest {
    @Test
    fun seleccionYLimpiarPerfilActualizanSesionLocal() {
        val vm = PerfilViewModel()

        assertEquals("Camila", vm.nombreUsuario.value)
        assertNull(vm.perfilSeleccionado.value)

        vm.seleccionarPerfil(PerfilUsuario.OPERARIO)
        assertEquals(PerfilUsuario.OPERARIO, vm.perfilSeleccionado.value)

        vm.actualizarNombre("Rimsky")
        assertEquals("Rimsky", vm.nombreUsuario.value)

        vm.limpiarPerfil()
        assertNull(vm.perfilSeleccionado.value)
        assertEquals("", vm.nombreUsuario.value)
    }
}
