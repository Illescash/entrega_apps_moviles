# PartyHub

App Android de minijuegos para jugar con amigos de forma presencial.

## Juegos incluidos

### The Mind
Juego cooperativo (2-4 jugadores). Los jugadores deben ordenar cartas del 1 al 100 sin comunicarse entre ellos. Incluye tres niveles de dificultad.

### El As
Juego competitivo (3-6 jugadores) con baraja española. Intercambia cartas a ciegas e intenta sobrevivir con 3 vidas.

## Requisitos

- Android 7.0 (API 24) o superior
- Android Studio Hedgehog o superior

## Cómo ejecutar

1. Abrir el proyecto en Android Studio
2. Sincronizar Gradle
3. Ejecutar en emulador o dispositivo fisico (Run > Run 'app')

## Stack tecnico

- **Lenguaje**: Kotlin
- **UI**: XML layouts con ViewBinding y DataBinding
- **Arquitectura**: MVVM (ViewModel + LiveData + GameEngine)
- **Navegacion**: Jetpack Navigation Component con Safe Args
- **Logging**: Timber
- **Persistencia**:
    - **SharedPreferences**: Se utiliza para datos ligeros de configuración (alias del jugador, tema oscuro, volumen). Su uso es ideal para pares clave-valor que afectan al comportamiento inmediato de la UI.
    - **Room**: Se utiliza para el historial de partidas (datos estructurados). Permite realizar consultas complejas, ordenación y persistencia de objetos que requieren un esquema definido.


## Estructura del proyecto

```
com.partyhub/
  core/model/         Data classes (Player, Card)
  feature/hub/        Pantalla principal (seleccion de juego)
  feature/themind/    Juego The Mind (Activity, Fragments, ViewModel, Engine)
  feature/elas/       Juego El As (Activity, Fragments, ViewModel, Engine)
```

## Autores

- Diego Illescas Lasa
- Rafael Romero Monzon

DADM - UAM EPS 2025-2026
