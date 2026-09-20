# TICKET-010: Genereller Stil (Art-Style-Detailregeln)

**Typ:** Design / Art Direction (Sub-Ticket)
**Epic:** Endzeit-Wirtschaftssimulator – Visuelle Identität
**Priorität:** Hoch
**Status:** Entwurf fertig, bereit für Review
**Abhängigkeiten:** TICKET-009 (Hauptticket Art-Style)
**Geschwistertickets:** TICKET-011 bis TICKET-017

> **Stilrevision (2026-09-20):** Verbindliche Stilreferenz ist ab sofort der
> C64-Look von *SKALD – Against the Black Priory*: 16-Farben-VIC-II-Palette,
> durchgängiges Bayer-Dithering, tiefschwarze Hintergründe. Die Struktur-,
> Raster- und Systemfestlegungen dieses Tickets gelten unverändert weiter;
> geändert haben sich Palette und Schattierungstechnik (Begründung und
> Regeln: TICKET-009, Abschnitt 20). Aktueller Asset-Satz:
> `design/assets/artstyle-c64/`, Generator: `tools/gen_assets_c64.py`.

## Ziel des Tickets

Konkretisierung der in TICKET-009 festgelegten Grundprinzipien zu einem
verbindlichen, technisch anwendbaren Regelwerk: exakte Rastergrößen je
Assetklasse, Detailregeln zur Basispalette, Outline- und Dithering-
Konventionen sowie ein direkter Referenzvergleich zu Burntime. Dieses
Ticket ist die technische Grundlage, auf die sich alle anderen
Art-Style-Sub-Tickets (011-017) berufen.

## 0. Verhältnis zu TICKET-009

Während TICKET-009 die konzeptionelle Begründung liefert (warum Burntime,
warum niedrige Auflösung, warum begrenzte Animation), beantwortet dieses
Ticket die praktische Folgefrage: Nach welchen exakten Maßen, Farbregeln
und Konventionen wird tatsächlich produziert? Jede Zahl und Regel in diesem
Ticket lässt sich auf einen entsprechenden, allgemeineren Grundsatz aus
TICKET-009 zurückführen und widerspricht diesem an keiner Stelle.

## 1. Basispalette im Detail

Die in TICKET-009 (Abschnitt 3) eingeführte 32-Farben-Palette
(`design/assets/artstyle-c64/01_palette.png`) gliedert sich in fünf
Funktionsgruppen:

- **Neutralgruppe** (8 Farben): Schwarz, Beinahe-Schwarz, drei Grauabstufungen,
  Off-White, Pergament/Papierton, Dunkelbraun – für UI-Rahmen, Text,
  neutrale Flächen.
- **Erdgruppe** (5 Farben): Brauntöne von dunkel bis hell plus zwei
  Rostvarianten – Basis für Holz, Erde, Wastelander-Materialien.
- **Grüngruppe** (4 Farben): dunkles bis helles Grün plus Oliv – Vegetation,
  Mutierten-Akzentfarbe.
- **Blaugruppe** (4 Farben): dunkles bis helles Blau/Stahl – Wasser,
  Bunker-Akzentfarbe.
- **Signal-/Sondergruppe** (11 Farben): Kontaminationstöne (Toxic-Dark/Mid/
  Light), Warnfarben (Rot, Gelb, Blut), Hauttöne (drei Abstufungen) sowie die
  vier expliziten Fraktionsakzentfarben (Bunker-Blau, Bunker-Stahl,
  Mutant-Grün, Mutant-Ocker, Waste-Tan, Waste-Rot).

Diese Gliederung soll sicherstellen, dass jede neue Asset-Kategorie (siehe
TICKET-011 bis TICKET-017) auf dieselben, klar benannten Farbwerte
zurückgreift, statt eigene, leicht abweichende Zwischentöne zu erfinden.

## 1.1 Farbsparsamkeit als durchgängiges Prinzip

Innerhalb eines einzelnen Sprites oder Icons soll die Anzahl gleichzeitig
verwendeter Farben zusätzlich begrenzt bleiben: maximal 6-8 Farben pro
Charaktersprite, maximal 5-6 Farben pro Item-Icon. Diese Beschränkung ist
strenger als die reine Verfügbarkeit der 32-Farben-Basispalette und dient
dazu, den in TICKET-009 (Abschnitt 4) geforderten Fokus auf Silhouette statt
Detailtreue auch tatsächlich durchzusetzen, statt die Palette pro Asset
vollständig auszuschöpfen und damit unbeabsichtigt einen höher aufgelöst
wirkenden, dem Referenzstil widersprechenden Detailgrad zu erzeugen.

