# TICKET-008: UI/UX-Konzept für den Simulator

**Typ:** Design / Produktdesign
**Epic:** Endzeit-Wirtschaftssimulator – Präsentation & Bedienbarkeit
**Priorität:** Mittel
**Status:** Entwurf fertig, bereit für Review
**Abhängigkeiten:** TICKET-001 bis TICKET-007 (alle inhaltlichen Systeme)
**Blockiert:** Keine (letztes Ticket der ersten Design-Runde)

## Ziel des Tickets

Definition eines UI/UX-Grundkonzepts für den Simulator, das die in
TICKET-001 bis TICKET-007 beschriebenen Systeme (Fraktionen, Karte,
Wirtschaft, Handel, Konflikt, Technologie, Bevölkerung) verständlich,
stimmungsvoll und ohne Reduzierung auf reine Zahlenlisten zugänglich macht.
Der visuelle und interaktive Stil soll die realistische, geerdete Tonalität
des Settings (siehe TICKET-001: kein Sci-Fi-Glanz) konsequent widerspiegeln.

## 1. Gestalterische Grundhaltung

Die Oberfläche soll bewusst **analog-diegetisch** wirken: keine glatten,
futuristischen Hologramm-Interfaces, sondern eine Ästhetik aus
Papierkarten, handschriftlichen Notizen, mechanischen Anzeigen und
abgenutzten Materialien, passend zur in TICKET-002 (Abschnitt 14) bereits
angelegten Idee unterschiedlich gestalteter Fraktionskarten. Farbpalette und
Typografie sollen sich je nach aktiver Fraktion leicht unterscheiden (siehe
Abschnitt 4), ohne die Wiedererkennbarkeit der Kernelemente zu verlieren.

## 2. Hauptbildschirme im Überblick

Der Simulator gliedert sich in fünf zentrale Bildschirme:

- **Kartenansicht**: die Hauptnavigationsebene, basierend auf der
  Region aus TICKET-002, mit Overlays für Kontamination, Handelsrouten,
  Bedrohungslagen und Fraktionsgrenzen.
- **Siedlungsübersicht**: Detailansicht einer einzelnen Siedlung/
  Bunkeranlage mit Produktionsgebäuden, Lagerbeständen (TICKET-003) und
  Bevölkerungsstatus (TICKET-007).
- **Handelsbildschirm**: Marktübersicht, aktive Verträge, Karawanenstatus
  und Reputationswerte (TICKET-004).
- **Rat-/Diplomatiebildschirm**: Übersicht über Beziehungen zu anderen
  Fraktionen, Eskalationsstufen (TICKET-005), laufende Verhandlungen und
  Bündnisoptionen.
- **Forschungsbildschirm**: Darstellung des Technologiebaums je Fraktion
  (TICKET-006) mit laufenden und abgeschlossenen Projekten.

## 3. Kartenansicht im Detail

Die Kartenansicht orientiert sich an der in TICKET-002 beschriebenen
Regionskarte des Talkessels und stellt standardmäßig eine vergilbte,
teilweise handannotierte Vorkriegskarte dar. Overlays lassen sich einzeln
ein- und ausblenden: ein Kontaminations-Overlay mit den vier Stufen aus
TICKET-002 (farblich abgestuft von neutral bis warnend), ein
Handelsroutebn-Overlay mit aktiven Karawanen und deren Risikostatus
(TICKET-004), sowie ein Bedrohungs-Overlay, das aktuelle Eskalationsstufen
zwischen Fraktionen (TICKET-005) an den betroffenen Grenzregionen
visualisiert. Wichtige Points of Interest (Staudamm, Lange Brücke,
Steinbrüche) sind als handgezeichnete Symbole markiert, deren Stil je nach
Perspektive (Bunker/Mutierte/Wastelander) variiert.

## 4. Fraktionsspezifische visuelle Sprache

Jede Fraktion erhält ein eigenes, aber kompatibles visuelles Thema:

- **Bunker-Überlebende**: kühle, technische Ästhetik – gedecktes Grau/Blau,
  klare Gitterlinien, Monospace-artige Typografie, nüchterne
  Statusanzeigen im Stil alter Kontrollraum-Displays.
- **Mutierte**: warme, organische Ästhetik – erdige Grün-/Brauntöne,
  handgezeichnete, leicht unregelmäßige Symbole, Typografie, die an
  handschriftliche Sippenaufzeichnungen erinnert.
