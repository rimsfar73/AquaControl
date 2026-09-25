📘 README.md — AquaControl 2.0 (MVVM + Compose + Roles + Navigation)
markdown
# AquaControl — Android App (Jetpack Compose + MVVM + Roles)

AquaControl es una aplicación móvil desarrollada en Android Studio utilizando **Jetpack Compose**, **MVVM**, **StateFlow**, **Navigation Compose** y **Repository Pattern**.  
Este proyecto corresponde a la **Evaluación Aplicada 2 (EA2)** del módulo de Desarrollo de Aplicaciones Móviles del Instituto Profesional DUOC UC.

El sistema simula el flujo operativo del **Caso Galpones**, permitiendo navegar por:
- Selección de Rol (Operario / Supervisor)
- Granjas
- Galpones
- Líneas de bebederos
- Detalle de temperatura por línea

---

## 🚀 Tecnologías utilizadas

- Kotlin
- Jetpack Compose (Material 3)
- MVVM Architecture
- StateFlow
- Navigation Compose
- Repository Pattern
- Android Studio Iguana / Koala / Quail
- Gradle Kotlin DSL

---

## 📁 Estructura del proyecto (MVVM + Roles)

```text
app/src/main/java/com/example/aquacontrol/
│
├── MainActivity.kt
│
├── ui/                         ← Capa UI (todas las pantallas)
│   ├── roles/
│   │   └── RolSelectionScreen.kt
│   │
│   ├── operario/
│   │   └── OperarioScreen.kt
│   │
│   ├── supervisor/
│   │   └── SupervisorScreen.kt
│   │
│   ├── granjas/
│   │   └── GranjaScreen.kt
│   │
│   ├── galpones/
│   │   └── GalponScreen.kt
│   │
│   ├── lineas/
│   │   └── LineaScreen.kt
│   │
│   ├── detalle/
│   │   └── DetalleLineaScreen.kt
│   │
│   ├── components/
│   │   └── ReusableButton.kt
│   │
│   └── navigation/
│       ├── AppNavHost.kt
│       └── Routes.kt
│
├── viewmodel/                  ← Capa ViewModel (lógica + estado)
│   ├── perfil/
│   │   └── PerfilViewModel.kt
│   │
│   ├── granjas/
│   │   └── GranjaViewModel.kt
│   │
│   ├── galpones/
│   │   └── GalponViewModel.kt
│   │
│   ├── lineas/
│   │   └── LineaViewModel.kt
│   │
│   └── detalle/
│       └── DetalleLineaViewModel.kt
│
├── model/                      ← Capa Model (datos del dominio)
│   ├── perfil/
│   │   └── Perfil.kt
│   │
│   ├── granja/
│   │   └── Granja.kt
│   │
│   ├── galpon/
│   │   └── Galpon.kt
│   │
│   ├── linea/
│   │   ├── LineaBebedero.kt
│   │   └── EstadoLinea.kt
│   │
│   └── temperatura/
│       └── HistorialTemperatura.kt
│
└── repository/                 ← Capa Repository (origen de datos)
    └── bebedero/
        └── BebederoRepository.kt
```



---

## 🧭 Navegación (Roles + Caso Galpones)

La app utiliza **Navigation Compose** con un `NavHost` central.

### Rutas principales:

roles
operario
supervisor
granjas
galpones/{granjaId}
lineas/{galponId}
detalle/{lineaId}


### Flujo:

1. Selección de rol  
2. Pantalla de bienvenida según rol  
3. Granjas  
4. Galpones  
5. Líneas  
6. Detalle de línea  

Los **ViewModels no conocen rutas**, siguiendo la arquitectura MVVM correcta.

---

## 👤 Funcionalidades EA2

- Selección de rol (Operario / Supervisor)
- Pantallas de bienvenida por rol
- Navegación jerárquica del Caso Galpones
- Visualización de:
  - Granjas
  - Galpones
  - Líneas de bebederos
  - Estado térmico (Normal / Advertencia / Crítico)
- Repository con datos simulados
- ViewModels con StateFlow
- Componentes reutilizables
- Arquitectura MVVM completa
- Documentación técnica y UML

---

## 🛠 Instalación

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/tuusuario/AquaControl.git
Abrir el proyecto en Android Studio

Ejecutar en un emulador o dispositivo físico

👥 Autores
Rimsky Farías Soto — UI, Navegación, Arquitectura

Christian Quiroz Roa — Modelos, Repository, ViewModels

📄 Licencia
Este proyecto es de uso académico para la Evaluación Aplicada 2 (EA2) — DUOC UC.
