# TICKET-004: Handelssystem zwischen Fraktionen

**Typ:** Design / Gameplay-System
**Epic:** Endzeit-Wirtschaftssimulator – Wirtschaft & Diplomatie
**Priorität:** Hoch
**Status:** Entwurf fertig, bereit für Review
**Abhängigkeiten:** TICKET-001 (Fraktionen), TICKET-002 (Karte & Routen),
TICKET-003 (Ressourcen & Währungen)
**Blockiert:** TICKET-005 (Kampf/Diplomatie nutzt Handelsbeziehungen als
Eskalationsgrundlage)

## Ziel des Tickets

Ausarbeitung des Handelssystems zwischen den drei Fraktionen: Wie entstehen
Preise, wie wirkt sich Knappheit aus, wie funktionieren Embargos und
Sanktionen, welche Rolle spielt Vertrauen/Reputation, und wie hängt Handel
mit den geografischen Gegebenheiten aus TICKET-002 sowie den Ressourcen- und
Währungssystemen aus TICKET-003 zusammen. Handel ist im Simulator kein reines
Zusatzfeature, sondern der zentrale Mechanismus, über den die strukturellen
Abhängigkeiten der Fraktionen (siehe TICKET-001 und TICKET-003) tatsächlich
aufgelöst oder verschärft werden.

## 1. Grundmechanik des Handels

Handel findet auf drei Ebenen statt: **lokaler Markthandel** (innerhalb einer
Siedlung, meist Nahrung und Alltagsgüter), **regionaler Karawanenhandel**
(zwischen Siedlungen und Fraktionen entlang der in TICKET-002 definierten
Routen) und **strategischer Fraktionshandel** (offizielle, oft über Tage oder
Wochen verhandelte Großlieferungen zwischen Bunker-Rat, Sippen-Ältesten und
Wastelander-Ältestenräten). Jede Ebene hat eigene Geschwindigkeit,
Risikoprofil und Mengenordnung: Marktscharmützel um ein paar Brote laufen in
Echtzeit, strategische Lieferverträge über mehrere Wagenladungen Medizin
oder Werkzeuge werden dagegen als mehrstufige Verhandlung mit
Zwischenereignissen (Verzögerung, Teillieferung, Vertragsbruch) modelliert.

## 2. Preisbildung

Preise entstehen grundsätzlich aus Angebot und Nachfrage der jeweiligen
Region, moduliert durch drei Faktoren: **Transportkosten** (Distanz,
Passzustand, Risiko laut TICKET-002), **Knappheitsmultiplikatoren** (siehe
Abschnitt 3) und **Vertrauens-/Reputationsaufschläge** (siehe Abschnitt 5).
Ein Gut, das in einer Region im Überfluss vorhanden ist (z. B. Getreide im
Osthügelland nach guter Ernte), ist dort günstig, wird aber in einer
Mangelregion (z. B. Bastion Nord kurz vor Wintereinbruch) zum Vielfachen des
Ausgangspreises gehandelt – vorausgesetzt, der Transport über den
entsprechenden Pass ist überhaupt möglich. Das System soll dadurch
regionale Preisunterschiede aktiv als Spielanreiz nutzen: Gewinnbringender
Handel bedeutet, Überschüsse aus Region A in die Mangelregion B zu bringen,
bevor Konkurrenz oder saisonale Sperrungen das Zeitfenster schließen.

## 3. Knappheit und Preisschocks

Neben der gewöhnlichen saisonalen Knappheit (siehe TICKET-003, Abschnitt 7)
sind gezielte Knappheitsereignisse vorgesehen, die kurzfristige
Preisschocks auslösen: eine Kontaminationsausbreitung, die eine
Fördergegend vorübergehend sperrt, ein Maschinenausfall in einem
Bunker-Werk, der die Werkzeugproduktion einbrechen lässt, oder ein
Krankheitsausbruch (siehe TICKET-007), der die Nachfrage nach Medizin
sprunghaft steigen lässt. Diese Ereignisse wirken sich nicht sofort
regionsweit gleich aus, sondern breiten sich mit einer Verzögerung entlang
der Handelsrouten aus – ein Preisschock in Aschenfeld erreicht Aarbrück
schneller als das entfernte Silo 12, was Spielern, die frühzeitig reagieren,
einen Informationsvorteil verschafft.

