# Cómo publicar una versión

Al subir un tag `vX.Y.Z`, el flujo `release.yml` corre las pruebas, compila el APK firmado y lo publica en GitHub Releases.

## Configuración inicial (una sola vez)

### 1. Crear la llave de firma

```bash
keytool -genkeypair -v -keystore suma-studio-release.jks \
  -alias suma-studio -keyalg RSA -keysize 4096 -validity 10000
```

> **Guarda el archivo `.jks` y sus contraseñas en un lugar seguro y con respaldo.** Si se pierden, no podrás publicar actualizaciones que se instalen sobre la versión anterior. Nunca lo subas al repositorio.

### 2. Cargar los secretos en GitHub

En **Settings → Secrets and variables → Actions → New repository secret**:

| Secreto | Valor |
| --- | --- |
| `SIGNING_KEYSTORE_BASE64` | Resultado de `base64 -w0 suma-studio-release.jks` (en macOS: `base64 -i suma-studio-release.jks`) |
| `SIGNING_KEYSTORE_PASSWORD` | Contraseña del keystore |
| `SIGNING_KEY_ALIAS` | `suma-studio` |
| `SIGNING_KEY_PASSWORD` | Contraseña de la llave |

## Publicar

```bash
git checkout main && git pull
git tag v0.1.0
git push origin v0.1.0
```

El número de versión se toma del tag (`v0.1.0` → `0.1.0`) y el código de versión del número de ejecución del flujo.
