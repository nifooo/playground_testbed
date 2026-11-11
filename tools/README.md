# Tools

| Skript | Zweck |
|---|---|
| `gen_assets_c64.py` | erzeugt den verbindlichen Asset-Satz (siehe `design/assets/artstyle-c64/README.md`) |
| `photo2pixel.py` | wandelt eigene Fotos in Pixelgrafik im selben C64/SKALD-Stil um |

## photo2pixel.py – Fotos in C64-Pixelgrafik

```bash
pip install pillow numpy
python tools/photo2pixel.py foto.jpg --typ portrait
```

Das Ergebnis folgt den Stilregeln des Projekts: nur die 16 C64-Farben (dieselbe
Tabelle `C64` wie im Asset-Generator), Schattierung über geordnetes
Bayer-Dithering, tiefes Schwarz, keine Zwischentöne.

### Ausgabe

Neben dem Foto (oder im Ordner aus `-o`) entstehen:

| Datei | Inhalt |
|---|---|
| `<name>_c64.png` | native Pixelgröße – **diese Datei bearbeiten und im Spiel nutzen** |
| `<name>_c64_x<N>.png` | ganzzahlig vergrößerte Vorschau |

Beide sind **indizierte PNGs** mit eingebetteter 16-Farben-Palette.
Aseprite, LibreSprite, GIMP, Krita, Photoshop und Piskel öffnen sie direkt
und bieten beim Weitermalen genau die C64-Farben an. Transparenz liegt auf
einem eigenen Palettenindex (16), Schwarz bleibt also als Farbe nutzbar.

Die Palette liegt zusätzlich als `design/assets/artstyle-c64/c64.gpl` vor
(GIMP-Palette; laden auch Aseprite, Krita und Inkscape).

### Asset-Typen

`--typ` setzt Größe und passende Voreinstellungen:

| Typ | Größe | Transparenz + Kontur |
|---|---|---|
| `szene` (Standard) | 320×200 | – |
| `karte` | 240×152 | – |
| `portrait` | 40×48 | – |
| `gebaeude` | 32×32 | ja |
| `sprite`, `gegner` | 16×24 | ja |
| `item` | 16×16 | ja |

Eigene Größe: `--groesse 64x64`.

### Typische Aufrufe

```bash
# Dialogportraet aus einem Gesichtsfoto
python tools/photo2pixel.py gesicht.jpg --typ portrait

# Figur/Gegenstand vor einfarbigem Hintergrund fotografiert -> freigestellter Sprite
python tools/photo2pixel.py figur.jpg --typ sprite --freistellen

# Bereits freigestelltes PNG (mit Alphakanal) -> Item
python tools/photo2pixel.py dose.png --typ item

# Szenenbild, ganzer Ordner auf einmal
python tools/photo2pixel.py fotos/*.jpg --typ szene -o out/

# Erst vergleichen: 3x3-Blatt mit Dither-Staerke x Kontrast
python tools/photo2pixel.py gesicht.jpg --typ portrait --varianten
```

### Die wichtigsten Regler

| Option | Wirkung |
|---|---|
| `--dither 0..1` | 0 = reine Farbflächen, 1 = volle Dither-Rampen |
| `--bayer 2\|4\|8` | Rastergröße des Dithers (8 = Projektregel, 2/4 = gröberes Muster) |
| `--ruhe` | höher = es werden nur ähnliche Farben gemischt, ruhigeres Bild |
| `--kontrast`, `--saettigung`, `--helligkeit` | Aufbereitung vor der Farbreduktion |
| `--schwarzpunkt` | wie viel vom Dunklen zu reinem Schwarz wird |
| `--farben black,dgrey,blue,lblue` | nur diese Palettenfarben verwenden (z.B. Fraktionsfarben) |
| `--glaetten` | Rauschen/Einzelpixel entfernen |
| `--freistellen`, `--toleranz` | einfarbigen Hintergrund von den Ecken aus entfernen |
| `--kontur` / `--no-kontur` | schwarze 1-Pixel-Kontur um freigestellte Motive |
| `--einpassen` | ganzes Bild zeigen statt mittig zuschneiden |

Alle Optionen: `python tools/photo2pixel.py --help`.

### Tipps für gute Ergebnisse

- Motiv formatfüllend und mit klarem Hell-Dunkel-Kontrast fotografieren;
  bei 16×24 Pixeln überlebt nur die Silhouette.
- Für Sprites und Items vor einer einfarbigen Fläche fotografieren, die sich
  farblich vom Motiv abhebt – dann reicht `--freistellen`. Bei unruhigem
  Hintergrund vorher in einer Bild-App freistellen und als PNG speichern.
- Kleine Typen brauchen fast immer Nacharbeit von Hand (Augen, Kanten). Dafür
  ist das native PNG gedacht.
