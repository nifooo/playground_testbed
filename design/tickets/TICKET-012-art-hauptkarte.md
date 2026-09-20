# TICKET-012: Hauptkarte (Art-Style)

**Typ:** Design / Art Direction (Sub-Ticket)
**Epic:** Endzeit-Wirtschaftssimulator – Visuelle Identität
**Priorität:** Hoch
**Status:** Entwurf fertig, bereit für Review
**Abhängigkeiten:** TICKET-009 (Hauptticket), TICKET-010 (Generelle
Stilregeln), TICKET-002 (Kartendesign & Regionen), TICKET-008 (Kartenansicht
in der UI)
**Geschwistertickets:** TICKET-010, TICKET-011, TICKET-013 bis TICKET-017

> **Stilrevision (2026-09-20):** Verbindliche Stilreferenz ist ab sofort der
> C64-Look von *SKALD – Against the Black Priory*: 16-Farben-VIC-II-Palette,
> durchgängiges Bayer-Dithering, tiefschwarze Hintergründe. Die Struktur-,
> Raster- und Systemfestlegungen dieses Tickets gelten unverändert weiter;
> geändert haben sich Palette und Schattierungstechnik (Begründung und
> Regeln: TICKET-009, Abschnitt 20). Aktueller Asset-Satz:
> `design/assets/artstyle-c64/`, Generator: `tools/gen_assets_c64.py`.

## Ziel des Tickets

Visuelle Ausarbeitung der Hauptkarte im Burntime-inspirierten Pixel-Art-
Stil: Terrain-Tiles für die in TICKET-002 beschriebene Talkessel-Region,
Kontaminations-Overlays, Siedlungs-/Bunker-Icons und Routenmarkierungen. Das
in TICKET-002 (Abschnitt 14) bereits grob skizzierte Konzept
unterschiedlicher, fraktionsspezifischer Kartenstile wird hier konkret in
Tile-Sets und Symbolsysteme übersetzt.

## 0. Verhältnis zu TICKET-002

TICKET-002 definiert die geografische und narrative Realität der
Talkessel-Region (Lage, Ausdehnung, Kontaminationsursprung, benannte Orte).
Dieses Ticket übernimmt diese Fakten unverändert und übersetzt sie
ausschließlich in die konkrete Pixel-Art-Darstellung. Keine Aussage in
diesem Ticket soll der in TICKET-002 festgelegten Geografie widersprechen;
etwaige Diskrepanzen (z. B. bei der relativen Lage zweier Siedlungen) sind
zugunsten von TICKET-002 aufzulösen.

## 1. Referenzmockup: Kartenausschnitt

Der Kartenmockup (`design/assets/artstyle-c64/02_hauptkarte.png`) zeigt bereits
zentrale Elemente im Zielstil: gedithertes Grasland als Basisterrain,
symmetrische, gezackte Gebirgssilhouetten am Nordrand (siehe TICKET-002,
Nordberge), ein mäandrierendes Flussband (die Aar), eine giftgrün
geditherte Kontaminationsfläche unten links (Aschenfeld-Randzone) sowie
kleine, farblich unterscheidbare Siedlungs-Icons. Dieses Mockup dient als
verbindliche Stilreferenz für alle weiteren Terrain- und Icon-Assets dieser
Karte.

## 2. Terrain-Tile-Set

Aufbauend auf dem 8×8-Tile-Raster aus TICKET-010 (Abschnitt 2) wird ein
Grundset an Terrain-Tiles definiert, das die in TICKET-002 (Abschnitt 7)
beschriebene Regionsübersicht abdeckt: Grasland (Grüngruppe, mittleres
Dithering), Wald/Gebirge (Grau-/Off-White-Dithering für Gebirgszüge, siehe
Mockup), Wasser (Blaugruppe, mit einzelnen helleren "Glitzer"-Pixeln für
Fließrichtung statt Animation), Ackerland (Oliv-Streifenmuster, siehe
Kornweiler-Darstellung im Mockup) sowie drei Kontaminationsstufen (siehe
Abschnitt 3). Jedes Tile ist so gestaltet, dass es nahtlos an gleichartige
Nachbartiles anschließt, ohne sichtbare Kachelgrenzen zu erzeugen – ein
zentrales technisches Kriterium für die Lesbarkeit einer zusammenhängenden
Kartenfläche.

## 3. Kontaminations-Overlay

Die vier Kontaminationsstufen aus TICKET-002 (Abschnitt 2) werden wie folgt
visuell kodiert, unter strikter Einhaltung der in TICKET-010 (Abschnitt 16)
festgelegten Farbbeschränkung auf die Toxic-Gruppe:

