"""
Beispiel: Projektdatei des Endzeit-Editors einlesen.

    python load_project.py beispiel.json

Zeigt, wie ein Spiel Karten, Begehbarkeit, Aktionen, Quests und Gegner
aus der JSON-Datei liest. Nur Standardbibliothek.
"""

import json
import sys


def load(path):
    with open(path, encoding="utf-8") as f:
        data = json.load(f)
    if data.get("format") != "endzeit-rpg":
        raise ValueError("keine Endzeit-RPG-Projektdatei")
    return data


def is_walkable(game_map, x, y):
    return game_map["collision"][y][x] in (".", "!")


def action_at(game_map, x, y):
    for a in game_map["actions"]:
        if a["x"] == x and a["y"] == y:
            return a
    return None


def main():
    data = load(sys.argv[1] if len(sys.argv) > 1 else "beispiel.json")
    print(f"Projekt: {data['name']}")
    for m in data["maps"]:
        print(f"\nKarte {m['id']} '{m['name']}' ({m['width']}x{m['height']})")
        for row in m["terrain"]:
            print("  " + row)
        for a in m["actions"]:
            print(f"  Aktion bei ({a['x']},{a['y']}): {a['id'] or '-'}  {a['note']}")
    for q in data["quests"]:
        print(f"\nQuest {q['id']}: {q['title']}\n  {q['description']}")
    for e in data["enemies"]:
        print(f"\nGegner {e['id']}: {e['name']}  "
              f"STR {e['strength']} KON {e['constitution']} "
              f"BEW {e['dexterity']} INT {e['intelligence']}")
    for w in data.get("wiki", []):
        links = ", ".join(w["quests"] + w["enemies"]) or "-"
        print(f"\nWiki [{w['category']}] {w['title']}  (verknüpft: {links})")


if __name__ == "__main__":
    main()
