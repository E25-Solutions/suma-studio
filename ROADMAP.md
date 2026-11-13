# Roadmap

Cada versión se publica en cuanto cumple su criterio de salida; nada espera a la siguiente.

## Fase 0 — Repositorio y arquitectura

- [x] Repositorio público con licencia Apache-2.0
- [x] README, CONTRIBUTING y código de conducta
- [x] Proyecto Kotlin Multiplatform: `core-math`, `core-ui`, `androidApp`, `iosApp`
- [x] CI en cada pull request: pruebas, lint y APK de depuración
- [x] Publicación del APK firmado en GitHub Releases al crear un tag
- [ ] Crear la llave de firma y cargar los secretos (ver [RELEASING.md](RELEASING.md))
- [ ] Ampliar las pruebas de referencia contra una calculadora física
- [ ] Abrir los issues de las Fases 1 y 2

**Criterio de salida:** el tag `v0.1.0` genera un APK firmado.

## Fase 1 — Calculadora básica (v1.0)

- [x] Expresiones completas con precedencia y paréntesis
- [x] Suma, resta, multiplicación, división, porcentaje, cambio de signo
- [x] Aritmética exacta con decimales de precisión arbitraria
- [x] Vista previa del resultado mientras se escribe
- [x] Historial con toque para reutilizar un resultado
- [x] Memoria (M+, M−, MR, MC)
- [x] Copiar y pegar números
- [x] Tema claro, oscuro y colores dinámicos (Material You)
- [x] Vibración al teclear (desactivable; falta recordar la preferencia al cerrar la app)
- [x] Interfaz en español e inglés
- [ ] Guardar historial, memoria y preferencias entre sesiones
- [ ] Ícono de la app
- [ ] Pruebas en dispositivos reales (teléfono pequeño, grande y tablet)

**Criterio de salida:** pasan todas las pruebas de referencia; APK v1.0 publicado, solicitud a F-Droid y ficha en Google Play.

## Fase 2 — Modo científico (v1.1 a v1.6)

| Versión | Contenido |
| --- | --- |
| v1.1 | Trigonometría (grados, radianes, gradianes) e inversas, hiperbólicas, logaritmos, potencias, raíces, factorial, nPr, nCr, constantes, números aleatorios |
| v1.2 | Resultados exactos: fracciones, mixtos, radicales simplificados y múltiplos de π; alternar exacto/decimal |
| v1.3 | Notación natural (editor tipo libro de texto) y modos Fix, Sci, Norm, ingeniería |
| v1.4 | Ecuaciones polinómicas hasta grado 4, sistemas lineales hasta 4×4, desigualdades, SOLVE; derivadas, integrales y sumatorias numéricas |
| v1.5 | Estadística de 1 y 2 variables, regresiones, distribuciones y tabla de valores |
| v1.6 | Complejos, base-N, matrices y vectores hasta 4×4, conversiones de unidades |

## Fase 3 — iOS y otras plataformas

- [ ] iOS / iPadOS con Compose Multiplatform
- [ ] F-Droid (Android sin servicios de Google) y Huawei AppGallery
- [ ] Versión web con Compose para Web (Wasm)
- [ ] HarmonyOS NEXT, según demanda
