# 🎵 AuraBeat - Premium Digital Music Player & Synthesizer

Bienvenidos a **AuraBeat**, un reproductor de música premium e interactivo con síntesis de audio en tiempo real, persistencia local, letras sincronizadas, espectaculares efectos visuales de ondas musicales y una interfaz de usuario pulida con estilo Cyber-SciFi basada en **Material Design 3** y **Jetpack Compose**.

---

## 🌟 Características Principales

### 1. Reproductor Digital de Alta Fidelidad & Interfaz de Vinilo
- **Vista Dual / Interactiva (Disco & Letras)**: Intercambia al instante entre la reproducción de un vinilo giratorio interactivo analógico y una vista de letras sincronizadas en tiempo real.
- **Control Draggable de Platter (Scratch Realista)**: Implementación de gestos altamente fluidos utilizando `.draggable()` con `rememberDraggableState` para permitir al usuario realizar *scratch* sobre el vinilo giratorio, modulando en tiempo real la velocidad de reproducción y frecuencia (*pitch bend* de `0.4f` a `2.2f`).
- **Ecualizador Visual de Ondas (Waveform)**: Visualizador de barra de ondas adaptativo que responde de manera reactiva al estado dinámico del motor de reproducción.
- **Controles Táctiles Precisos**: Botones elegantes e intuitivos para juego/pausa, salto de pistas, modo aleatorio (shuffle), repetición y descargas locales, diseñados con un tamaño óptimo para accesibilidad física (>48dp).

### 2. Sintetizador de Audio Integrado (Synth Engine)
- **Motor de Audio de Baja Latencia**: Utiliza `AudioTrack` en modo stream (`AudioTrack.MODE_STREAM`) para sintetizar ondas de sonido reales client-side.
- **Controlador Dinámico de Volumen**: Barra de deslizamiento (Slider) de precisión para cambiar el nivel de atenuación de la señal generada.
- **Mute Táctil**: Control rápido para activar o desactivar instantáneamente las ondas sintetizadoras.
- **Soporte Multi-Versión Seguro**: Adaptado automáticamente para cumplir con los requisitos reglamentarios de Android 12+ (SDK 31+) inyectando de forma segura el contexto de atribución en el constructor de `AudioTrack.Builder(context)`.

### 3. Diseño Visual Futurista e Interfaz Fluida
- **Esquema de Colores "Cosmic Slate & Neon"**:
  - `DeepSableSpace`: El color profundo de fondo absoluto.
  - `NeonCyan` & `NeonViolet`: Acentos de alta tensión y brillo futurista.
  - `HotPink`: Alertas audaces y estados destacados.
- **Diseños Libres de Congestión**: Optimización de márgenes y paddings (`verticalArrangement = Arrangement.spacedBy(6.dp)`, límites máximos de altura y paddings adaptables) para evitar cualquier superposición de elementos, lo que da como resultado un reproductor increíblemente balanceado con excelente uso del espacio negativo.
- **Transiciones y Brillo Ambiental (Ambient Glow)**: Degradados interactivos fluidos que reflejan el estado activo de la pista.

### 4. Persistencia de Datos y Listas de Reproducción
- **Base de Datos Integrada**: Almacenamiento local seguro del catálogo musical, estados de descargas sin conexión de pistas individuales y listas personalizadas.
- **Cola Activa (Up Next)**: Lista interactiva de reproducción que muestra las canciones consecutivas, tiempos de duración exactos e indicadores visuales de reproducción en tiempo real.

---

## 🛠️ Arquitectura de Software

La aplicación sigue fielmente las pautas de arquitectura moderna de Android (**MVVM - Model-View-ViewModel**) con flujo de datos unidireccional (UDF) y componentes totalmente declarativos en **Jetpack Compose**:

```
app/src/main/java/com/example/
│
├── data/                  # Modelo de datos, canciones y mock database consistente
│   └── MusicDatabase.kt   # Definición de pistas, géneros y metadatos
│
├── player/                # Capa de hardware y síntesis
│   └── MusicPlayerEngine.kt # Manejador de AudioTrack, buffer y corrutinas de audio en tiempo real
│
├── ui/                    # Capa de presentación reactiva
│   ├── theme/             # Identidad visual de marca, paleta de colores M3, tipografía y formas
│   │   ├── Color.kt
│   │   └── Theme.kt
│   │
│   ├── screens/           # Pantallas de la aplicación
│   │   └── MainMusicScreen.kt # El reproductor digital completo y componentes interactivos
│   │
│   └── MusicViewModel.kt  # Gestión del estado global, sincronización reactiva y lógica de negocio
│
└── sync/                  # Repositorios y lógica de sincronización remota opcional
```

---

## 🚀 Requisitos y Configuración de Compilación

### Requisitos del Sistema
* **Android Gradle Plugin (AGP)** optimizado
* **Kotlin 1.9+** y soporte de **Jetpack Compose**
* **SDK Mínimo**: API level 26+

### Comandos de Compilación Rápidos (Gradle)

Para limpiar el proyecto y preparar las dependencias para una construcción limpia:
```bash
gradle compileJava
```

Para compilar el proyecto y generar el APK de prueba:
```bash
gradle assembleDebug
```

Para correr las pruebas unitarias locales y simuladores de control JVM (Robolectric):
```bash
gradle :app:testDebugUnitTest
```

---

## 🌟 Detalle de la Solución de Espaciado & Resolución de Errores (UI Refinement)

A solicitud del usuario, optimizamos la visualización del reproductor interactivo para dar un aspecto impecable, futurista y espacioso:
1. **Ajuste de Escala del Disco**: Redujimos la anchura relativa del vinilo giratorio (`fillMaxWidth(0.58f)`) y los espaciados verticales para liberar espacio en pantallas de tamaño medio.
2. **Interacciones Robustas con Draggable**: Reemplazamos los gestos pesados de drag con la API unificada `.draggable()`, logrando un control del vinilo mucho más suave y ligero, evitando a su vez colisiones con gestos del sistema y garantizando compatibilidad total en todas las plataformas.
3. **Consistencia de Corchetes & Áreas de Compilación**: Corregimos un error crítico de balanceo de corchetes en el archivo principal `MainMusicScreen.kt` que causaba que diversos componentes principales de la UI (tales como `HeroCard`, `EmptyStatePlaceholder`, `PlaylistCard` y el visualizador de letras sincronizadas) no fueran detectados en los bloques de nivel superior. La corrección restableció la jerarquía de llamadas a funciones Composable de manera óptima sin afectar el rendimiento.
4. **Paddings de Navegación del Sistema**: Añadimos soporte para `navigationBarsPadding()` en combinación con `statusBarsPadding()` para estructurar el lienzo de manera cómoda y balanceada sin importar el tipo de barra de navegación del dispositivo físico.
5. **Reducción de Tamaños de Barra de Progreso y Fuentes**: Redujimos ligeramente las fuentes e introdujimos un slider más delgado de `16.dp` para la barra de tiempo físico, otorgando una legibilidad impecable.

---

*Desarrollado con pasión utilizando las mejores prácticas de ingeniería de software en Android de Google DeepMind.*
