# Bewegungs-Prototyp

Minimaler Prototyp für den Endzeit-Wirtschaftssimulator: eine Figur
(Wastelander-Sprite aus TICKET-014) wird mit **WASD** (oder Pfeiltasten)
über die Hauptkarte (TICKET-012) bewegt. Keine Interaktion, keine
Animation – nur Bewegung im Pixelraster.

## Voraussetzungen

- Python 3.10+
- pygame (`pip install pygame`)

## Starten

```bash
python prototype/main.py
```

## Steuerung

| Taste | Aktion |
|---|---|
| W / A / S / D | Figur bewegen |
| Pfeiltasten | Figur bewegen (alternativ) |
| ESC | Beenden |

## Hinweise zur Umsetzung

- Karte und Sprite werden in **nativer Pixelgröße** aus
  `design/assets/artstyle-c64/native/` geladen (240×152 bzw. 16×24) –
  erzeugt von `tools/gen_assets_c64.py`.
- Gerendert wird auf einer nativen Canvas, die als Ganzes ganzzahlig
  hochskaliert wird (Nearest-Neighbor, Faktor 4) – gemäß den Stilregeln aus
  TICKET-009 (Abschnitt 2 und 20).
- Der Sprite trägt einen echten Alphakanal, es wird kein Colorkey benötigt.
- Die Figur wird beim Zeichnen auf ganze Pixel gerundet und am Kartenrand
  begrenzt.