## 2. Rastergrößen je Assetklasse

Verbindliche native Pixelgrößen (vor Skalierung, siehe TICKET-009
Abschnitt 2):

| Assetklasse | Rastergröße | Referenz-Sub-Ticket |
|---|---|---|
| Charaktersprite (stehend) | 16×24 px | TICKET-014, TICKET-016 |
| Porträtbüste (Dialog) | 32×32 px | TICKET-013 |
| Gebäude (Standardgröße) | 32×28 px | TICKET-015 |
| Gebäude (groß, z. B. Bunkereingang) | 48×36 px | TICKET-015 |
| Item-/Waren-Icon | 16×16 px | TICKET-017 |
| Terrain-Tile (Hauptkarte) | 8×8 px | TICKET-012 |
| UI-Rahmenkachel | 8×8 px | TICKET-011 |

Diese Rastergrößen sind bewusst klein gehalten, um dem Referenzstil (siehe
TICKET-009, Abschnitt 1) zu entsprechen, und lassen sich beliebig
ganzzahlig auf moderne Bildschirmauflösungen skalieren (siehe TICKET-009,
Abschnitt 2).

## 3. Outline-Konvention

Sprites und Icons erhalten grundsätzlich eine **1 Pixel breite, dunkle
Kontur** (meist "near_black" oder "black" aus der Palette) an den äußeren
Silhouettenkanten, wie in den Mockups (`07a_sprite_bunker.png`,
`10a_item_werkzeug.png`) sichtbar. Innenliegende Kanten (z. B. zwischen
Torso und Akzentstreifen) erhalten in der Regel keine zusätzliche Kontur,
um die Leseflächen nicht zu überladen. Ausnahmen (z. B. Waffen, Werkzeuge
mit dünnen Formen) sind in den jeweiligen Sub-Tickets gesondert vermerkt.

## 4. Dithering-Konventionen

Dithering wird nach einem einfachen, wiederkehrenden Schema aus zwei
Palettenfarben im Schachbrett- oder Streifenmuster eingesetzt (siehe
TICKET-009, Abschnitt 3, sowie die Funktion `dither()` im
Referenzgenerator der Mockups). Es gelten drei Dichtestufen:

- **Dicht** (Muster alle 2 Pixel): für starke Texturwirkung, z. B.
  Kontaminationsflächen (TICKET-012) oder verwitterte Materialoberflächen.
- **Mittel** (Muster alle 3 Pixel): Standardschattierung für Terrain und
  größere Flächen.
- **Locker** (Muster alle 4 Pixel): dezente Texturandeutung, z. B. auf
  Kleidung oder Hautschattierung (siehe Porträtmockups, TICKET-013).

Reine Volltonflächen ohne jedes Dithering sind für kleine, klar
abgegrenzte Akzentflächen (z. B. Fraktionsstreifen) zulässig und
gewünscht, um den Kontrast zur texturierten Umgebung zu erhöhen.

## 5. Lichtrichtung und Schattenkonvention

Obwohl auf dynamische Beleuchtung verzichtet wird (siehe TICKET-009,
Abschnitt 7), erhält jedes Sprite eine konsistente, statische
"Lichtrichtung von oben links": hellere Palettenfarben oben/links, dunklere
unten/rechts innerhalb derselben Farbgruppe (z. B. bei Gebäuden sichtbar in
`08a_gebaeude_bunker.png`: helles Steel oben, dunklerer Rand unten). Diese
Konvention gilt projektweit für alle Sub-Tickets, damit Licht und Schatten
über unterschiedliche Assets hinweg konsistent wirken, obwohl keine echte
Beleuchtungssimulation stattfindet.

## 6. Typografie

Textdarstellung erfolgt über eine pixelbasierte Bitmap-Schrift im Stil
klassischer 8×8- oder 8×10-Pixel-Zeichensätze, ohne Anti-Aliasing (harte
Pixelkanten wie bei Sprites). Es werden zwei Schriftvarianten vorgesehen:
eine technisch-klare Variante für Bunker-Oberflächen (siehe TICKET-008,
Abschnitt 4) und eine leicht unregelmäßige, "handschriftlich" wirkende
Variante für Wastelander- und Mutierten-Oberflächen, jeweils im selben
Pixelraster, um die Lesbarkeit trotz stilistischer Differenzierung zu
gewährleisten.

