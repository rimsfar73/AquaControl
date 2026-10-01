# AquaControl 3.0 — Android App

Aplicación móvil desarrollada en **Android Studio** utilizando **Kotlin, Jetpack Compose y arquitectura MVVM**.

Este proyecto corresponde a la **Evaluación Aplicada 2 (EA2)** del módulo de Desarrollo de Aplicaciones Móviles de **DUOC UC**.

La versión **3.0** incorpora mejoras en arquitectura, navegación, documentación y estructura del proyecto, además de un nuevo módulo técnico de **Flushing**, que simula el proceso de limpieza térmica de las líneas de bebederos.

---

## 📱 Funcionalidades

* Selección de rol:

  * Operario
  * Supervisor
* Pantallas de bienvenida según el rol seleccionado.
* Navegación jerárquica:

  * Granjas
  * Galpones
  * Líneas de bebederos
  * Detalle de línea
* Visualización del estado térmico:

  * Normal
  * Advertencia
  * Crítico
* Módulo de **Flushing** para simular la limpieza térmica de líneas.
* Repository con datos simulados.
* ViewModels utilizando `StateFlow`.
* Componentes UI reutilizables.
* Navegación mediante **Navigation Compose**.
* Arquitectura **MVVM**.
* Documentación técnica y UML actualizados a la versión 3.0.

---

## 🎨 Prototipo UI

Prototipo desarrollado en Canva:

**[Ver prototipo AquaControl 3.0](https://vistausariofullstack.my.canva.site/aquacontrol-2-0-x-arizt-a)**

---

## 🚀 Tecnologías utilizadas

| Tecnología             | Uso                           |
| ---------------------- | ----------------------------- |
| **Kotlin**             | Lenguaje principal            |
| **Jetpack Compose**    | Desarrollo de interfaz        |
| **Material 3**         | Componentes y diseño UI       |
| **MVVM**               | Arquitectura de la aplicación |
| **StateFlow**          | Gestión de estados            |
| **Navigation Compose** | Navegación entre pantallas    |
| **Repository Pattern** | Gestión del origen de datos   |
| **Gradle Kotlin DSL**  | Configuración y construcción  |
| **Android Studio**     | Entorno de desarrollo         |

---

## 🏗️ Arquitectura

El proyecto utiliza una arquitectura basada en **MVVM (Model-View-ViewModel)**, separando la interfaz de usuario, la gestión de estados, los modelos de dominio y el acceso a los datos.

```text
UI
 │
 ▼
ViewModel
 │
 ▼
Repository
 │
 ▼
Model / Data
```

Los **ViewModels no conocen las rutas de navegación**, manteniendo separadas las responsabilidades de la lógica de presentación y la navegación.

---

## 📁 Estructura del proyecto

```text
app/src/main/java/com/example/aquacontrol/
│
├── MainActivity.kt
│
├── ui/                              # Capa de interfaz de usuario
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
│   ├── flushing/
│   │   └── FlushingScreen.kt
│   │
│   ├── components/
│   │   └── ReusableButton.kt
│   │
│   └── navigation/
│       ├── AppNavHost.kt
│       └── Routes.kt
│
├── viewmodel/                       # Capa ViewModel
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
│   ├── detalle/
│   │   └── DetalleLineaViewModel.kt
│   │
│   └── flushing/
│       └── FlushingViewModel.kt
│
├── model/                           # Modelos de dominio
│   ├── perfil/
│   │   └── PerfilUsuario.kt
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
│   ├── historial/
│   │   └── HistorialTemperatura.kt
│   │
│   └── flushing/
│       └── Flushing.kt
│
└── repository/                      # Capa de acceso a datos
    └── bebedero/
        └── BebederoRepository.kt
```

---

## 🧭 Navegación

La aplicación utiliza **Navigation Compose** mediante un `NavHost` central.

### Rutas principales

```text
roles
operario
supervisor
granjas
galpones/{granjaId}
lineas/{galponId}
detalle/{lineaId}
flushing
```

### Flujo de navegación

```text
Selección de rol
       │
       ├── Operario
       │
       └── Supervisor
              │
              ▼
       Pantalla de bienvenida
              │
              ▼
           Granjas
              │
              ▼
           Galpones
              │
              ▼
     Líneas de bebederos
              │
              ▼
       Detalle de línea
              │
              ▼
           Flushing
```

---

## 🔥 Módulo Flushing

La versión **3.0** incorpora el módulo **Flushing**, orientado a simular un proceso de limpieza térmica de las líneas de bebederos.

El módulo permite representar el proceso dentro del flujo de navegación de la aplicación y mantener su estado mediante un `FlushingViewModel`.

```text
FlushingScreen
      │
      ▼
FlushingViewModel
      │
      ▼
Flushing
```

---

## 📊 Gestión del estado

La aplicación utiliza **StateFlow** para representar y observar los estados de la aplicación desde los ViewModels.

Ejemplo conceptual:

```text
UI
 │
 │ observa StateFlow
 ▼
ViewModel
 │
 │ actualiza estado
 ▼
Repository
```

Esto permite mantener una separación clara entre:

* Interfaz de usuario.
* Estado de la aplicación.
* Lógica de presentación.
* Acceso a datos.
* Modelos de dominio.

---

## 🛠️ Instalación

### 1. Clonar el repositorio

```bash
git clone https://github.com/tuusuario/AquaControl.git
```

### 2. Abrir el proyecto

Abrir el proyecto en **Android Studio**.

### 3. Sincronizar Gradle

Esperar a que Android Studio complete la sincronización de Gradle y la descarga de las dependencias necesarias.

### 4. Ejecutar

Ejecutar la aplicación utilizando:

* Un emulador Android.
* Un dispositivo físico Android.

---

## 👥 Autores

### Rimsky Farías Soto

* UI
* Navegación
* Arquitectura
* Documentación UI 3.0

### Christian Quiroz Roa

* Modelos
* Repository
* ViewModels
* Documentación MVVM 3.0

---

## 🎓 Contexto académico

Proyecto desarrollado para la:

**Evaluación Aplicada 2 (EA2)**
Módulo: **Desarrollo de Aplicaciones Móviles**
Institución: **DUOC UC**

---

## 📄 Licencia

Este proyecto fue desarrollado con fines **académicos** y corresponde a una evaluación de DUOC UC.

No está destinado a uso comercial.
