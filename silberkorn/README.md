# Silberkorn

Eine Android-Kamera, die jedes Foto aus den rohen Sensordaten selbst entwickelt –
mit einem festen, filmischen Look statt der Bildverarbeitung des Telefons.

Die Idee stammt von der iPhone-App „Chromagraph Camera“; Silberkorn ist eine
eigenständige Umsetzung mit eigenem Namen, eigenem Code und eigenem Look. Die
Farbmathematik des Vorbilds ist nicht öffentlich und wird hier nicht nachgebildet.

## Was die App macht

- **RAW-Aufnahme** über Camera2 (`RAW_SENSOR`). Kameras ohne RAW fallen auf JPEG zurück.
- **Eigene Entwicklung**: Schwarzwert, Lens-Shading, Weißabgleich, Demosaicing
  (Malvar-He-Cutler), Kameramatrix → lineares sRGB, dann der Look: Print-Tonkurve,
  zurückhaltende, dichte Farben, warme Lichter, Halation um helle Lichtquellen, Korn.
- **Negative bleiben erhalten**: Sensordaten liegen unverändert in der App, jede
  Bearbeitung ist nachträglich änderbar.
- **Editor** mit vier Bereichen: Ton, Farbe, Textur, Format (inkl. 2,39:1).
- **Export** als JPEG nach `Bilder/Silberkorn`, auf Wunsch automatisch; optional DNG.
- **Import** vorhandener Fotos, um sie mit dem Look zu entwickeln.

## Bauen

```
./gradlew :app:assembleDebug        # APK unter app/build/outputs/apk/debug/
./gradlew :app:testDebugUnitTest    # Tests der Bild-Pipeline
```

Benötigt JDK 17+ und das Android SDK (Plattform 35). Mindestversion: Android 10.

## Aufbau

- `core/` – die Bild-Pipeline, reines Kotlin ohne Android-Abhängigkeiten.
  `Sources.kt` liest und demosaict, `Developer.kt` druckt den Look.
- `CameraController.kt` – Camera2: Sucher und RAW-Aufnahme samt Metadaten.
- `Library.kt` – die Negative auf dem Gerät. `Darkroom.kt` – Entwickeln und Export.
- `ui/` – Kamera, Galerie, Editor, Einstellungen (Jetpack Compose).

## Stand

Die Pipeline ist mit synthetischen RAW-Daten getestet. Kamera und Oberfläche
sind gebaut und geprüft (Lint), aber noch nicht auf einem echten Gerät gelaufen.