## 4. Embargos und Sanktionen

Embargos sind ein zentrales diplomatisches Werkzeug, das eine Fraktion
gegen eine andere einsetzen kann, um politischen Druck auszuüben, ohne
offen militärisch zu eskalieren. Ein Embargo bedeutet in der Spielmechanik:
offizielle Handelsrouten und -verträge zwischen zwei Fraktionen werden
ausgesetzt, wodurch beide Seiten auf teurere, riskantere
Schwarzmarktalternativen (siehe TICKET-003, Abschnitt 11) oder auf
Umwege über die neutrale dritte Fraktion ausweichen müssen. Ein Beispiel:
Sollte Bastion Nord ein Embargo gegen die Aschenfeld-Sippen verhängen (etwa
nach einem Sabotageakt am Staudamm), können die Sippen weiterhin über
Aarbrücker Zwischenhändler an Bunker-Werkzeuge gelangen, allerdings zu
deutlich höheren Preisen und mit dem Risiko, dass die Lieferung als
Embargobruch entdeckt und diplomatisch eskaliert wird. Embargos sind damit
nie vollständig wirksam, aber wirtschaftlich schmerzhaft für beide Seiten –
ein bewusst realistisches Abbild historischer Sanktionsdynamiken.

## 5. Vertrauen und Reputation

Jede Handelsbeziehung zwischen zwei Parteien (Fraktion-Fraktion,
Siedlung-Siedlung, oder sogar einzelne Händlerfiguren) wird durch einen
Reputationswert abgebildet, der sich aus eingehaltenen Verträgen,
Zahlungsverlässlichkeit und diplomatischem Verhalten speist. Hohe Reputation
senkt Preise (Vertrauensrabatt), ermöglicht Kredit (Lieferung gegen
zukünftige Zahlung) und Zugang zu selteneren, nicht öffentlich gehandelten
Gütern. Niedrige Reputation führt zu Aufschlägen, Vorkasse-Pflicht oder
vollständiger Handelsverweigerung. Reputation ist bewusst asymmetrisch
modelliert: Ein einzelner Vertragsbruch schadet deutlich mehr, als ein
einzelner eingehaltener Vertrag nützt – ein Prinzip, das dem realen
Verhalten von Vertrauensbeziehungen in fragmentierten Nachkriegsgesellschaften
entspricht und Spielern verdeutlicht, dass kurzfristiger Betrug langfristig
teuer wird.

## 6. Aarbrück als neutraler Handelsknoten

Wie in TICKET-002 beschrieben, ist Aarbrück der einzige wirklich neutrale
Handelsort der Region. Im Handelssystem übernimmt die Stadt eine
Vermittlerrolle: Sie bietet einen zentralen Marktplatz mit öffentlich
einsehbaren Referenzpreisen (in Aarbrücker Talons, siehe TICKET-003), eine
neutrale Lagerinfrastruktur gegen Gebühr, sowie einen Schlichtungsmechanismus
des Ältestenrats für Streitfälle zwischen Händlern verschiedener Fraktionen.
Dieser neutrale Status ist jedoch fragil: Sollte eine Fraktion versuchen,
Aarbrück militärisch oder politisch zu dominieren, würde die Stadt ihre
Vermittlerfunktion verlieren, was allen drei Fraktionen wirtschaftlich
schaden würde – ein eingebauter Anreiz, den Status quo der Stadt zu
respektieren, aber auch ein Angriffspunkt für Spieler, die gezielt die
Handelsstruktur der Region destabilisieren wollen.

## 7. Karawanen, Risiko und Eskorten

Der physische Transport von Handelsgütern zwischen Regionen erfolgt über
Karawanen, die entlang der Routen aus TICKET-002 reisen und dabei Risiken
ausgesetzt sind: Überfälle durch Banden im Grauwald, Wetterrisiken an den
Pässen, sowie Verderb bei zu langer Transportzeit. Spieler können Karawanen
eskortieren lassen (kostet Ressourcen/Personal, senkt Verlustrisiko),
versichern lassen (gegen Gebühr bei Aarbrücker Händlern, kompensiert
Teilverlust) oder ungeschützt reisen lassen (günstiger, aber riskanter).
Diese Abwägung verbindet das Handelssystem direkt mit dem Kampf- und
Konfliktsystem (TICKET-005): Überfallhäufigkeit hängt von der allgemeinen
Sicherheitslage einer Region ab, die wiederum von militärischen
Auseinandersetzungen und Patrouillenpräsenz beeinflusst wird.

