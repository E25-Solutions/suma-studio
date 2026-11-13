# iosApp (Fase 3)

Aquí vivirá el proyecto de Xcode de Suma Studio para iOS. Se crea en la Fase 3 del roadmap.

La interfaz ya es compartida: el módulo `core-ui` genera el framework `SumaStudioUI` y expone `MainViewController()`. La app de iOS solo necesita mostrarlo:

```swift
import SwiftUI
import SumaStudioUI

struct ContentView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
```

Requisitos: una Mac con Xcode y una cuenta de Apple Developer. La forma más rápida de generar el proyecto es el asistente de [Kotlin Multiplatform](https://kmp.jetbrains.com) y copiar su carpeta `iosApp`.