- **Wastelander**: raue, improvisierte Ästhetik – gedeckte Beige-/
  Rosttöne, collagenartige Elemente aus wiederverwendetem
  Vorkriegsmaterial (Zeitungsausschnitte, Verpackungsreste als
  Hintergrundtextur), unregelmäßige, handgeschriebene Beschriftungen.

Diese visuelle Differenzierung dient nicht nur der Ästhetik, sondern hilft
Spielern, sich in Mehrfraktionen-Situationen (z. B. Verhandlungen) schnell
zu orientieren, aus wessen Perspektive gerade welche Information dargestellt
wird.

## 4.1 Konsistente Elemente über alle Fraktionen hinweg

Trotz dieser Differenzierung bleiben bestimmte strukturelle Elemente über
alle drei Fraktionen hinweg identisch positioniert und gleich bedienbar:
die Position der vier Bedürfnisstatusbalken (Abschnitt 5), der Aufbau der
Vertragskarten (Abschnitt 6) und die grundlegende Logik der
Eskalationsleiter-Anzeige (Abschnitt 7). Diese Konsistenz ist bewusst
gewählt, damit Spieler, die im Laufe einer Partie über Diplomatie oder
Migration Einblick in die Oberfläche einer anderen Fraktion erhalten (etwa
bei einem gemeinsamen Aarbrücker Handelsgeschäft), sich trotz der
unterschiedlichen visuellen Sprache sofort zurechtfinden, statt eine
komplett neue Bedienlogik erlernen zu müssen.

## 5. Siedlungsübersicht

Die Siedlungsübersicht zeigt Produktionsgebäude als einfache, ikonografische
Darstellungen (Mühle, Schmiede, Werkstatt) mit sichtbaren Warteschlangen und
Engpassindikatoren (z. B. ein visuelles Warnsymbol, wenn eine Produktionskette
aus TICKET-003 durch fehlende Zwischenprodukte blockiert ist). Bevölkerungs-
und Bedürfniswerte aus TICKET-007 werden als vier kompakte Statusbalken
(Nahrung, Gesundheit, Sicherheit, Moral) mit den in TICKET-007 (Abschnitt 10)
definierten vier Schwellenstufen dargestellt, farblich abgestuft von
neutral über angespannt und kritisch bis zum Zusammenbruch.

## 6. Handelsbildschirm

Der Handelsbildschirm zeigt eine Liste aktiver und potenzieller
Handelspartner mit aktuellem Reputationswert (TICKET-004, Abschnitt 5),
verfügbaren Referenzpreisen (sofern der Marktzugang zu Aarbrück besteht,
siehe TICKET-004, Abschnitt 12) sowie eine Übersicht laufender Karawanen mit
Risikostatus und geschätzter Ankunftszeit unter Berücksichtigung saisonaler
Einschränkungen (TICKET-002, Abschnitt 9). Verträge werden als einfache,
lesbare "Vertragskarten" dargestellt (Laufzeit, Menge, Preis, Strafklausel),
um die in TICKET-004 (Abschnitt 9) beschriebene Vertragslogik greifbar zu
machen, ohne den Spieler mit reinen Tabellen zu erschlagen.

## 7. Rat-/Diplomatiebildschirm

Dieser Bildschirm visualisiert die Beziehungen zu den jeweils anderen zwei
Fraktionen als einfache Eskalationsleiter-Anzeige (Spannung bis Belagerung,
siehe TICKET-005, Abschnitt 2), ergänzt um eine kurze Historie jüngster
Ereignisse (Raids, Vertragsbrüche, Verhandlungserfolge). Verhandlungsdialoge
werden als textbasierte, mehrstufige Interaktionen dargestellt, in denen der
Spieler konkrete Angebote (Tribut, Gebietskompromiss, Waffenstillstand)
formulieren kann, mit klar sichtbaren Auswirkungen auf Reputation und
Eskalationsstufe vor Bestätigung der Entscheidung.

## 8. Forschungsbildschirm

Der Forschungsbaum wird nicht als abstrakter Graph, sondern als
begehbare "Werkstatt-/Archiv-Metapher" dargestellt: Bunker-Spieler sehen
ein Archivregal mit Aktenordnern je Technologie-Tier (TICKET-006, Abschnitt
2), Mutierten-Spieler eine Art Sippen-Wandbehang mit verknüpften Symbolen,
Wastelander-Spieler ein Pinnwand-artiges Sammelsurium aus Notizzetteln und
Skizzen. Diese Metaphern greifen die kulturelle Einbettung von Technologie
aus TICKET-006 (Abschnitt 13) auf und machen den in TICKET-006 (Abschnitt
10) beschriebenen Technologiekatalog visuell erkundbar statt nur listenartig
abzubilden.

