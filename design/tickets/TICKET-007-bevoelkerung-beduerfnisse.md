# TICKET-007: Bevölkerungs- & Bedürfnissystem (Nahrung, Gesundheit, Moral)

**Typ:** Design / Gameplay-System
**Epic:** Endzeit-Wirtschaftssimulator – Bevölkerung & Soziales
**Priorität:** Mittel-Hoch
**Status:** Entwurf fertig, bereit für Review
**Abhängigkeiten:** TICKET-001 (Fraktionen), TICKET-003 (Ressourcen/Nahrung/
Medizin), TICKET-005 (Konfliktverluste), TICKET-006 (Medizintechnologie)
**Blockiert:** TICKET-008 (Darstellung von Bevölkerungs- und
Bedürfniswerten in der UI)

## Ziel des Tickets

Definition des Bevölkerungs- und Bedürfnissystems: wie Bevölkerung wächst
oder schrumpft, welche Grundbedürfnisse (Nahrung, Gesundheit, Sicherheit,
Moral/Kultur) befriedigt werden müssen, wie sich die in TICKET-001
beschriebenen fraktionsspezifischen demografischen Eigenheiten (geringe
Bunker-Bevölkerung, gesundheitlich belastete Mutierten-Bevölkerung, große,
aber verwundbare Wastelander-Bevölkerung) mechanisch niederschlagen, und wie
Migration zwischen Fraktionen funktioniert.

## 1. Grundprinzip: Bevölkerung als knappste Ressource

Anders als Rohstoffe oder Werkzeuge lässt sich Bevölkerung nicht handeln
oder schnell nachproduzieren – sie wächst langsam, braucht Generationen für
signifikante Veränderung und ist die eigentliche Grundlage aller
Produktions-, Forschungs- und Militärkapazität der vorherigen Tickets. Das
System soll vermitteln, dass jeder Bevölkerungsverlust (durch Hunger,
Krankheit, Konflikt) eine langfristige, nicht kurzfristig kompensierbare
Schwächung bedeutet, während Bevölkerungswachstum eine der wertvollsten,
aber am schwersten zu beeinflussenden strategischen Ressourcen darstellt.

## 2. Grundbedürfnisse

Jede Siedlung/Fraktion verwaltet vier zentrale Bedürfniskategorien:

- **Nahrung**: unmittelbar aus TICKET-003 abgeleitet; Mangel führt zu
  Hunger, sinkender Arbeitsproduktivität und im Extremfall zu
  Hungertoten.
- **Gesundheit**: abhängig von Medizinversorgung (TICKET-003),
  Wohnverhältnissen und Kontaminationsbelastung (TICKET-002); Mangel führt
  zu erhöhter Krankheits- und Sterblichkeitsrate.
- **Sicherheit**: abhängig von Verteidigungsstärke und Konflikthäufigkeit
  (TICKET-005); Mangel führt zu Angst, sinkender Moral und im Extremfall zu
  Abwanderung.
- **Moral/Kultur**: abhängig von sozialem Zusammenhalt, Erfolgserlebnissen
  (Forschungserfolge, Konfliktsiege, gute Ernten) und der in TICKET-001
  beschriebenen kulturellen Identität jeder Fraktion; Mangel führt zu
  innerer Unruhe, sinkender Produktivität und im Extremfall zu Aufständen
  oder Abspaltungen.

## 3. Bevölkerungswachstum je Fraktion

**Bunker-Überlebende**: geringes, stark limitiertes Wachstum durch
begrenzten physischen Raum und strikte Zuteilungspolitik (Geburten werden
teils bewusst durch den Rat der Sieben reguliert, um die Tragfähigkeit der
Anlage nicht zu überschreiten). Bevölkerungswachstum ist hier eine
politische Entscheidung, kein rein biologischer Prozess.

**Mutierte**: mittleres, aber durch gesundheitliche Belastung gebremstes
Wachstum (siehe TICKET-001: erhöhte Kindersterblichkeit,
Autoimmunerkrankungen, Fruchtbarkeitsprobleme). Zusätzlich variiert die
Ausprägung neuer Mutationen bei Nachkommen, was ein eigenes, im nächsten
Abschnitt beschriebenes Ereignis darstellt.

**Wastelander**: höchstes natürliches Wachstum unter den drei Fraktionen,
aber auch höchste Verwundbarkeit gegenüber Hungersnöten, Epidemien und
Konfliktverlusten – ein klassisches Muster hoher Geburten- bei ebenfalls
hoher Sterblichkeitsrate, wie es historisch in vielen agrarisch geprägten
Nachkriegsgesellschaften zu beobachten war.