| Stufe | Farbkombination | Dithering-Dichte (TICKET-010 Abschnitt 4) |
|---|---|---|
| 0 – Sauber | keine Überlagerung, reines Basisterrain | – |
| 1 – Gering belastet | Toxic-Dark über Basisterrain | Locker |
| 2 – Mittel belastet | Toxic-Mid über Basisterrain | Mittel |
| 3 – Kraterzone | Toxic-Light plus vereinzelte Blood-Akzente | Dicht |

Die Kraterzone (Stufe 3) erhält zusätzlich unregelmäßige, dunkle
Kraterflecken (siehe Mockup: Blood-farbiger Fleck im Zentrum der
Kontaminationsfläche), um sie von Stufe 2 auch bei ähnlicher Grundfarbe
eindeutig unterscheidbar zu machen.

## 4. Gebirge, Fluss und Sonderterrain

Gebirgszüge (Nordberge, Südsteig, siehe TICKET-002) werden als
wiederkehrendes Dreiecksmuster in Grau/Off-White dargestellt (siehe
Mockup), wobei die Zackenhöhe grob die relative Steilheit andeutet, ohne
eine echte Höhenkarte zu simulieren. Der Fluss Aar wird als durchgehend
blaues Band mit einzelnen helleren Pixeln zur Andeutung von Strömung
gestaltet; Brücken (z. B. die Lange Brücke von Aarbrück, siehe TICKET-002
Abschnitt 8) erhalten ein eigenes, kurzes Überbrückungs-Tile in Brauntönen,
das den Fluss an dieser Stelle überlagert. Der Grauwald (TICKET-002,
Abschnitt 10) wird als dichteres, dunkleres Grün-Muster mit
unregelmäßigeren Dithering-Übergängen dargestellt, um seine Charakteristik
als unübersichtliches Niemandsland auch grafisch zu unterstreichen.

## 4.1 Aarbrück als visuelles Zentrum

Da Aarbrück, wie in TICKET-002 (Abschnitt 5) und TICKET-004 (Abschnitt 6)
beschrieben, die zentrale neutrale Handelsstadt der Region ist, erhält sie
auf der Karte eine leicht erhöhte visuelle Präsenz gegenüber anderen
Wastelander-Siedlungen: ein etwas größeres Icon, eine sichtbare
Brückenverbindung über den Fluss (siehe Abschnitt 4) sowie mehrere von
dort ausgehende Routenlinien (Abschnitt 6), die die Stadt schon rein
optisch als Knotenpunkt vieler Handelswege erkennbar machen, ohne dass ein
Spieler dafür erst die zugehörigen Texttickets (TICKET-002, TICKET-004)
gelesen haben muss.

## 5. Siedlungs- und Bunker-Icons

Jede in TICKET-002 benannte Siedlung erhält ein kompaktes Icon im
Haus-/Turm-Piktogramm-Stil (siehe Mockup), dessen Akzentfarbe die
Fraktionszugehörigkeit anzeigt (Bunker-Steel für Bastion Nord und Silo 12,
Mutant-Green für Sippensiedlungen, Waste-Tan/Waste-Red für
Wastelander-Orte, neutrale Pergamentfarbe für Aarbrück). Größere,
strategisch wichtige Orte (Bastion Nord, Kessingen-Ruinen) erhalten ein
leicht größeres Icon mit zusätzlichem Turm-/Antennenelement, um ihre
Bedeutung schon auf den ersten Blick von kleineren Außenposten
abzuheben, ohne dafür eine zusätzliche Beschriftung zu benötigen.

## 6. Routen- und Handelswege-Darstellung

Handelsrouten (siehe TICKET-002, Abschnitt 6, TICKET-004) werden als
gepunktete Linien in Pergamentfarbe über das Terrain gelegt (siehe Mockup),
die bei aktiver Karawanenbewegung (siehe TICKET-004, Abschnitt 7) durch ein
kleines, sich schrittweise entlang der Route bewegendes Karren-Icon ergänzt
werden – bewusst als diskrete Sprung-Bewegung in festen Zeitintervallen
statt als flüssige Animation, passend zur limitierten Animationsphilosophie
aus TICKET-009 (Abschnitt 5). Gesperrte Pässe (siehe TICKET-002, Abschnitt
9: saisonale Sperrung) werden durch ein kleines Schneekristall- oder
Barrieren-Icon direkt auf der Route markiert.

## 7. Bedrohungs- und Eskalations-Overlay

