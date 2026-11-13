# Cómo contribuir

Gracias por tu interés en Suma Studio. Esta guía explica cómo proponer cambios.

## Antes de empezar

- Revisa los [issues abiertos](https://github.com/E25-Solutions/suma-studio/issues) y el [ROADMAP.md](ROADMAP.md).
- Para cambios grandes, abre primero un issue para acordar el enfoque.
- Toda participación sigue el [código de conducta](CODE_OF_CONDUCT.md).

## Flujo de trabajo

1. Haz un fork y crea una rama desde `main` con un nombre descriptivo (`fase-1/historial`, `fix/division-cero`).
2. Haz tus cambios con pruebas.
3. Ejecuta localmente:
   ```bash
   ./gradlew :core-math:jvmTest :androidApp:lintDebug :androidApp:assembleDebug
   ```
4. Abre un pull request hacia `main` explicando qué cambia y por qué. La CI debe pasar antes de integrarlo.

## Precisión ante todo

Un resultado incorrecto es el peor error que puede tener una calculadora. Por eso:

- **Todo cálculo nuevo lleva pruebas** en `core-math/src/commonTest`.
- **Pruebas de referencia:** cada caso esperado se verifica a mano contra una calculadora física (o su manual) antes de agregarse. Indica en el PR con qué la verificaste.
- **Nunca uses `Double` ni `Float`** para resultados que ve el usuario. Usa `BigDecimal` del motor.
- El motor (`core-math`) no depende de la interfaz ni de ninguna plataforma.

## Estilo de código

- Convenciones oficiales de Kotlin (`kotlin.code.style=official`); el `.editorconfig` del repo configura tu editor.
- Nombres de código en inglés; comentarios, textos de interfaz y documentación en español (con traducción al inglés en los recursos de la app).
- Commits cortos en modo imperativo: `Agrega memoria M+`, `Corrige redondeo en división`.

## Reportar errores

Usa la plantilla de *Reporte de error* e incluye la expresión exacta, el resultado obtenido y el esperado.

## Licencia

Al contribuir aceptas que tu código se publique bajo la [licencia Apache-2.0](LICENSE) del proyecto.