## 4. Mutationsmanifestation bei Nachkommen

Ein spezifisches Ereignis für die Mutierten-Fraktion: Bei der Geburt eines
Kindes wird (basierend auf den Mutationsausprägungen der Elternteile sowie
der Kontaminationsbelastung der Umgebung) bestimmt, ob und welche Mutation
sich zeigt – "Drittes Auge", "Dritte Hand", eine der selteneren
Ausprägungen (siehe TICKET-001, Abschnitt 4), eine gesundheitlich
belastende Komplikation ohne funktionalen Vorteil, oder gar keine sichtbare
Mutation. Dieses Ereignis soll bewusst nicht vom Spieler direkt steuerbar
sein, sondern als Zufallselement mit spürbaren, aber nicht deterministischen
Wahrscheinlichkeiten funktionieren, um die in TICKET-001 beschriebene
biologische Unvorhersehbarkeit glaubwürdig abzubilden.

## 5. Krankheit und Epidemien

Krankheit ist ein eigenständiges Subsystem: Grundkrankheitsrisiko steigt bei
unzureichender Ernährung, schlechten Wohnverhältnissen (Überbelegung in
Bunkern) oder Kontaminationsexposition. Einzelne Krankheitsfälle können sich
bei unzureichender medizinischer Versorgung zu regionalen Epidemien
ausweiten, die mehrere Siedlungen gleichzeitig betreffen und temporär
Arbeitskraft binden, Handel einschränken (siehe TICKET-004: gesunkene
Karawanenkapazität) und militärische Kampfbereitschaft schwächen (siehe
TICKET-005: Moral- und Personalverluste). Ein historisches Beispiel aus dem
Weltbau (siehe TICKET-001: Cholera-Welle in Flüchtlingslagern nach dem
Krieg) dient als narrative Vorlage für spätere, spielinterne Epidemieereignisse.

## 6. Migration zwischen Fraktionen

Bevölkerung ist nicht vollständig an eine Fraktion gebunden. Einzelpersonen
oder kleine Gruppen können abwandern, wenn Bedürfnisse in der eigenen
Fraktion chronisch unerfüllt bleiben und eine andere Fraktion attraktiver
erscheint: ein hungernder Wastelander-Bauer, der versucht, in einem Bunker
aufgenommen zu werden; ein unzufriedener Bunker-Bewohner, der die strikte
Hierarchie satt hat und zu den freieren Wastelander-Siedlungen wechselt;
ein junger Mutierter ohne funktionale Mutation, der sich in der eigenen
Sippe unterlegen fühlt und bei den Wastelandern ein unauffälligeres Leben
sucht. Migration ist dabei nie garantiert erfolgreich: Aufnahme hängt von
der Reputation des Migranten, dem aktuellen Bedarf der Zielfraktion und
gesellschaftlichen Vorurteilen ab (siehe TICKET-001, Abschnitt 6: kulturelle
Distanz und Stigmatisierung).

## 6.1 Aufnahmekriterien und Ablehnung

Ob ein Migrant tatsächlich aufgenommen wird, hängt nicht nur vom
allgemeinen Bedarf der Zielfraktion ab, sondern auch von individuellen
Kriterien: Bunkeranlagen bevorzugen Personen mit nachweisbaren technischen
oder medizinischen Fähigkeiten und lehnen Bewerber ohne verwertbare
Qualifikation häufig ab, was historisch bereits bei der ursprünglichen
Bunkerauswahl (siehe TICKET-001, Abschnitt 3) zu Ungerechtigkeitsempfinden
geführt hat und sich in der Gegenwart des Spiels fortsetzt. Mutierten-Sippen
prüfen eher die Bereitschaft, sich in bestehende Verpflichtungsstrukturen
(siehe TICKET-003, Abschnitt 5: Sippen-Tauschehre) einzufügen, während
Wastelander-Siedlungen am ehesten spontane Aufnahme gewähren, dafür aber im
Ernstfall auch am schnellsten wieder ausgrenzen, wenn Ressourcenknappheit
herrscht und die Gemeinschaft das Gefühl bekommt, mehr Mäuler zu ernähren,
als sie an zusätzlicher Arbeitskraft oder Sicherheit gewinnt.

## 7. Arbeitskraftallokation

