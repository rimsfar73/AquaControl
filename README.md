🟦 📄 RESUMEN EJECUTIVO

AquaControl es una aplicación móvil desarrollada en Android Studio utilizando Jetpack Compose, MVVM, StateFlow, Navigation Compose y Repository Pattern. El sistema implementa un flujo jerárquico basado en el caso real de producción avícola, permitiendo visualizar:
•	Granjas disponibles
•	Galpones asociados
•	Líneas de bebederos
•	Estado térmico de cada línea
•	Alertas críticas, advertencias y condiciones normales
La aplicación simula la lógica de monitoreo presentada en el informe del caso, integrando modelos de datos, repositorios y pantallas funcionales que representan el flujo operativo de un supervisor u operario en terreno.
El proyecto cumple con los requisitos de la Evaluación Aplicada 2 (EA2), demostrando:
•	Arquitectura profesional MVVM
•	Navegación jerárquica con parámetros
•	Gestión de estado con StateFlow
•	Componentes reutilizables
•	Documentación técnica completa
•	Diagramas UML del flujo y estructura
AquaControl queda preparado como base para una futura integración con sensores reales, API REST y almacenamiento local mediante Room.

🟦 🎯 OBJETIVO GENERAL

Desarrollar una aplicación móvil funcional que permita visualizar y monitorear la temperatura de las líneas de bebederos en galpones avícolas, utilizando arquitectura MVVM, Jetpack Compose y Navigation, simulando el flujo operativo del caso real presentado en la EA2.

🟦 📌 OBJETIVOS ESPECÍFICOS

✔ 1. Implementar la arquitectura MVVM
Crear una estructura modular que separe UI, ViewModels, Modelos y Repository, asegurando mantenibilidad y escalabilidad.

✔ 2. Construir la navegación jerárquica del caso
Implementar rutas dinámicas para recorrer: Granjas → Galpones → Líneas → Detalle de Línea

✔ 3. Modelar los datos del caso Galpones
Crear clases de datos para:
•	Granja
•	Galpón
•	Línea de Bebedero
•	Estado de Línea
•	Historial de temperatura

✔ 4. Simular el origen de datos
Implementar un Repository que entregue datos simulados del caso, replicando el comportamiento esperado de un sistema real.

✔ 5. Gestionar estado con StateFlow
Crear ViewModels que expongan datos reactivos para cada pantalla, asegurando actualizaciones fluidas en Compose.

✔ 6. Construir pantallas funcionales
Diseñar pantallas Compose para cada nivel del flujo, mostrando información relevante y navegando entre ellas.

✔ 7. Documentar el proyecto
Generar README, diagramas UML, estructura de commits y documentación EA2 alineada al caso.

🟦 📁 Estructura completa del proyecto (versión profesional — Caso Galpones)
Dentro de:
app/src/main/java/com/example/aquacontrol/
Crea estas carpetas:
Código
ui/
    granjas/
    galpones/
    lineas/
    detalle/
    components/
    navigation/
viewmodel/
    granjas/
    galpones/
    lineas/
    detalle/
model/
    granja/
    galpon/
    linea/
    estado/
    historial/
repository/
    bebedero/