## 7. Referenzvergleich zu Burntime im Detail

Ergänzend zu TICKET-009 (Abschnitt 12) hier eine tiefere technische
Einordnung: Burntime nutzte für Charakterporträts kreisförmig maskierte,
recht detailreiche Büsten bei insgesamt niedriger Palette. Für dieses
Projekt wird dieses Prinzip in Abschnitt 2 auf ein kompakteres 32×32-Raster
übersetzt (siehe TICKET-013), das bewusst blockiger und reduzierter bleibt,
um näher an der von TICKET-009 geforderten "Amiga-Chunkiness" zu liegen als
am Referenzspiel selbst, das teils schon an die Grenze dessen ging, was im
gewählten Retro-Stil als "grob genug" gelten soll.

## 8. Konsistenzregeln für Icon- und Sprite-Familien

Jede Familie verwandter Assets (z. B. alle Bunker-Gebäude, siehe
TICKET-015) muss dieselbe Grundpalette und dieselbe Dithering-Dichte
verwenden, darf sich aber in Form und Silhouette unterscheiden. Umgekehrt
dürfen unterschiedliche Fraktionen (siehe TICKET-001) niemals dieselbe
Akzentfarbe für ihre jeweilige Fraktionskennzeichnung verwenden, um
Verwechslungen auf der Hauptkarte (TICKET-012) und in Konfliktsituationen
(siehe TICKET-005) zu vermeiden.

## 8.1 Umgang mit Randgruppen und Sonderfällen

Für die in TICKET-005 (Abschnitt 8) beschriebenen Randgruppen (Banden,
Deserteure, Sektenanhänger), die keiner der drei Hauptfraktionen angehören,
wird bewusst keine eigene, feste Akzentfarbe reserviert. Stattdessen
nutzen ihre Sprites (siehe TICKET-016) ausschließlich die Neutral- und
Erdgruppe (Abschnitt 1) ohne klare Fraktionsakzentfarbe, um visuell sofort
als "nicht einer der drei Hauptfraktionen zugehörig" erkennbar zu sein –
ein einfaches, aber wirkungsvolles Mittel, um Übersichtlichkeit auf der
Hauptkarte (TICKET-012) auch bei einer wachsenden Zahl an Sprite-Typen zu
erhalten.

## 9. Zusammenspiel mit anderen Sub-Tickets

Dieses Ticket liefert die technische Referenzbasis für alle sechs
inhaltlichen Sub-Tickets (011-017). Insbesondere die Rastergrößen aus
Abschnitt 2 und die Palettengliederung aus Abschnitt 1 werden dort jeweils
konkret angewendet und nicht erneut hergeleitet.

## 10. Perspektive und Projektion

Für alle Sprite- und Gebäudeassets gilt eine einheitliche, leicht
frontal-schräge Perspektive ("Halbdraufsicht", vergleichbar mit klassischen
JRPG- und Amiga-Adventure-Darstellungen), bei der Charaktere und Gebäude
weitgehend frontal, aber mit leicht angedeuteter Tiefe durch Bodenschatten
(nicht durch echte perspektivische Verzerrung) dargestellt werden. Die
Hauptkarte (siehe TICKET-012) selbst nutzt hingegen eine reine
Draufsicht-Projektion für Terrain-Tiles, ähnlich einer klassischen
Overworld-Karte. Diese bewusste Zweiteilung der Perspektive zwischen
Kartenebene (Draufsicht) und Detailebene (Halbdraufsicht) folgt derselben
Logik wie die in TICKET-009 (Abschnitt 6) beschriebene Trennung zwischen
Übersichts- und Detaildarstellung und darf in keinem Sub-Ticket vermischt
werden, da sonst visuelle Brüche zwischen Kartenansicht und Detailszenen
entstehen würden.

## 11. Transparenz und Hintergrundbehandlung

Alle Sprite- und Icon-Assets werden mit einer klar definierten
Transparenzfarbe bzw. echtem Alphakanal freigestellt, sodass sie auf
beliebigen Hintergründen (Terrain, UI-Panel-Hintergrund) korrekt
erscheinen. Wichtig ist dabei, dass die in Abschnitt 3 beschriebene
Outline-Kontur immer Teil des sichtbaren Sprites bleibt und nicht
versehentlich mit der Transparenzfarbe verwechselt wird – ein häufiger
Fehler bei Pixel-Art mit dunklen Konturfarben nahe Schwarz. Zur Vermeidung
wird empfohlen, für Testzwecke jedes neue Asset einmal vor einem
kontrastierenden Testhintergrund (z. B. Magenta) zu prüfen, bevor es in die
Asset-Pipeline übernommen wird.