Die verfügbare, gesunde und ausreichend ernährte Bevölkerung wird auf
Produktions- (TICKET-003), Forschungs- (TICKET-006) und
Verteidigungsaufgaben (TICKET-005) verteilt. Diese Allokation ist eine
zentrale strategische Entscheidung: Mehr Arbeitskraft in der Nahrungsmittelproduktion
reduziert kurzfristiges Hungerrisiko, aber auch die verfügbare Kapazität für
Forschung oder Militär. Sinkt die Bedürfnisbefriedigung (siehe Abschnitt 2)
unter kritische Werte, sinkt automatisch auch die Produktivität der
verbleibenden Arbeitskraft – ein sich selbst verstärkender negativer Kreislauf,
dem Spieler durch rechtzeitige Gegenmaßnahmen (Nahrungsimporte,
Gesundheitsinvestitionen) entgegenwirken müssen.

## 7.1 Spezialisierung durch Mutationsverteilung

Wie in TICKET-003 (Abschnitt 10) angedeutet, beeinflusst die individuelle
Mutationsausprägung bei den Mutierten direkt die sinnvollste
Arbeitseinsatzentscheidung. Ein Sippenältester, der die Zuteilung neuer
Arbeitskräfte plant, muss abwägen, ob ein Träger des Dritten Auges besser als
Wächter am Südsteig, als Kräutersammler im Bittergraben oder als Kundschafter
für neue Kraterpfade (siehe TICKET-006, Abschnitt 11) eingesetzt wird – eine
Entscheidung, die je nach aktueller Bedrohungslage (TICKET-005) und
Forschungspriorität (TICKET-006) unterschiedlich ausfallen sollte und damit
den Mutierten-Spielstil von den stärker zentral geplanten Bunkern und der
familiär organisierten Wastelander-Arbeitsteilung spürbar unterscheidbar
hält und Spielern eine eigene, mutationsbewusste Personalplanung abverlangt.

## 8. Moral, Kultur und gesellschaftlicher Zusammenhalt

Moral wirkt nicht nur auf Produktivität, sondern auch auf politische
Stabilität: Anhaltend niedrige Moral in einem Bunker kann zu offenen
Spannungen zwischen alteingesessenen und später aufgenommenen Bewohnern
führen (siehe TICKET-001, Abschnitt 3: "Nachzügler"-Konflikt), anhaltend
niedrige Moral bei den Wastelandern kann zu Abspaltung einzelner Dörfer vom
Ältestenrat führen, und anhaltend niedrige Moral bei den Mutierten kann
bestehende Spannungen zwischen unterschiedlichen religiösen Deutungen der
Mutation (siehe TICKET-001, Abschnitt 4) eskalieren lassen. Diese
gesellschaftlichen Bruchlinien sind bewusst als potenzielle interne
Krisenherde angelegt, unabhängig von externen Konflikten mit den anderen
Fraktionen.

## 8.1 Interne Führungskrisen

Anhaltend niedrige Moral kann sich nicht nur diffus, sondern in konkreten
Führungskrisen entladen: ein Misstrauensvotum gegen ein Mitglied des Rats
der Sieben, ein Bruch zwischen zwei rivalisierenden Ältesten einer
Wastelander-Siedlung, oder eine Abspaltung einer unzufriedenen Sippe von
einem größeren Mutierten-Verband. Solche internen Krisen sollen als eigene
Ereignisklasse neben den zwischenfraktionellen Konflikten aus TICKET-005
existieren und verdeutlichen, dass Bedrohungen für eine Fraktion nicht nur
von außen, sondern auch aus ungelöster innerer Unzufriedenheit entstehen
können.

## 9. Zusammenspiel mit anderen Systemen

Das Bevölkerungssystem ist die zentrale Verbindungsstelle zwischen
Wirtschaft (TICKET-003: Nahrungs-/Medizinbedarf), Handel (TICKET-004:
Versorgungssicherheit über Importe), Konflikt (TICKET-005: Verluste,
Moralwirkung von Kriegen) und Technologie (TICKET-006: Forschungskapazität
abhängig von qualifizierter Bevölkerung). Es liefert zudem die
demografische Grundlage für alle in TICKET-001 beschriebenen
gesellschaftlichen Eigenheiten der drei Fraktionen und macht sie im
laufenden Spiel konkret erlebbar.

## 10. Bedürfnisschwellen und abgestufte Auswirkungen

Jedes Grundbedürfnis wird nicht binär (erfüllt/nicht erfüllt), sondern in
abgestuften Schwellenwerten modelliert, die unterschiedlich starke
Konsequenzen auslösen:

| Stufe | Nahrung | Gesundheit | Sicherheit | Moral |
|---|---|---|---|---|
| Gut versorgt | Vorräte für >3 Monate | Medizin ausreichend, keine Epidemie | Keine akuten Bedrohungen | Erfolge überwiegen |
| Angespannt | Vorräte für 1-3 Monate | Medizinknappheit, erhöhte Krankheitsrate | Gelegentliche Grenzzwischenfälle | Gemischte Stimmung |
| Kritisch | Vorräte <1 Monat, Rationierung | Spürbare Übersterblichkeit | Aktive Bedrohung/Belagerung | Unruhe, erste Abwanderung |
| Zusammenbruch | Hungertote | Unkontrollierte Epidemie | Dauerhafter Kriegszustand | Aufstand/Abspaltung droht |

Diese Abstufung ermöglicht es, frühzeitig Warnsignale zu erkennen und
gegenzusteuern, bevor eine Fraktion in die kritische oder gar
Zusammenbruchsphase eines Bedürfnisses gerät, und liefert der UI (TICKET-008)
klare visuelle Ankerpunkte für Statusanzeigen.

## 11. Alterung, Ausbildung und Generationenwechsel

Bevölkerung wird nicht als homogene Masse behandelt, sondern grob nach
Altersgruppen (Kinder, Erwachsene, Alte) gegliedert, die unterschiedliche
Rollen im Wirtschafts- und Bedürfnissystem einnehmen. Kinder benötigen
Betreuung und Ausbildung, bevor sie als volle Arbeitskraft zur Verfügung
stehen (siehe TICKET-003, Abschnitt 10: Zunftausbildung in Bunkern,
Mutations-basierte Lehrlingsausbildung bei den Sippen, familiäre
Wissensweitergabe bei den Wastelandern); Alte tragen weniger körperliche
Arbeitskraft, aber wertvolles Erfahrungswissen bei, dessen Verlust (durch
Tod ohne Nachfolgeausbildung) spürbare Auswirkungen auf Produktions- oder
Forschungsqualität haben kann (siehe TICKET-006, Abschnitt 10: Wissensverlust
durch fehlende Nachfolge). Dieser Generationenkreislauf soll dem Spieler
vermitteln, dass Bevölkerung nicht nur eine Zahl, sondern eine Struktur mit
eigener Trägheit und eigenen Investitionsbedarfen ist.

## 12. Flüchtlingsströme durch Kriegs- und Umweltereignisse

Neben individueller Migration (siehe Abschnitt 6) können größere,
schlagartige Bevölkerungsbewegungen auftreten: Ein schwerer Konflikt (siehe
TICKET-005, insbesondere Belagerungen) oder eine plötzliche
Kontaminationsausbreitung (siehe TICKET-002) kann eine ganze Siedlung zur
Aufgabe zwingen, deren Bewohner in Wellen bei benachbarten Siedlungen oder
sogar bei einer anderen Fraktion Zuflucht suchen. Solche Flüchtlingswellen
stellen aufnehmende Siedlungen vor kurzfristige Versorgungsprobleme (Anstieg
des Nahrungs- und Gesundheitsbedarfs ohne sofortigen Anstieg der
Produktionskapazität), bieten aber mittelfristig zusätzliche Arbeitskraft und
gelegentlich wertvolles Wissen aus der Herkunftssiedlung. Der Umgang mit
Flüchtlingen (aufnehmen, abweisen, temporär versorgen) ist eine bewusst
schwierige moralische und wirtschaftliche Entscheidung, die die Spannungen
aus TICKET-001 (Bunker-Auswahlkriterien, gesellschaftliche Stigmatisierung)
im laufenden Spiel konkret erfahrbar macht.

## 13. Todesursachen und ihre Sichtbarkeit für den Spieler

Um Transparenz über den Zustand der eigenen Bevölkerung zu schaffen, werden
Sterbefälle nach Ursache kategorisiert und ausgewiesen: Hunger, Krankheit/
Epidemie, Konfliktverluste, Alter sowie – speziell bei den Mutierten –
mutationsbedingte Komplikationen bei Geburt oder im frühen Kindesalter.
Diese Kategorisierung soll dem Spieler helfen, gezielt gegenzusteuern (z. B.
Investition in Medizin bei hoher Krankheitssterblichkeit statt pauschal in
alle Bedürfnisse gleichzeitig) und macht die in TICKET-001 beschriebenen
strukturellen Nachteile jeder Fraktion (z. B. erhöhte Kindersterblichkeit
bei den Mutierten) im Spielverlauf konkret sichtbar und nachvollziehbar.

## 14. Beispielhafter Verlauf: Eine Familie über zwei Generationen