Aufbauend auf TICKET-008 (Abschnitt 3) wird ein einblendbares
Bedrohungs-Overlay bereitgestellt, das betroffene Grenzregionen (siehe
TICKET-005, Abschnitt 2: Eskalationsleiter) mit einer halbtransparenten,
geditherten Rottönung überlagert, deren Intensität mit der Eskalationsstufe
zunimmt (leichtes Dithering bei "Spannung", dichtes Dithering bei
"Belagerung"). Dieses Overlay ist unabhängig vom Kontaminations-Overlay
(Abschnitt 3) einzeln zu- und abschaltbar (siehe TICKET-011, Abschnitt 8),
da beide Overlays gleichzeitig auf derselben Fläche relevant sein können
(z. B. ein Konflikt am Rand der Aschenfeld-Kraterzone).

## 8. Fraktionsspezifische Kartendarstellung

Wie in TICKET-002 (Abschnitt 14) angelegt, unterscheidet sich die
Gesamtdarstellung der Karte je nach aktiver Spielerfraktion: Die
Bunker-Kartenvariante überlagert ein feines, technisches Gitternetz über
das Terrain und nutzt präzise, rechtwinklige Symbolik; die
Mutierten-Kartenvariante verzichtet auf das Gitternetz und betont
stattdessen die Kontaminationsgrenzen durch dickere, handgezeichnet
wirkende Umrisslinien; die Wastelander-Kartenvariante nutzt eine sichtbar
"abgepauste", leicht schräg wirkende Liniendarstellung mit zusätzlichen,
handschriftlichen Warnhinweisen an gefährlichen Stellen (z. B. am
Grauwald). Alle drei Varianten teilen sich dieselben Terrain-Tiles
(Abschnitt 2) und Icons (Abschnitt 5), unterscheiden sich aber im
Überlagerungsstil, konsistent mit der in TICKET-010 (Abschnitt 8)
beschriebenen Konsistenzregel für Assetfamilien.

## 9. Zoomstufen und Kartendetailgrad

Die Karte unterstützt zwei Zoomstufen: eine Übersichtsstufe (gesamte
Talkessel-Region sichtbar, Icons in verkleinerter Symbolvariante gemäß
TICKET-010 Abschnitt 15) und eine Detailstufe (einzelne Region, z. B. das
Osthügelland, mit vollständig aufgelösten Terrain-Tiles und sichtbaren
einzelnen Siedlungsgebäuden, die in dieser Zoomstufe bereits Teile der in
TICKET-015 beschriebenen Gebäudesprites andeuten). Der Übergang zwischen
den Zoomstufen erfolgt, konsistent mit TICKET-011 (Abschnitt 12), als
harter Schnitt ohne weiche Zoom-Animation.

## 9.1 Performance-Überlegung bei großen Kartenflächen

Da die Talkessel-Region (siehe TICKET-002, Abschnitt 1: ca. 260×190 km,
abgebildet über ein feinmaschiges Tile-Raster) in der Detailstufe aus
Abschnitt 9 potenziell aus einer sehr großen Zahl einzelner 8×8-Tiles
besteht, wird empfohlen, Terrain-Chunks (z. B. 20×20-Tile-Blöcke) als
vorgerenderte Bildkacheln statt einzelner Tile-Draw-Calls zu behandeln.
Dies ist eine rein technische Optimierungsempfehlung ohne Einfluss auf die
in den vorherigen Abschnitten beschriebene visuelle Erscheinung.

## 10. Zusammenspiel mit anderen Sub-Tickets

Die Hauptkarte ist die zentrale Bühne, auf der Elemente aus praktisch
allen anderen Sub-Tickets sichtbar werden: Siedlungsicons verweisen auf
TICKET-015 (Gebäude), Karawanen-Icons auf TICKET-017 (Items/Waren, in
Form der transportierten Güter) und TICKET-004 (Handel), und die
Bedrohungs-Overlays auf TICKET-016 (Gegner) und TICKET-005 (Konflikt).

## 11. Saisonale Terrain-Varianten

Aufbauend auf TICKET-002 (Abschnitt 9: Klima und Jahreszeiten) erhält das
Grundterrain zwei zusätzliche saisonale Varianten: eine Wintervariante mit
teilweise durch Off-White-Dithering überlagerten Grasflächen (Schneedecke)
und sichtbar zugefrorenen, hellblau geditherten Flussabschnitten, sowie
eine Dürre-Variante für die in TICKET-002 (Abschnitt 9) beschriebenen
Sommertrockenperioden, bei der Ackerland-Tiles (Abschnitt 2) von Oliv zu
einem stumpferen, bräunlicheren Grünton wechseln. Beide Varianten nutzen
ausschließlich bereits definierte Palettenfarben (TICKET-010, Abschnitt 1)
und ersetzen lediglich die Dithering-Kombination des jeweiligen
Basis-Tiles, ohne neue Tile-Formen einzuführen, um den Produktionsaufwand
gering zu halten.