## 12. Asset-Benennungskonvention

Um die in TICKET-009 (Abschnitt 13) empfohlene Produktionspipeline
konsistent zu halten, wird folgendes Namensschema für alle Sub-Tickets
festgelegt: `<kategorie>_<fraktion-oder-typ>_<variante>.png`, z. B.
`sprite_bunker_stehend_frame1.png` oder `gebaeude_mutant_sippenhaus.png`.
Dieses Schema erleichtert die spätere automatisierte Zusammenstellung von
Sprite-Sheets und verhindert Verwechslungen zwischen ähnlich benannten
Assets unterschiedlicher Fraktionen, wie sie bei den in TICKET-014 und
TICKET-015 beschriebenen, strukturell ähnlichen Sprite-Familien leicht
auftreten könnten.

## 13. Beispielhafter Produktionsdurchlauf: Vom Konzept zum Asset

Zur Veranschaulichung des Zusammenspiels aller vorherigen Abschnitte ein
durchgespieltes Beispiel: Für ein neues Wastelander-Gebäude (siehe
TICKET-015) wird zunächst die Grundform im 32×28-Raster (Abschnitt 2)
festgelegt, orientiert an der in TICKET-002 beschriebenen improvisierten
Bauweise. Die Farbwahl beschränkt sich auf die Erdgruppe (Abschnitt 1) plus
die Wastelander-Akzentfarbe Waste-Rot. Schattierung erfolgt über
mittleres Dithering (Abschnitt 4) auf der Dachfläche, während die
Akzentfläche (Fraktionskennzeichnung, Abschnitt 8) als reine Volltonfläche
ohne Dithering gesetzt wird, um sie klar hervorzuheben. Die Lichtrichtung
folgt der in Abschnitt 5 festgelegten Konvention (helle Dachspitze oben
links, dunklere Wandpartie unten rechts). Abschließend erhält das fertige
Sprite eine 1-Pixel-Outline (Abschnitt 3) und wird nach dem
Benennungsschema aus Abschnitt 12 abgelegt. Dieser Ablauf soll als
Referenzprozess für alle weiteren, in TICKET-011 bis TICKET-017
beschriebenen Assets dienen.

## 14. Qualitätssicherung und Abnahmekriterien

Vor Übernahme eines neuen Assets in die Produktionspipeline (siehe
TICKET-009, Abschnitt 13) wird eine kurze Checkliste angewendet: Werden
ausschließlich Palettenfarben aus Abschnitt 1 verwendet? Entspricht das
Raster den Vorgaben aus Abschnitt 2? Ist die Fraktionszugehörigkeit auch in
kleiner Skalierung (wie sie auf der Hauptkarte, TICKET-012, tatsächlich
zum Einsatz kommt) noch erkennbar? Wurde ausschließlich mit den
Dithering-Dichtestufen aus Abschnitt 4 gearbeitet? Diese Checkliste
entspricht in ihrer Grundstruktur der in TICKET-009 (Abschnitt 9.1)
beschriebenen projektweiten Konsistenzprüfung, ist hier aber auf die
technischen Detailregeln dieses Sub-Tickets zugeschnitten.

## 15. Umgang mit Skalierungsstufen in der UI

Da dasselbe Sprite je nach Kontext unterschiedlich groß dargestellt werden
kann (z. B. ein Charaktersprite in der Detailszene groß, auf der
Hauptkarte nur als winziger Punkt, siehe TICKET-012), wird empfohlen, für
jede Assetklasse mindestens zwei Darstellungsgrößen vorzuhalten: die volle
native Auflösung aus Abschnitt 2 für Detailszenen sowie eine vereinfachte,
noch gröber reduzierte Symbolvariante für die Kartenübersicht (wie in
`02_hauptkarte.png` an den kleinen Siedlungssymbolen sichtbar). Diese
Doppelvorhaltung verhindert, dass Details, die in großer Darstellung
sinnvoll sind, in der winzigen Kartenübersicht zu einem unlesbaren
Pixelbrei verschwimmen.

## 16. Umgang mit Kontamination und Sonderfärbung