Zur Veranschaulichung ein narratives Beispiel bei den Bittergraben-Sippen:
Eine Mutter, Trägerin des Dritten Auges, und ein Vater ohne sichtbare
Mutation bekommen zwei Kinder. Das erste Kind zeigt bei der Geburt keine
Mutation und wächst als geschätzter, aber gesundheitlich unauffälliger
Landarbeiter auf, der später in die Feldarbeit der toxinresistenten
Fruchtfolge (siehe TICKET-006, Abschnitt 10) eingebunden wird. Das zweite
Kind entwickelt eine Ausprägung der Dritten Hand und wird früh als
Lehrling bei einem erfahrenen Feinmechaniker der Sippe aufgenommen,
leidet aber im Jugendalter unter einer belastenden Autoimmunerkrankung, die
zeitweise medizinische Versorgung aus dem Handel mit Bastion Nord
erfordert (siehe TICKET-004, Abschnitt 11: Beispielverhandlung Werkzeuge
gegen Medizin). Als die Mutter im mittleren Alter bei einem Kundschafter-
Einsatz in der Kraterzone (siehe TICKET-006, Abschnitt 11) ums Leben kommt,
geht ihr Erfahrungswissen über sichere Pfade teilweise verloren, da sie
ihre Beobachtungen nur unvollständig an jüngere Kundschafter weitergegeben
hatte – ein konkretes Beispiel für den in Abschnitt 11 beschriebenen
Wissensverlust durch fehlende Nachfolgeausbildung. Dieses Beispiel zeigt,
wie eng Mutationsmanifestation, Gesundheit, Arbeitskraftallokation,
Handel und Wissensweitergabe im System ineinandergreifen und wie
individuelle Schicksale die übergeordneten Systemmechaniken greifbar machen
können, etwa in Form von Ereignistexten oder kurzen Vignetten im
späteren Spiel.

## 15. Bevölkerungsobergrenzen und Tragfähigkeit

Jede Siedlung und jede Bunkeranlage hat eine geografisch und infrastrukturell
begrenzte Tragfähigkeit (siehe TICKET-002: verfügbare Fläche, Wasserzugang,
Gebäudekapazität), die nicht beliebig überschritten werden kann. Wird diese
Obergrenze durch Bevölkerungswachstum oder Flüchtlingsaufnahme (siehe
Abschnitt 12) überschritten, sinkt automatisch die Bedürfnisbefriedigung in
allen vier Kategorien (Überbelegung wirkt sich negativ auf Gesundheit und
Moral aus, selbst bei ausreichender Nahrung), was einen natürlichen,
spielmechanischen Druck erzeugt, entweder in zusätzliche Infrastruktur zu
investieren, neue Siedlungsflächen zu erschließen oder überschüssige
Bevölkerung durch Migration oder Auswanderung abzugeben.

## Offene Punkte für Folgetickets

- Konkrete Formeln für Wachstums-, Krankheits- und Migrationsraten
  (Balancing-Iteration).
- UI-Darstellung von Bedürfniswerten, Bevölkerungspyramiden und
  Mutationsstatistiken (TICKET-008).
- Detaillierte Liste möglicher Aufstands-/Abspaltungsereignisse je
  Fraktion.

## 16. Zusammenfassung der Designabsicht

Das Bevölkerungs- und Bedürfnissystem soll dem Spieler durchgehend vermitteln,
dass Menschen (und Mutierte) in dieser Welt keine austauschbaren
Produktionseinheiten sind, sondern eine begrenzte, verletzliche und nur
langsam erneuerbare Grundlage jeder wirtschaftlichen, technologischen und
militärischen Stärke aus den vorherigen Tickets. Jede Entscheidung – ob
Investition in Medizin, Aufnahme von Flüchtlingen oder Eskalation eines
Konflikts – soll spürbare, oft erst mit Verzögerung sichtbare demografische
Konsequenzen haben, die konsistent zu den in TICKET-001 angelegten
gesellschaftlichen Grundcharakteren der drei Fraktionen stehen.

## Akzeptanzkriterien

- [x] Vier Grundbedürfniskategorien definiert.
- [x] Fraktionsspezifisches Bevölkerungswachstum konsistent mit TICKET-001
      ausgearbeitet.
- [x] Mutationsmanifestation bei Nachkommen als eigenes Ereignis beschrieben.
- [x] Krankheits-/Epidemiesystem definiert.
- [x] Migrationsmechanik zwischen Fraktionen beschrieben.
- [x] Umfang mindestens 2000 Wörter.
