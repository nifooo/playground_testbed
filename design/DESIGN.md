# Design-Dokument: Endzeit-Wirtschaftssimulator

## Projektübersicht

Arbeitstitel eines Wirtschaftssimulators, angesiedelt in einem realistischen
(nicht-magischen, kaum sci-fi-lastigen) Endzeit-Szenario nach einem Dritten
Weltkrieg. Der Fokus liegt auf wirtschaftlichem Aufbau, Ressourcenmanagement,
Handel und Diplomatie/Konflikt zwischen drei ungleichen Fraktionen, die
unterschiedliche wirtschaftliche und gesellschaftliche Strategien verfolgen.

Dieses Dokument dient als Ankerpunkt für das Gesamtprojekt. Die eigentliche
inhaltliche Ausarbeitung erfolgt in Einzeltickets unter `design/tickets/`,
damit jeder Themenblock (Hintergrundgeschichte, Fraktionen, Wirtschaftssystem,
Karte, Technologiebaum, UI/UX, Balancing, ...) unabhängig bearbeitet,
diskutiert und versioniert werden kann.

## Leitplanken für das Setting

- **Realismus vor Science-Fiction/Magie**: Keine übernatürlichen Kräfte, keine
  Laser-Fantasy-Technologie. Mutationen sind biologisch/genetisch erklärbar
  und dezent (keine "Superkräfte").
- **Dritter Weltkrieg als Ausgangspunkt**: Ein global/regional eskalierter
  konventioneller Krieg mit begrenztem Nuklear-, Chemie- und Cyber-Einsatz,
  kein vollständiger nuklearer Overkill.
- **Drei Kern-Fraktionen** als Grundgerüst für Wirtschaft, Diplomatie und
  Konflikt:
  1. Bunker-Überlebende (gut ausgerüstet, kaum Mutation)
  2. Mutierte (gering ausgeprägte, funktionale Mutationen)
  3. Oberflächen-Überlebende / Wastelander (kaum Mutation, improvisiert, aber
     bestens angepasst)

## Roadmap / Ticket-Übersicht

| Ticket | Thema | Status |
|---|---|---|
| [TICKET-001](tickets/TICKET-001-hintergrundgeschichte.md) | Hintergrundgeschichte & Worldbuilding (3. Weltkrieg, Fraktionen) | Entwurf fertig |
| [TICKET-002](tickets/TICKET-002-kartendesign-regionen.md) | Kartendesign & Regionen (Kontaminationszonen, Bunker-Standorte, Handelsrouten) | Entwurf fertig |
| [TICKET-003](tickets/TICKET-003-wirtschaftssystem.md) | Wirtschaftssystem: Ressourcen, Produktionsketten, Währungen | Entwurf fertig |
| [TICKET-004](tickets/TICKET-004-handelssystem.md) | Handelssystem zwischen Fraktionen (Preise, Knappheit, Embargos) | Entwurf fertig |
| [TICKET-005](tickets/TICKET-005-kampf-konfliktsystem.md) | Kampf- & Konfliktsystem (Raids, Verteidigung, Diplomatie) | Entwurf fertig |
| [TICKET-006](tickets/TICKET-006-technologiebaum.md) | Technologie- & Forschungsbaum pro Fraktion | Entwurf fertig |
| [TICKET-007](tickets/TICKET-007-bevoelkerung-beduerfnisse.md) | Bevölkerungs- & Bedürfnissystem (Nahrung, Gesundheit, Moral) | Entwurf fertig |
| [TICKET-008](tickets/TICKET-008-ui-ux-konzept.md) | UI/UX-Konzept für den Simulator | Entwurf fertig |
| [TICKET-009](tickets/TICKET-009-art-style-hauptticket.md) | Grundlegendes visuelles Design (Art-Style, Hauptticket) – Burntime/Amiga-Stil | Entwurf fertig |
| [TICKET-010](tickets/TICKET-010-art-genereller-stil.md) | Art-Style: Genereller Stil (Palette, Raster, Dithering) | Entwurf fertig |
| [TICKET-011](tickets/TICKET-011-art-menues.md) | Art-Style: Menüs | Entwurf fertig |
| [TICKET-012](tickets/TICKET-012-art-hauptkarte.md) | Art-Style: Hauptkarte | Entwurf fertig |
| [TICKET-013](tickets/TICKET-013-art-portraets.md) | Art-Style: Porträts | Entwurf fertig |
| [TICKET-014](tickets/TICKET-014-art-spielfiguren-fraktionen.md) | Art-Style: Spielfiguren – Fraktionen | Entwurf fertig |
| [TICKET-015](tickets/TICKET-015-art-gebaeude-fraktionen-neutral.md) | Art-Style: Gebäude – Fraktionen & neutral | Entwurf fertig |
| [TICKET-016](tickets/TICKET-016-art-gegner.md) | Art-Style: Gegner | Entwurf fertig |
| [TICKET-017](tickets/TICKET-017-art-items-waren.md) | Art-Style: Items / Waren | Entwurf fertig |

Weitere Tickets werden bei Bedarf ergänzt (z. B. Balancing-Iterationen,
Soundkonzept, finale Production-Art auf Basis von TICKET-009 bis -017).

## Grafik-Assets

Verbindlicher Asset-Satz: [`assets/artstyle-c64/`](assets/artstyle-c64/README.md)
– C64-Retro-Stil nach der Stilrevision in TICKET-009 (Abschnitt 20):
16-Farben-VIC-II-Palette, durchgängiges Bayer-Dithering, schwarze
Hintergründe.

Alle Assets sind reproduzierbar und werden nicht von Hand nachbearbeitet:

```bash
pip install pillow
python tools/gen_assets_c64.py
```

- `assets/artstyle-c64/native/` – native Pixelgröße (Spiel und Prototyp)
- `assets/artstyle-c64/` – hochskalierte Vorschau (Tickets, Review)
- `assets/artstyle/` – früherer Entwurfssatz, nur noch Historie

## Prototyp

[`prototype/`](../prototype/README.md) enthält einen minimalen
pygame-Prototyp, der eine Figur mit WASD über die Hauptkarte bewegt.