## 12. Points of Interest im Detail

Die in TICKET-002 (Abschnitt 8) benannten Points of Interest (Staudamm,
Regionalflughafen, Steinbrüche, Lange Brücke) erhalten jeweils ein
individuelles, aber im selben Piktogramm-Stil wie die Siedlungsicons
(Abschnitt 5) gehaltenes Sondericon: der Staudamm als graue Blockform mit
zwei kleinen blauen Turbinenkreisen, der Flughafen als längliche,
hellgraue Streifenfläche mit einem stilisierten Flugzeug-Silhouetten-Icon,
die Steinbrüche als graue, treppenartig gestufte Fläche, die Lange Brücke
als schmales braunes Balkenelement über dem Fluss-Tile (Abschnitt 4).
Diese Sonder-Icons sind bewusst neutral (ohne Fraktionsakzentfarbe)
gehalten, da die zugehörigen Orte, wie in TICKET-002 beschrieben, oft
zwischen mehreren Fraktionen umstritten oder gemeinsam genutzt sind.

## 13. Unerforschte Gebiete und Nebel des Krieges

Zu Spielbeginn (siehe TICKET-008, Abschnitt 10: Onboarding) sind nicht alle
Bereiche der Talkessel-Region gleichermaßen bekannt. Unerforschte Flächen
werden nicht durch einen modernen, weichen Nebeleffekt verdeckt, sondern im
Stil einer unvollständig ausgefüllten Papierkarte dargestellt: grobe,
ungeditherte Pergamentfläche ohne Terrain-Details, mit vereinzelten,
handschriftlich wirkenden Fragezeichen-Symbolen an vermuteten, aber nicht
bestätigten Siedlungsorten. Sobald eine Region durch Kundschafter (siehe
TICKET-005, Abschnitt 3 und TICKET-006, Abschnitt 11) erkundet wurde, wird
die entsprechende Fläche einmalig und dauerhaft durch die volle
Terrain-Darstellung (Abschnitt 2-4) ersetzt – ohne erneutes "Verblassen",
da dies weder dem Referenzstil noch der Erzähllogik einer physisch
gezeichneten Karte entsprechen würde.

## 14. Minikarte

Zusätzlich zur Hauptkartenansicht (Abschnitt 1) wird eine kompakte
Minikarte vorgesehen, die permanent in einer Ecke der übrigen
Bildschirme (siehe TICKET-008, Abschnitt 2) eingeblendet werden kann. Sie
nutzt eine noch stärker reduzierte Symbolvariante der Terrain- und
Siedlungsdarstellung (vergleichbar mit der in TICKET-010, Abschnitt 15,
beschriebenen Doppelvorhaltung von Detail- und Symbolgrößen), wobei nur
die grobe Fraktionszugehörigkeit einzelner Regionen (nicht einzelne
Siedlungen) farblich angedeutet wird, um die Minikarte auch bei sehr
kleiner Darstellung übersichtlich zu halten.

## 15. Produktionshinweis für Terrain-Tiles

Für die spätere tatsächliche Asset-Produktion (siehe TICKET-009, Abschnitt
13) wird empfohlen, zunächst einen minimalen Satz von vier bis sechs
Basisterrain-Tiles inklusive ihrer nahtlosen Kantenübergänge (Abschnitt 2)
fertigzustellen und in einer kleinen Testkarte zu prüfen, bevor
Sonderterrain (Gebirge, Kontamination, saisonale Varianten) ergänzt wird.
Dieses schrittweise Vorgehen folgt demselben Prinzip wie der in TICKET-010
(Abschnitt 13) beschriebene Produktionsdurchlauf und verhindert, dass
Kantenübergangsfehler erst nach Fertigstellung des gesamten Tile-Sets
auffallen.

## 16. Wettereffekte auf der Karte

Konsistent mit der in TICKET-009 (Abschnitt 7) festgelegten Zurückhaltung
bei Effekten werden Wetterzustände (Regen, Schneefall, siehe TICKET-002,
Abschnitt 9) nicht durch Partikelsysteme, sondern durch statische,
mehrfach versetzt übereinandergelegte Dithering-Ebenen dargestellt, die in
festen, groben Zeitintervallen (nicht flüssig) zwischen zwei bis drei
Zuständen wechseln. Regen wird beispielsweise durch kurze, diagonale
Off-White-Striche in lockerer Dichte (TICKET-010, Abschnitt 4) angedeutet,
die alle paar Sekunden neu positioniert werden, statt sich flüssig über den
Bildschirm zu bewegen. Diese Lösung erzeugt einen spürbaren
Wettereindruck, ohne die für den Zielstil zentrale, harte
Pixelcharakteristik durch weiche Partikeleffekte zu verwässern.

