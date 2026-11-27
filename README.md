# Suma Studio

Calculadora minimalista y de código abierto para Android. Sin anuncios, sin permisos de red y sin rastreo, con resultados exactos: `0.1 + 0.2` da `0.3`, no `0.30000000000000004`.

> **Estado:** Fase 1 en desarrollo — la calculadora básica ya funciona; falta pulirla para publicar la v1.0.

## Qué incluirá

- **Modo básico:** operaciones, porcentaje, historial, memoria, tema claro y oscuro.
- **Modo científico:** funciones del nivel de una calculadora científica escolar tipo fx-991: trigonometría, fracciones y radicales exactos, notación natural, ecuaciones, estadística, números complejos, bases numéricas, matrices y vectores.

El plan completo está en [ROADMAP.md](ROADMAP.md).

## Descargar

Cuando salga la v1.0, el APK estará en [Releases](https://github.com/E25-Solutions/suma-studio/releases). Después llegará a F-Droid y Google Play.

## Compilar desde el código

Requisitos: JDK 17 y el SDK de Android (lo instala Android Studio).

```bash
git clone https://github.com/E25-Solutions/suma-studio.git
cd suma-studio
./gradlew :core-math:jvmTest          # pruebas del motor matemático
./gradlew :androidApp:assembleDebug   # APK en androidApp/build/outputs/apk/debug/
```

También puedes abrir la carpeta en Android Studio y ejecutar `androidApp`.

## Estructura

| Módulo | Contenido |
| --- | --- |
| `core-math` | Motor matemático en Kotlin común: parser, evaluador y aritmética de precisión arbitraria. Sin interfaz. |
| `core-ui` | Interfaz compartida con Compose Multiplatform (Android e iOS). |
| `androidApp` | App de Android. |
| `iosApp` | App de iOS (Fase 3). |

Usa [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html), así que la lógica y la interfaz se comparten entre plataformas.

## Contribuir

Las contribuciones son bienvenidas. Lee [CONTRIBUTING.md](CONTRIBUTING.md) antes de abrir un pull request y respeta el [código de conducta](CODE_OF_CONDUCT.md).

## Contacto

Escribe a **edgaralfredoc@proton.me**. Para reportar una vulnerabilidad, sigue la [política de seguridad](SECURITY.md).

## Licencia

[Apache-2.0](LICENSE) © E25 Solutions.

Las marcas de calculadoras mencionadas (por ejemplo, Casio fx-991) pertenecen a sus dueños y se citan solo como referencia de funciones. Suma Studio no está afiliado a ellas.
