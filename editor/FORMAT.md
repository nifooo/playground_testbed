# Projektdatei-Format (`endzeit-rpg`, Version 1)

Der Editor speichert **eine JSON-Datei** pro Projekt. JSON lässt sich in praktisch
jeder Sprache ohne Zusatzaufwand einlesen (Python `json`, C# `System.Text.Json`,
Lua `dkjson`, Godot `JSON.parse_string`, JavaScript `JSON.parse`, Rust `serde_json` …).

## Aufbau

```json
{
  "format": "endzeit-rpg",
  "version": 1,
  "name": "Mein Endzeit-RPG",
  "tileset": {
    "terrain":   { ".": { "name": "Ödland", "default_collision": "." }, "...": {} },
    "collision": { ".": "Begehbar", "#": "Nicht begehbar", "!": "Löst Aktion aus" }
  },
  "maps":    [ ... ],
  "quests":  [ ... ],
  "enemies": [ ... ],
  "wiki":    [ ... ]
}
```

`tileset` ist nur zur Information/Selbstbeschreibung enthalten – das Spiel kann es
nutzen, muss aber nicht.

## Karten (`maps`)

```json
{
  "id": "map_001",
  "name": "Ödland",
  "width": 6,
  "height": 3,
  "terrain":   ["..~~^^", ",,=~^^", "##+###"],
  "collision": ["..##!#", "...###", "##.###"],
  "actions":   [ { "x": 4, "y": 0, "id": "hoehle_eingang", "note": "Führt in die Höhle" } ]
}
```

* `terrain` und `collision` sind Listen von Zeilen (oben → unten). Jede Zeile ist ein
  String mit genau `width` Zeichen, ein Zeichen pro Feld. Feld (x, y) ist also
  `terrain[y][x]`.
* **Begehbarkeit** steht ausschließlich in `collision` – das ist die maßgebliche
  Information fürs Spiel:

  | Zeichen | Bedeutung        |
  |---------|------------------|
  | `.`     | begehbar         |
  | `#`     | nicht begehbar   |
  | `!`     | löst Aktion aus (begehbar, beim Betreten wird die Aktion ausgelöst) |

* Für jedes `!`-Feld gibt es genau einen Eintrag in `actions` mit derselben Position.
  `id` ist ein frei vergebener Schlüssel (kann leer sein, solange noch nichts
  definiert ist). Das Spiel verknüpft diese ID später mit Code/Skript/Dialog.
  `note` ist eine Notiz für dich.
* `terrain` bestimmt nur das Aussehen:

  | Zeichen | Gelände      | Standard-Eigenschaft |
  |---------|--------------|----------------------|
  | `.`     | Ödland       | begehbar |
  | `,`     | Steppe       | begehbar |
  | `*`     | Schutt       | begehbar |
  | `T`     | Tote Bäume   | begehbar |
  | `=`     | Straße       | begehbar |
  | `_`     | Boden innen  | begehbar |
  | `+`     | Tür          | begehbar |
  | `%`     | Giftsumpf    | begehbar |
  | `~`     | Wasser       | nicht begehbar |
  | `^`     | Berge        | nicht begehbar |
  | `#`     | Mauer        | nicht begehbar |
  | `X`     | Autowrack    | nicht begehbar |
  | `R`     | Ruine        | nicht begehbar |
  | ` ` (Leerzeichen) | Leere | nicht begehbar |

## Quests (`quests`)

```json
{ "id": "quest_001", "title": "Wasser für Neu-Eden", "description": "Freitext …" }
```

## Gegner (`enemies`)

```json
{
  "id": "enemy_001",
  "name": "Mutierte Ratte",
  "description": "Freitext …",
  "strength": 10,
  "constitution": 8,
  "dexterity": 14,
  "intelligence": 2
}
```

| Feld           | Editor-Bezeichnung | Wertebereich |
|----------------|--------------------|--------------|
| `strength`     | Stärke             | 0–100 (ganzzahlig) |
| `constitution` | Kondition          | 0–100 |
| `dexterity`    | Beweglichkeit      | 0–100 |
| `intelligence` | Intelligenz        | 0–100 |

## Wiki (`wiki`)

World-Building-Einträge. Fürs Spiel optional – nützlich z.B. für ein Ingame-Lexikon,
Questtexte oder einfach als Dokumentation.

```json
{
  "id": "wiki_003",
  "category": "main_story",
  "kind": "",
  "title": "Akt 1: Durst",
  "text": "# Akt 1\nNeu-Eden verdurstet. Der Spieler übernimmt [[quest_001]] …",
  "quests": ["quest_001"],
  "enemies": []
}
```

| Feld       | Bedeutung |
|------------|-----------|
| `category` | `world` (Die Welt), `main_story` (Hauptstory), `side_story` (Nebenstorys), `characters` (Gegner & NPCs), `items` (Gegenstände) |
| `kind`     | nur bei `characters`: `npc` oder `enemy`, sonst leer |
| `title`    | Überschrift des Eintrags |
| `text`     | Freitext mit einfachem Markdown: `# Überschrift`, `## Unterüberschrift`, `- Liste`, `**fett**`, `*kursiv*` |
| `quests`   | IDs der verknüpften Quests (im Editor bei Haupt-/Nebenstory) |
| `enemies`  | IDs der verknüpften Gegner (im Editor bei Gegner & NPCs) |

**Verweise im Text:** `[[id]]` oder `[[id|Anzeigetext]]` zeigt auf eine Quest, einen
Gegner, eine Karte oder einen anderen Wiki-Eintrag mit dieser ID. Benennt man im
Editor eine ID um, werden die Verknüpfungen und `[[…]]`-Verweise automatisch
angepasst.

## IDs

Alle `id`-Felder bestehen nur aus Buchstaben, Ziffern, `_` und `-` und sind innerhalb
ihrer Liste eindeutig. Damit kann das Spiel Karten, Quests und Gegner gegenseitig
referenzieren (z.B. eine Aktion „starte quest_001“).

## Erweiterungen

Neue Felder werden in späteren Versionen nur **hinzugefügt**; ein Spiel sollte
unbekannte Felder einfach ignorieren. Bei inkompatiblen Änderungen wird `version`
erhöht.