## 17. Kartenrand und Weltgrenze

Da die Talkessel-Region, wie in TICKET-002 (Abschnitt 13) beschrieben,
bewusst als abgegrenzte, aber erweiterbare Spielwelt angelegt ist, wird der
Kartenrand nicht als harter, technischer Abbruch dargestellt, sondern als
allmählich in unerforschtes Gebiet (siehe Abschnitt 13) übergehende Fläche,
ergänzt um vereinzelte, handschriftliche Randnotizen im Kartenstil der
jeweiligen Fraktion (Abschnitt 8), die auf die drei in TICKET-002 genannten
Nachbarregionen (Küstenregion, zweites Bunkernetzwerk, Ostebenen) referieren
– etwa ein kleiner Vermerk "Weiter Richtung Küste – unbekannt" am
Südrand der Karte. Diese Gestaltung hält die Welt spürbar größer als den
tatsächlich bespielbaren Bereich, ohne einen abrupten, unmotivierten
Kartenrand zu erzeugen, sondern lädt Spieler subtil dazu ein, sich diese
Nachbarregionen als greifbaren nächsten Schritt einer möglichen
Spielerweiterung vorzustellen.

## 18. Zusammenfassung der Designabsicht

Die Hauptkarte soll nicht als reines Funktionsraster (wie eine moderne
Strategiespiel-Minimap), sondern als eigenständiges, atmosphärisches
Artefakt der Spielwelt wirken – eine Karte, die die jeweilige Fraktion
selbst gezeichnet und annotiert haben könnte (siehe Abschnitt 8). Jede in
diesem Ticket getroffene Entscheidung, von der Tile-Gestaltung über die
Kontaminations-Kodierung bis zur Behandlung unerforschter Gebiete, verfolgt
dieses eine Ziel: Die Karte soll gleichzeitig die in TICKET-002
beschriebene geografische Realität korrekt vermitteln und den in TICKET-009
festgelegten Retro-Charakter des gesamten Spiels konsequent fortführen.

## Offene Punkte für Folgetickets

- Vollständiges Tile-Set für Übergangskacheln zwischen unterschiedlichen
  Terrain-Typen (Produktionsdetail).
- Animationsdetails für das Karawanen-Icon (Sprung-Intervall, siehe
  Abschnitt 6).
- Technische Umsetzung der drei fraktionsspezifischen Kartenüberlagerungen
  als austauschbare Shader-/Overlay-Ebenen.

## 19. Bezug zur Kartenästhetik aus TICKET-002

Die in TICKET-002 (Abschnitt 14) bereits angedeutete Idee dreier
unterschiedlich annotierter Kartenversionen wird durch dieses Ticket
vollständig eingelöst: Die technisch-präzise Bunker-Karte, die auf
Kontaminationsgrenzen fokussierte Mutierten-Karte und die handgezeichnet
wirkende Wastelander-Karte (siehe Abschnitt 8) sind keine bloße
Beschriftungsvariante, sondern eigenständige Überlagerungsebenen über
demselben Terrain-Grundgerüst (Abschnitt 2), die dem Spieler intuitiv
vermitteln, aus wessen Perspektive er die Welt gerade betrachtet, ganz ohne
zusätzlichen erklärenden Text und ohne dass die zugrunde liegende
Terrain-Wahrheit der Karte dabei jemals zwischen den drei Varianten
divergiert.

## Akzeptanzkriterien

- [x] Terrain-Tile-Set inkl. Sonderterrain (Gebirge, Fluss, Grauwald)
      definiert.
- [x] Kontaminations-Overlay mit vier Stufen tabellarisch festgelegt.
- [x] Siedlungs-/Bunker-Icons mit Fraktionsfarblogik beschrieben.
- [x] Routen- und Bedrohungs-Overlay ausgearbeitet.
- [x] Fraktionsspezifische Kartendarstellung konkretisiert.
- [x] Umfang mindestens 2000 Wörter.

## Beispielgrafiken (C64-Stilrevision)

Vorschau (hochskaliert); native Pixeldateien liegen unter
`design/assets/artstyle-c64/native/`.

![Hauptkarte](../assets/artstyle-c64/02_hauptkarte.png)
![Szenenbild Bastion Nord](../assets/artstyle-c64/05_szene_bastion.png)
