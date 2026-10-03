# 📱 AppProximaPrueba — Prototipo 2

App Android en **Java** con **8 intents**: 5 implícitos y 3 explícitos. También tiene funciones de linterna y ubicación, validaciones y un Thread.

- 🤖 **Android mínimo:** 12 (API 31) · **Target:** API 36
- 🛠️ **AGP:** 9.0.1 · **Java:** 11

---

## 🧭 Intents explícitos (3)

| # | Intent | Cómo probarlo |
|---|--------|---------------|
| 1 | `Panel` → `Segunda_Vista` con `putExtra("nombre")` | Escribe tu nombre y presiona **🪟 Segunda Ventana**. Aparece "Cargando…" y luego el saludo |
| 2 | `Panel` → `Ayuda` | Presiona **❓ Ayuda** |
| 3 | `Panel` → `Config` | Presiona **🔧 Configuración** |

## 🌐 Intents implícitos (5)

| # | Intent | Cómo probarlo |
|---|--------|---------------|
| 1 | 🗺️ Mapa: `ACTION_VIEW` con `geo:lat,lng` | Presiona **📍 Obtener mi Ubicación** y luego **🗺️ Ver ubicación en el Mapa** |
| 2 | 🌍 Web: `ACTION_VIEW` con `https://` | Presiona **🌍 Abrir Página Web** |
| 3 | 📞 Marcador: `ACTION_DIAL` con `tel:` | Escribe un número de 9 dígitos y presiona **📞 Llamar** |
| 4 | ✉️ Correo: `ACTION_SENDTO` con `mailto:` | Presiona **✉️ Enviar Correo**. Se abre con el asunto y el cuerpo ya escritos |
| 5 | 📶 Wi-Fi: `Settings.ACTION_WIFI_SETTINGS` | Presiona **📶 Ajustes Wi-Fi** |

## ⚙️ Funciones extra

- 🔦 **Linterna**: prende y apaga el flash con `CameraManager`.
- 📍 **Ubicación**: obtiene la latitud y longitud con `LocationManager`.
- 🧵 **Thread**: la Segunda Ventana simula una carga en segundo plano y actualiza la pantalla con `runOnUiThread`.

## ✅ Validaciones

- ✍️ El nombre no puede ir vacío.
- 📞 El teléfono debe tener 9 dígitos.
- 📍 Hay que obtener la ubicación antes de abrir el mapa.
- 🔐 Pide los permisos de cámara y ubicación, y avisa si se rechazan.
- 🚫 Si no hay una app para abrir un intent, muestra un mensaje en vez de cerrarse (`ActivityNotFoundException`).
- 🧩 Revisa que el extra recibido no sea `null`.

## 🎨 Organización

Los textos, colores, tamaños y estilos están en `res/values/` (`strings.xml`, `colors.xml`, `dimens.xml`, `styles.xml`). Los botones azules son explícitos, los verdes son implícitos y los oscuros son funciones.

## 📸 Capturas

| Panel | Segunda Ventana | Mapa | Correo |
|-------|-----------------|------|--------|
| ![](capturas/panel.png) | ![](capturas/segunda.png) | ![](capturas/mapa.png) | ![](capturas/correo.png) |

## ▶️ Cómo compilar

1. Abre el proyecto en **Android Studio**.
2. Presiona **Run ▶️**, o en la terminal ejecuta `./gradlew assembleDebug`.
3. El APK queda en `app/build/outputs/apk/debug/app-debug.apk`.