## 8.1 Darstellung von Forschungsfehlschlägen

Da Forschung, insbesondere im Mutierten-Pfad, auch scheitern kann (siehe
TICKET-006, Abschnitt 8), muss die Oberfläche Fehlschläge ebenso klar wie
Erfolge kommunizieren: ein sichtbar "durchgestrichener" Eintrag im
jeweiligen Werkstatt-/Archiv-Element, ergänzt um eine kurze, atmosphärische
Erklärung im Benachrichtigungsprotokoll (Abschnitt 9), etwa einen Bericht
über eine missglückte Kundschaftertour in die Kraterzone. Diese
transparente Darstellung von Rückschlägen soll verhindern, dass Spieler
Fehlschläge als reine Bestrafung ohne erzählerischen Kontext wahrnehmen,
sondern sie als Teil einer glaubwürdigen, manchmal unglücklich verlaufenden
Simulation begreifen, die zum Gesamtton des Settings passt.

## 9. Ereignis- und Benachrichtigungssystem

Ereignisse (Epidemien, Preisschocks, Raids, Migrationswellen,
Mutationsmanifestationen bei Geburten) werden über ein zentrales
Benachrichtigungsprotokoll kommuniziert, das kurze, atmosphärische
Textmeldungen im Stil eines Logbuchs oder Boten-Berichts nutzt, statt reiner
Systemmeldungen ("Ein Bote aus Aarbrück berichtet von steigenden
Getreidepreisen im Osthügelland" statt "Preis Getreide +15%"). Wichtige,
handlungsrelevante Ereignisse werden zusätzlich prominent hervorgehoben und
verlinken direkt zum betroffenen Bildschirm (z. B. ein Raid-Ereignis
verlinkt zum betroffenen Kartenausschnitt und Verteidigungsstatus).

## 10. Onboarding und Zugänglichkeit

Angesichts der Systemtiefe (sieben eng verzahnte Kernsysteme) ist ein
schrittweises Onboarding vorgesehen: Neue Spieler beginnen mit einer
einzelnen Fraktion und einer reduzierten Kartenansicht (nur die unmittelbare
Nachbarregion), bevor nach und nach Handels-, Diplomatie- und
Forschungssysteme freigeschaltet werden. Für Barrierefreiheit sind
farbenblindfreundliche Statusfarben (zusätzliche Symbole neben reiner
Farbkodierung bei den Bedürfnisschwellen aus Abschnitt 5), skalierbare
Textgrößen und optionale reduzierte Bewegungseffekte vorgesehen.

## 10.1 Kontextsensitive Hilfe

Über die anfängliche Tutorial-Sequenz aus Abschnitt 14 hinaus soll jederzeit
eine kontextsensitive Hilfe verfügbar sein: Ein Klick auf ein beliebiges
Statuselement (etwa einen Bedürfnisbalken aus Abschnitt 5 oder eine
Eskalationsstufe aus Abschnitt 7) öffnet eine kurze, in der jeweiligen
Fraktionssprache aus Abschnitt 4 gehaltene Erklärung, was der Wert bedeutet
und welche der in den vorherigen Tickets beschriebenen Mechaniken ihn
beeinflussen. Dies reduziert die Notwendigkeit, Spielmechaniken auswendig
zu lernen, und unterstützt insbesondere den in Abschnitt 17 beschriebenen
Beobachtungsmodus für erfahrene Spieler, die gezielt tiefer in einzelne
Systeme eintauchen möchten, ohne dabei den atmosphärischen Grundton der
Oberfläche zu verlassen.

## 11. Zusammenspiel mit anderen Tickets

Dieses Ticket bildet den visuellen und interaktiven Abschluss der ersten
Design-Runde: Es übersetzt die Inhalte aus TICKET-001 (Fraktionsidentität),
TICKET-002 (Karte), TICKET-003 (Wirtschaft), TICKET-004 (Handel), TICKET-005
(Konflikt), TICKET-006 (Technologie) und TICKET-007 (Bevölkerung) in konkret
erlebbare, konsistente Bildschirme und Interaktionsmuster, ohne inhaltlich
neue Spielmechaniken einzuführen.

## 12. Beispielhafter Nutzerfluss: Ein Handelsgeschäft abschließen

Um die Bildschirme konkret zu verzahnen, ein durchgespielter Nutzerfluss:
Der Spieler bemerkt auf der Kartenansicht (Abschnitt 3) ein
Preisschock-Symbol nahe Bastion Nord, ausgelöst durch eine Werkzeugknappheit
(siehe TICKET-004, Abschnitt 3). Ein Klick auf das Symbol öffnet direkt den
Handelsbildschirm (Abschnitt 6) mit vorausgewähltem Handelspartner. Der
Spieler sieht den aktuellen Reputationswert, die zuletzt bei Aarbrück
notierten Referenzpreise (TICKET-004, Abschnitt 12) und schlägt ein Angebot
vor: Getreide gegen Werkzeuge. Das System zeigt in Echtzeit eine Vorschau der
zu erwartenden Reputationsänderung und der ungefähren Transportzeit über den
betroffenen Pass, unter Berücksichtigung der aktuellen Jahreszeit
(TICKET-002, Abschnitt 9). Bestätigt der Spieler das Angebot, wechselt die
Ansicht zu einer kompakten Vertragskarte (Abschnitt 6), die im
Handelsbildschirm dauerhaft sichtbar bleibt, bis die Karawane ihr Ziel
erreicht oder ein Ereignis (Überfall, Verzögerung) eintritt, das über das
Benachrichtigungssystem (Abschnitt 9) gemeldet wird.

## 13. Interaktionsmuster und Feedback

Alle zentralen Interaktionen (Vertragsabschluss, Diplomatiezusage,
Forschungsstart, Bautätigkeit) folgen demselben dreistufigen Muster:
Auswahl der Handlungsoption, Vorschau der unmittelbaren und mittelbaren
Konsequenzen (Ressourcenkosten, Reputationsänderung, Zeitaufwand), sowie
eine explizite Bestätigung, bevor die Handlung ausgeführt wird. Dieses
Muster soll verhindern, dass Spieler versehentlich folgenreiche
Entscheidungen (etwa ein Embargo, siehe TICKET-004, Abschnitt 4, oder eine
Kriegserklärung, siehe TICKET-005) ohne volles Verständnis der Konsequenzen
auslösen. Rückmeldungen nach Abschluss einer Handlung erfolgen sowohl
visuell (kurze Animation, Statusänderung) als auch textuell über das
Benachrichtigungsprotokoll (Abschnitt 9), um unterschiedliche
Wahrnehmungspräferenzen der Spieler zu bedienen.

## 14. Tutorial- und Lernkurve

Aufbauend auf dem Onboarding-Konzept aus Abschnitt 10 ist ein konkretes
Tutorial vorgesehen, das den Spieler in einer vereinfachten, risikofreien
Startsituation Schritt für Schritt durch die Kernbildschirme führt: zunächst
Siedlungsübersicht und Grundproduktion (TICKET-003), dann ein erstes,
begleitetes Handelsgeschäft mit einer freundlich gesinnten Nachbarsiedlung
(TICKET-004), gefolgt von einer ersten, risikoarmen Forschungsentscheidung
(TICKET-006) und schließlich einer Einführung in die
Bedürfnisstatusanzeigen (TICKET-007). Diplomatie- und Konfliktsysteme
(TICKET-005) werden bewusst erst nach dieser Grundeinführung freigeschaltet,
da sie am stärksten von einem Verständnis der übrigen Systeme abhängen.

## 15. Plattform- und Eingabeüberlegungen

Das Interface wird primär für Maus-/Tastatur-Bedienung an Desktop-Systemen
konzipiert, mit einer klaren Informationsdichte, die sich aber auf
Tablet-Formate übertragen lassen soll (größere Touch-Ziele, ausklappbare statt
permanent sichtbare Detailpanels). Die Kartenansicht (Abschnitt 3) nutzt
Zoom- und Pan-Gesten, die sowohl über Maus/Scrollrad als auch über
Touch-Eingaben funktionieren, während komplexere Bildschirme wie der
Forschungsbaum (Abschnitt 8) auf kleineren Bildschirmen in eine vereinfachte
Listendarstellung wechseln, um die charakteristischen Werkstatt-/Archiv-
Metaphern nicht durch Überladung unleserlich zu machen.

## 16. Akustische Gestaltung als UX-Signal

Auch ohne ein eigenes, vollständiges Soundkonzept (das als separates Ticket
vorgesehen ist, siehe Offene Punkte) soll die Oberfläche bereits jetzt
grundlegende akustische Leitplanken berücksichtigen: Jede Fraktion erhält
charakteristische, dezente Interface-Klänge, die zur visuellen Sprache aus
Abschnitt 4 passen – mechanisches Klicken und Relais-Geräusche für
Bunker-Oberflächen, organische, gedämpfte Klänge für Mutierten-Oberflächen,
raue, materialbetonte Geräusche (Papier, Metallschrott) für
Wastelander-Oberflächen. Wichtige Warnungen (kritische Bedürfnisschwellen aus
TICKET-007, Abschnitt 10, oder eskalierende Konflikte aus TICKET-005)
erhalten zusätzlich ein fraktionsübergreifend einheitliches akustisches
Warnsignal, damit Spieler kritische Situationen auch bei geteilter
Aufmerksamkeit zuverlässig wahrnehmen.

## 17. Schwierigkeitsgrade und Anpassbarkeit

Um sowohl Einsteigern als auch erfahrenen Spielern gerecht zu werden, sind
grundlegende Schwierigkeitsoptionen vorgesehen, die primär die
Geschwindigkeit von Krisenereignissen (Knappheitsschocks aus TICKET-004,
Epidemien aus TICKET-007, Eskalationsgeschwindigkeit aus TICKET-005)
sowie die Großzügigkeit der Ausgangsressourcen beeinflussen, ohne die
grundsätzliche Systemtiefe zu verändern. Zusätzlich soll ein optionaler
"Beobachtungsmodus" existieren, der detailliertere Zahlenwerte hinter den
in den vorherigen Abschnitten beschriebenen atmosphärischen Textmeldungen
und Statusbalken offenlegt – gedacht für Spieler, die ein tieferes
Verständnis der zugrunde liegenden Simulationsmechanik suchen, ohne dass
diese Detailtiefe neuen Spielern von Anfang an aufgezwungen wird.

## 18. Testkriterien für die UI-Umsetzung

Für die spätere Umsetzung dieses Konzepts gelten folgende qualitative
Testkriterien: Ein neuer Spieler soll nach dem Tutorial (Abschnitt 14) ohne
externe Erklärung ein einfaches Handelsgeschäft abschließen können; ein
erfahrener Spieler soll den Zustand aller vier Bedürfniskategorien
(TICKET-007) einer Siedlung binnen weniger Sekunden über die
Statusbalken aus Abschnitt 5 erfassen können; und die fraktionsspezifische
visuelle Sprache aus Abschnitt 4 soll auch ohne Beschriftung eindeutig
erkennbar sein, wenn Spieler zwischen Bildschirmen unterschiedlicher
Fraktionen wechseln. Diese Kriterien dienen als Grundlage für spätere
Usability-Tests, sobald erste Umsetzungen der beschriebenen Bildschirme
vorliegen.

## 20. Ausblick

Sollten die in TICKET-002 (Abschnitt 13) angedeuteten Erweiterungsflächen
später spielbar werden, ist das hier beschriebene UI-Grundgerüst bewusst so
gestaltet, dass zusätzliche Fraktionen eigene visuelle Themen nach demselben
Muster aus Abschnitt 4 erhalten können, ohne die fünf Hauptbildschirme aus
Abschnitt 2 strukturell verändern zu müssen.

## Offene Punkte für Folgetickets

- Konkrete Wireframes/Mockups je Bildschirm (nächste Design-Iteration).
- Detaillierte Interaktionsflüsse für Verhandlungsdialoge.
- Soundkonzept und Musikuntermalung je Fraktion (neues Ticket erforderlich).

## 19. Zusammenfassung der Designabsicht

Das UI/UX-Konzept soll insgesamt sicherstellen, dass die erhebliche
inhaltliche Tiefe der Tickets 001 bis 007 – Weltgeschichte, Geografie,
Wirtschaft, Handel, Konflikt, Technologie und Bevölkerung – nicht in einer
unübersichtlichen Ansammlung von Zahlen und Tabellen versinkt, sondern über
konsistente, atmosphärisch gestaltete Bildschirme erlebbar bleibt. Jede
Designentscheidung in diesem Ticket – von der Kartenästhetik über die
fraktionsspezifische visuelle Sprache bis zur Tutorial-Reihenfolge – ist
bewusst so gewählt, dass sie die realistische, geerdete Tonalität des
Settings aus TICKET-001 unterstützt, statt sie durch glatte, generische
Aufbauspiel-Optik zu untergraben.

## Akzeptanzkriterien

- [x] Fünf Hauptbildschirme definiert und inhaltlich mit den
      vorherigen Tickets verknüpft.
- [x] Fraktionsspezifische visuelle Sprache für alle drei Fraktionen
      ausgearbeitet.
- [x] Ereignis- und Benachrichtigungssystem im atmosphärischen Stil
      beschrieben.
- [x] Onboarding- und Zugänglichkeitskonzept enthalten.
- [x] Umfang mindestens 2000 Wörter.