Wie in TICKET-002 beschrieben, spielt Kontamination eine zentrale Rolle im
Setting. Für die visuelle Umsetzung wird festgelegt, dass Kontamination
niemals über zusätzliche, neue Farben außerhalb der Signal-/Sondergruppe
(Abschnitt 1) dargestellt wird, sondern ausschließlich über die drei
Toxic-Abstufungen (Toxic-Dark/Mid/Light) in Kombination mit dichtem
Dithering (Abschnitt 4). Dies gilt sowohl für Terrain-Darstellung
(TICKET-012) als auch für eventuelle visuelle Statusmarkierungen an
Charakteren, die sich zu lange in kontaminierten Zonen aufgehalten haben.
Diese Beschränkung auf eine feste, kleine Farbgruppe verhindert, dass
Kontamination im Spielverlauf uneinheitlich dargestellt wird, je nachdem,
welches Sub-Ticket ein bestimmtes Asset ursprünglich definiert hat.

## 17. Einheitliche Bodenschatten

Charaktere und Gebäude (siehe TICKET-014, TICKET-015, TICKET-016) erhalten
grundsätzlich einen einfachen, ovalen Bodenschatten in einer einzigen,
projektweit einheitlichen Halbton-Farbe (abgeleitet aus "near_black" mit
reduzierter Deckkraft, falls Alphakanal genutzt wird, sonst als feste
Ditherfläche in "dark_grey"). Dieser Bodenschatten dient ausschließlich der
räumlichen Verortung auf der Halbdraufsicht-Ebene (siehe Abschnitt 10) und
ersetzt keine echte Beleuchtungssimulation. Er ist bewusst als einziges
"weiches" Element im ansonsten hart konturierten Stil zugelassen, da er in
praktisch jedem vergleichbaren Retro-Titel dieser Ära in ähnlicher Form
vorkommt und zur räumlichen Lesbarkeit beiträgt, ohne den Grundsatz aus
TICKET-009 (Abschnitt 7) zu verletzen.

## 18. Zusammenfassung der Designabsicht

Dieses Sub-Ticket übersetzt die in TICKET-009 formulierten, eher
konzeptionellen Leitlinien in ein konkretes, technisch nachvollziehbares
Regelwerk, das von allen an der Asset-Produktion Beteiligten unmittelbar
angewendet werden kann. Jede Regel in diesem Ticket – von der
Palettengliederung über die Rastergrößen bis zur Bodenschatten-Konvention
– existiert nicht als Selbstzweck, sondern um sicherzustellen, dass die
sechs inhaltlichen Sub-Tickets (011-017), obwohl sie unterschiedliche
Themen behandeln, am Ende wie aus einer Hand gestaltet wirken.

## Offene Punkte für Folgetickets

- Finales Bitmap-Font-Design (Zeichensatz, siehe Abschnitt 6).
- Technische Spezifikation für Sprite-Sheet-Formate (Padding, Trimming).
- Erweiterung der Palette bei Bedarf künftiger Regionen (siehe TICKET-002,
  Abschnitt 13), nur über TICKET-009 (Governance, siehe dort Abschnitt 14).

## 19. Wartbarkeit über die Projektlaufzeit

Da die in Abschnitt 12 beschriebene Benennungskonvention und die in
Abschnitt 14 beschriebene Qualitätssicherung über die gesamte Projektlaufzeit
angewendet werden sollen, wird empfohlen, dieses Ticket bei jeder größeren
Erweiterung der Asset-Basis (z. B. neue Randgruppen, neue Regionen gemäß
TICKET-002 Abschnitt 13) erneut als Referenz heranzuziehen, statt neue,
lokale Ad-hoc-Konventionen in einzelnen Sub-Tickets zu etablieren. Auf
diese Weise bleibt die visuelle Konsistenz auch dann erhalten, wenn das
Projekt über die in dieser Design-Runde beschriebenen acht Sub-Tickets
hinaus wächst.

## Akzeptanzkriterien

- [x] Basispalette in fünf Funktionsgruppen gegliedert.
- [x] Rastergrößen je Assetklasse tabellarisch festgelegt.
- [x] Outline- und Dithering-Konventionen definiert.
- [x] Lichtrichtungs- und Schattenkonvention festgelegt.
- [x] Referenzvergleich zu Burntime vertieft.
- [x] Umfang mindestens 2000 Wörter.

## Beispielgrafiken (C64-Stilrevision)

Vorschau (hochskaliert); native Pixeldateien liegen unter
`design/assets/artstyle-c64/native/`.

![Palette und Dither-Rampen](../assets/artstyle-c64/01_palette.png)
![Stilblatt](../assets/artstyle-c64/11_stilblatt.png)