## 8. Handelsgüterprofile je Fraktion

Zur Übersicht, was jede Fraktion typischerweise anbietet und nachfragt:

| Fraktion | Wichtigste Exportgüter | Wichtigste Importbedarfe |
|---|---|---|
| Bunker-Überlebende | Werkzeuge, Medizin (synthetisch), Waffen, Ersatzteile | Nahrung, Fasern, Rohstoffe, Frischwasser |
| Mutierte | Heilpflanzen, veredelte Chemikalien, Feinmechanik, seltene Legierungen | Nahrung, einfache Werkzeuge, Schutzausrüstung |
| Wastelander | Getreide, Vieh, Rohstoffe, Textilien | Werkzeuge, Medizin, Waffen, Chemikalien |

Diese komplementäre Struktur ist bewusst so gestaltet, dass jedes
Zweier-Bündnis zwischen Fraktionen wirtschaftlich sinnvoll ist, aber keine
Kombination die dritte Fraktion vollständig überflüssig macht – ein
Gleichgewicht, das strategische Bündnisentscheidungen im späteren
Diplomatiesystem (TICKET-005) interessant halten soll.

## 9. Handelsverträge und Vertragsbruch

Strategische Lieferverträge zwischen Fraktionen werden als mehrstufige
Objekte modelliert: Laufzeit, Liefermenge, Preis, Strafklausel bei
Nichteinhaltung, sowie optionale Bedingungen (z. B. "nur bei offenem
Nordpass"). Vertragsbruch – sei es durch tatsächliche Unmöglichkeit
(Ernteausfall) oder bewusste Entscheidung – wirkt sich unmittelbar auf die
Reputation aus (siehe Abschnitt 5) und kann, je nach Schwere und
politischer Lage, auch zu diplomatischen Verstimmungen bis hin zu offenen
Konflikten eskalieren, die in TICKET-005 detailliert ausgearbeitet werden.

## 9.1 Eskalationspfad bei wiederholtem Vertragsbruch

Ein einzelner Vertragsbruch löst zunächst nur einen Reputationsverlust und
möglicherweise eine vertraglich festgelegte Strafzahlung aus. Wiederholter
oder besonders schwerwiegender Vertragsbruch (z. B. Nichtlieferung
lebenswichtiger Medizin während einer Epidemie) kann jedoch eine
diplomatische Eskalationsstufe auslösen: von einer formellen Beschwerde über
ein Teilembargo bis hin zu einer vollständigen Aufkündigung aller
Handelsbeziehungen. Dieser gestufte Übergang von wirtschaftlicher zu
diplomatischer und potenziell militärischer Eskalation ist die zentrale
Schnittstelle zu TICKET-005 und soll dort im Detail als Eskalationsleiter
ausgearbeitet werden.

## 10. Zusammenspiel mit anderen Systemen

Das Handelssystem verbindet die Ressourcen- und Währungslogik aus
TICKET-003 mit der Geografie aus TICKET-002 und liefert die wirtschaftliche
Eskalationsgrundlage für das Konfliktsystem aus TICKET-005 (Embargos und
Vertragsbrüche als Vorstufe zu bewaffneten Auseinandersetzungen). Zugleich
liefert es dem Technologiebaum (TICKET-006) einen Anreiz: Neue
Produktionsstufen verschieben die Handelsgüterprofile aus Abschnitt 8 und
verändern damit langfristig, welche Bündnisse wirtschaftlich am
attraktivsten sind.

## 11. Beispielhafte Verhandlung: Werkzeuge gegen Medizin

Um die Mechanik konkret zu machen, ein durchgespieltes Beispiel: Die
Bittergraben-Sippen benötigen dringend neue Erntewerkzeuge, da ihre alten
durch den Kontakt mit dem toxischen Boden schneller verschleißen als
gewöhnlich. Sie bieten Bastion Nord eine Lieferung seltener
Heilpflanzenextrakte an, die in der Bunker-Krankenstation zur Behandlung
einer grassierenden Atemwegserkrankung benötigt werden (siehe TICKET-007).
Der Rat der Sieben muss abwägen: Werkzeuge aus eigener Produktion
abzugeben schwächt kurzfristig die interne Versorgung, sichert aber eine
Ressource, die im Bunker selbst nicht herstellbar ist. Die Verhandlung läuft
über einen Aarbrücker Vermittler, der aufgrund seiner neutralen Reputation
(siehe Abschnitt 5) von beiden Seiten als Garant für eine faire, zeitgleiche
Übergabe akzeptiert wird ("Zug-um-Zug"-Mechanik, die das Risiko einseitigen
Vertragsbruchs strukturell reduziert, aber eine zusätzliche
Vermittlungsgebühr in Aarbrücker Talons kostet und damit einen klaren,
spielerisch nachvollziehbaren Kompromiss zwischen Sicherheit und Kosten
darstellt). Solche Aarbrücker
Treuhandgeschäfte sollen als eigene, gegenüber direktem Fraktionshandel
sicherere, aber teurere Handelsoption im System verankert werden.

## 12. Referenzpreise und Marktinformation

Der Aarbrücker Marktplatz führt öffentlich einsehbare Referenzpreise für die
gängigsten Güter, die als Ankerpunkt für Verhandlungen in der gesamten
Region dienen, auch wenn tatsächliche Geschäfte oft davon abweichen. Diese
Referenzpreise werden wöchentlich aktualisiert und spiegeln die zuletzt
gehandelten Mengen wider. Spieler, die keinen direkten Zugang zu
Aarbrück haben (etwa weil ein Pass gesperrt ist), operieren mit veralteten
oder unvollständigen Preisinformationen – ein bewusst eingebauter
Informationsnachteil, der Handelsposten und Kundschafter (verbunden mit dem
"Drittes Auge"-Vorteil der Mutierten, siehe TICKET-001) zu einer wertvollen
strategischen Investition macht. Gerüchte und verzögerte, teils unzuverlässige
Marktinformationen aus entfernten Regionen sollen zusätzlich als eigenes
Spielelement (z. B. über reisende Händler oder Kundschafter) eingeführt
werden, um Informationsasymmetrie als Handelsvorteil erlebbar zu machen.

## 13. Die Rolle des Spielers im Handelssystem

Der Spieler agiert nicht als allwissender Marktteilnehmer, sondern als
Entscheidungsträger einer der drei Fraktionen (oder in späteren
Erweiterungen ggf. einer kleineren Splittergruppe), der über
Handelsvertreter, Karawanenführer und diplomatische Gesandte indirekt auf
den Markt einwirkt. Zentrale Entscheidungen umfassen: welche Güter in
welcher Menge zum Handel freigegeben werden, mit welchen Fraktionen
langfristige Verträge angestrebt werden, wie viel Risiko bei
Karawanentransporten akzeptiert wird, und wie auf Embargos oder
Vertragsbrüche anderer Fraktionen reagiert wird (Vergeltung, Verhandlung,
Ignorieren). Diese Entscheidungen wirken sich verzögert, aber spürbar auf
Reputation, Preise und letztlich auf die politische Gesamtlage der Region
aus, wodurch das Handelssystem zum zentralen, mit Wirtschaft, Diplomatie und
Militär eng verzahnten Steuerungsinstrument des Simulators wird.

## 14. Saisonale Handelsereignisse und Zufallsvarianten

Aufbauend auf den saisonalen Zyklen aus TICKET-003 sind für das
Handelssystem eigene, zufallsgesteuerte Ereignisse vorgesehen, die die
Grundmechanik lebendig halten: verspätete Karawanen durch überraschenden
Frühschnee, ein plötzlich auftauchender Fernhändler aus den in TICKET-002
erwähnten unkartierten Ostebenen mit exotischen, einmalig verfügbaren
Gütern, oder ein Gerücht über eine bevorstehende Passsperrung, das
kurzfristig zu panikartigem Vorratskauf und entsprechend verzerrten Preisen
führt. Solche Ereignisse sollen nicht rein kosmetisch sein, sondern echte
Entscheidungssituationen erzeugen: Vertraut man dem Gerücht und kauft
vorsorglich teuer ein, oder riskiert man, bei tatsächlicher Sperrung ohne
ausreichende Vorräte dazustehen? Diese Art von Unsicherheit ist bewusst
gewählt, um Handel als aktive, unter Unsicherheit zu treffende
Managemententscheidung zu gestalten und nicht als passives Zuteilungssystem.

## 15. Grenzen des Handelssystems

Nicht alles soll handelbar sein: Bunkerinterne Sicherheitstechnologie,
militärisches Kerngerät und lebenswichtige Infrastruktur (etwa die
Staudammturbinen selbst) sind explizit vom freien Handel ausgeschlossen und
nur über politische Vereinbarungen (siehe TICKET-005, Diplomatie) zugänglich.
Diese Einschränkung verhindert, dass Spieler kritische strategische Vorteile
allein durch überlegenes Handelsgeschick erlangen, und erzwingt, dass
militärische und diplomatische Systeme eigenständige, nicht rein
wirtschaftlich lösbare Herausforderungen bleiben.

## 17. Zusammenfassung der Kernidee

Im Kern soll das Handelssystem vermitteln, dass Wirtschaft in dieser
Endzeitwelt nie ein rein technisches Optimierungsproblem ist, sondern immer
auch eine Frage von Vertrauen, Geografie und politischem Timing. Wer die
saisonalen Zyklen, die Reputationsmechanik und die strukturellen
Abhängigkeiten der drei Fraktionen versteht, kann durch klugen Handel
Vorteile erzielen, die kein militärisches System allein bieten kann – und
riskiert zugleich, durch Gier, Nachlässigkeit oder schlechtes Timing
genau diese Vorteile wieder zu verspielen. Dieses Wechselspiel aus
wirtschaftlicher Chance und politischem Risiko ist die eigentliche
Spielerfahrung, die das Handelssystem liefern soll, und bildet die
Brücke zum nachfolgenden Kampf- und Diplomatiesystem.

## Offene Punkte für Folgetickets

- Konkrete Formeln für Preisbildung, Knappheitsmultiplikatoren und
  Reputationsauf-/-abschläge (Balancing-Iteration).
- UI-Darstellung von Marktpreisen, Verträgen und Karawanenrouten
  (TICKET-008).
- Detaillierte Eskalationsstufen von Embargo zu offenem Konflikt
  (TICKET-005).

## 16. Langfristige Handelsmuster und Spielerstrategien

Über eine längere Spielsitzung hinweg sollen sich erkennbare, aber nicht
starre Handelsmuster etablieren: Eine Fraktion, die frühzeitig in den Ausbau
von Karawanenschutz investiert, kann riskantere, aber lukrativere Routen
durch den Grauwald nutzen; eine Fraktion, die gezielt Vertrauen bei allen
Handelspartnern aufbaut, profitiert von günstigeren Kreditkonditionen in
Krisenzeiten; eine Fraktion, die stattdessen auf kurzfristigen Vorteil durch
Vertragsbrüche setzt, gewinnt kurzfristig, verliert aber mittelfristig den
Zugang zu stabilen, günstigen Lieferbeziehungen. Keines dieser Muster soll
strikt überlegen sein – vielmehr sollen unterschiedliche Spielerstile
(kooperativ-aufbauend, opportunistisch-risikofreudig, isolationistisch-
autark) jeweils eigene, plausible wirtschaftliche Konsequenzen haben, die
sich aus dem Zusammenspiel der in diesem Ticket beschriebenen Mechaniken
ergeben.

## Akzeptanzkriterien

- [x] Drei Handelsebenen (lokal, regional, strategisch) definiert.
- [x] Preisbildungslogik inkl. Knappheit, Transportkosten und Reputation
      beschrieben.
- [x] Embargo-Mechanik mit konkretem Beispiel ausgearbeitet.
- [x] Vertrauens-/Reputationssystem definiert.
- [x] Rolle Aarbrücks als neutraler Handelsknoten beschrieben.
- [x] Handelsgüterprofile je Fraktion tabellarisch dargestellt.
- [x] Umfang mindestens 2000 Wörter.
