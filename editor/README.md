# Endzeit RPG Editor

Editor für Karten, Quests und Gegner eines Endzeit-Top-Down-RPGs im Stil von
Ultima I/II. Eine einzelne HTML-Datei, keine Installation nötig.

## Starten

`editor/index.html` im Browser (Chrome, Firefox, Edge) öffnen – fertig.

## Bedienung

**Karten**
* Links: Karten anlegen/löschen, ID, Name und Größe ändern.
* Rechts: Gelände wählen (Ödland, Straße, Wasser, Mauer, …) oder auf
  „Eigenschaft“ umschalten und Felder als **begehbar**, **nicht begehbar** oder
  **löst Aktion aus** markieren. Jedes Gelände setzt beim Malen seine
  Standard-Eigenschaft, die du danach überschreiben kannst.
* Werkzeuge: Pinsel (B), Rechteck (R), Füllen (F), Auswahl (I).
  Rechtsklick auf ein Feld nimmt Gelände/Eigenschaft auf (Pipette).
  Strg+Z / Strg+Y: Rückgängig / Wiederholen.
* Mit „Auswahl“ ein Aktionsfeld anklicken, um ihm eine **Aktions-ID** und eine
  Notiz zu geben. Die eigentliche Aktion wird später im Spiel über die ID verknüpft.
* „Begehbarkeit zeigen“: rot schraffiert = nicht begehbar, gelb mit `!` = Aktion.

**Quests** – ID, Titel, Quest-Beschreibung.

**Gegner** – ID, Name, Beschreibung, Stärke, Kondition, Beweglichkeit, Intelligenz.

**Speichern / Laden** – „Speichern“ (Strg+S) lädt das Projekt als `.json`
herunter, „Öffnen…“ liest es wieder ein. Zusätzlich hält der Browser den letzten
Stand automatisch vor – die JSON-Datei ist aber die eigentliche Sicherung.

## Dateiformat

Siehe [`FORMAT.md`](FORMAT.md). Kurz: eine JSON-Datei; Karten als Textzeilen mit
einem Zeichen pro Feld (`.` begehbar, `#` blockiert, `!` Aktion). Ein Beispiel
liegt in [`examples/beispiel.json`](examples/beispiel.json), ein Lese-Beispiel in
Python in [`examples/load_project.py`](examples/load_project.py).
