# Bewegungs-Prototyp

Minimaler Prototyp für den Endzeit-Wirtschaftssimulator: eine Figur
(Wastelander-Sprite aus TICKET-014) wird mit **WASD** (oder Pfeiltasten)
über die Hauptkarte (Mockup aus TICKET-012) bewegt. Keine Interaktion,
keine Animation – nur Bewegung im Pixelraster.

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

- Karte und Sprite werden aus `design/assets/artstyle/` geladen; die dort
  liegenden Mockups sind 6x-Exporte und werden zur Laufzeit auf ihre
  native Pixelgröße heruntergerechnet.
- Gerendert wird auf einer nativen Canvas, die als Ganzes ganzzahlig
  hochskaliert wird (Nearest-Neighbor) – gemäß den Stilregeln aus
  TICKET-009 (Abschnitt 2).
- Die Figur wird beim Zeichnen auf ganze Pixel gerundet und am Kartenrand
  begrenzt.
