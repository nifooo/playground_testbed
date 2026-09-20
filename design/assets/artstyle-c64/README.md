# Grafik-Assets: C64-Retro-Stil

Aktueller, verbindlicher Asset-Satz des Endzeit-Wirtschaftssimulators.
Stilreferenz: **SKALD – Against the Black Priory** (C64-Optik). Der
fruehere, flachere Amiga-Entwurfssatz wurde mit dieser Revision entfernt
(siehe TICKET-009, Abschnitt 20).

## Stilregeln

| Regel | Wert |
|---|---|
| Palette | 16 Farben, VIC-II / C64 (Pepto-Kalibrierung) – keine Zwischentoene |
| Schattierung | ausschliesslich geordnetes 8×8-Bayer-Dithering |
| Hintergrund | tiefes Schwarz, Formen mit hartem Kontrast davor |
| Skalierung | nur ganzzahlig, Nearest-Neighbor |
| Effekte | keine Verlaeufe, keine Weichzeichnung, kein Alpha-Blending |
| Schrift | eigener 3×5-Bitmapfont, keine Antialiasing-Glyphen |

## Ordnerstruktur

- `native/` – exakte Pixelgroesse, **diese Dateien nutzt das Spiel**
- `./` – dieselben Bilder ganzzahlig hochskaliert, fuer Tickets und Review

## Bestand

| Datei | Native | Inhalt |
|---|---|---|
| `01_palette.png` | 152×74 | Palette + Dither-Rampen |
| `02_hauptkarte.png` | 240×152 | Talkessel-Uebersichtskarte (TICKET-012) |
| `03_spielbildschirm.png` | 320×200 | Hauptbildschirm: Rahmen, Viewport, Portraetspalte, Statuspanel, Icon-Leiste (TICKET-011) |
| `04_dialogszene.png` | 320×200 | Dialogsystem mit Portraet, Text, Antwortoptionen (TICKET-011/-013) |
| `05_szene_bastion.png` | 320×200 | Szenenbild „Bastion Nord bei Nacht" |
| `06a–c_portrait_*.png` | 40×48 | Dialogportraets der drei Fraktionen (TICKET-013) |
| `07a–c_sprite_*.png` | 16×24 | Spielfiguren der drei Fraktionen (TICKET-014) |
| `08a–d_gebaeude_*.png` | 32×32 | Gebaeude Bunker / Mutierte / Wastelander / neutral (TICKET-015) |
| `09a–b_gegner_*.png` | 16×24, 24×16 | Raeuber und verwilderter Hund (TICKET-016) |
| `10a–h_item_*.png` | 16×16 | Werkzeug, Waffe, Medizin, Nahrung, Erz, Talon, Treibstoff, Chemie (TICKET-017) |
| `11_stilblatt.png` | 320×200 | Uebersichtstafel aller Assets + Palette |

Sprites, Gebaeude, Gegner und Items haben einen echten Alphakanal und sind
direkt blitbar.

## Regenerieren

Alle Assets sind vollstaendig reproduzierbar:

```bash
pip install pillow
python tools/gen_assets_c64.py
```

Der Generator ist die Quelle der Wahrheit – Aenderungen am Stil gehoeren
dorthin, nicht in nachtraeglich bearbeitete PNGs. Die Palette ist zentral
in `C64` definiert (siehe TICKET-009, Governance-Regel zur Palette).
