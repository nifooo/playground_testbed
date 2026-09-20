# TICKET-003: Wirtschaftssystem – Ressourcen, Produktionsketten, Währungen

**Typ:** Design / Gameplay-System
**Epic:** Endzeit-Wirtschaftssimulator – Wirtschaft
**Priorität:** Hoch
**Status:** Entwurf fertig, bereit für Review
**Abhängigkeiten:** TICKET-001 (Fraktionen), TICKET-002 (Karte & Ressourcenverteilung)
**Blockiert:** TICKET-004 (Handel), TICKET-006 (Technologie), TICKET-007 (Bevölkerung)

## Ziel des Tickets

Definition des wirtschaftlichen Kernsystems des Simulators: welche
Ressourcenkategorien existieren, wie Produktionsketten von Rohstoff zu
Endprodukt aufgebaut sind, wie die drei Fraktionen wirtschaftlich
unterschiedlich funktionieren, und welche Währungs- bzw. Tauschsysteme
jeweils genutzt werden. Die Wirtschaft soll knappheitsgetrieben und
interdependent sein: Kein Fraktionswirtschaftskreislauf ist für sich allein
überlebensfähig, was Handel (TICKET-004) zur zentralen Spielmechanik macht.

## 1. Grundprinzipien der Wirtschaftssimulation

Die Wirtschaft folgt drei Leitprinzipien: Erstens, **Knappheit vor
Wachstum** – anders als in klassischen Aufbauspielen mit exponentiellem
Wachstum bleibt die Gesamtressourcenmenge der Welt endlich und regeneriert
sich nur langsam (Ernten, langsam nachwachsende Wälder, begrenzt förderbare
Altbestände). Zweitens, **Spezialisierung durch Geografie und Fraktion** –
jede Fraktion kann nur eine Teilmenge aller benötigten Güter effizient selbst
herstellen, der Rest muss gehandelt, erobert oder mühsam substituiert werden.
Drittens, **Verderb und Verschleiß** – Nahrung verrottet, Werkzeuge nutzen
sich ab, Maschinen benötigen Wartung; reines Horten ist daher keine
dominante Strategie, was den Handelsdruck zusätzlich erhöht.

## 2. Ressourcenkategorien

Ressourcen werden in sechs Hauptkategorien gegliedert, die jeweils
unterschiedliche Lagerungs- und Verderbseigenschaften besitzen:

- **Nahrung** (Getreide, Gemüse, Fleisch, Konserven): verderblich, saisonal
  produziert, wichtigste Grundlage für Bevölkerungswachstum und Moral (siehe
  TICKET-007).
- **Rohstoffe** (Holz, Erz, Altmetall, Fasern, Steine): kaum verderblich,
  Basis für fast alle Produktionsketten.
- **Chemikalien & Spezialstoffe** (Reinigungsmittel, Düngemittel-Reste,
  Treibstoffzusätze, Konservierungsstoffe): mittel verderblich, oft nur in
  kontaminierten Zonen oder alten Industrieanlagen verfügbar.
- **Medizin & Heilpflanzen**: sehr wertvoll, begrenzt verfügbar,
  Schlüsselressource für Bevölkerungsgesundheit und ein zentrales
  Handelsgut der Mutierten.
- **Werkzeuge, Waffen & Ausrüstung**: kaum verderblich, aber
  verschleißanfällig, benötigen regelmäßigen Nachschub an Ersatzteilen.
- **Energie & Treibstoff** (Strom aus dem Staudamm, Restöl, Holzkohle,
  Biogas): unterschiedlich lagerbar (Strom nicht speicherbar ohne
  Batterien, Treibstoff begrenzt lagerbar), treibt Produktion und Transport
  an.

## 3. Produktionsketten

Produktionsketten bestehen aus drei Stufen: Rohstoff → Zwischenprodukt →
Endprodukt. Beispiele, die im Simulator konkret abgebildet werden sollen:

- **Nahrungskette**: Getreide (Anbau) → Mehl (Mühle) → Brot (Bäckerei);
  parallel: Vieh (Weidehaltung) → Rohfleisch (Schlachtung) → haltbares
  Fleisch (Räucherei/Konservierung).
- **Metallkette**: Altmetall/Erz (Bergung/Abbau) → Rohmetall (Schmelze) →
  Werkzeuge/Waffenteile (Schmiede/Werkstatt).
- **Chemiekette**: Kontaminierter Rohstoff (Bergung in Zone 2/3) →
  aufbereitete Chemikalien (Raffination, nur mit Bunker- oder
  Mutierten-Fachwissen möglich) → Medizin, Treibstoffzusätze,
  Konservierungsmittel.
- **Textilkette**: Fasern (Anbau/Sammlung) → Garn (Spinnerei) → Kleidung/
  Schutzausrüstung (Weberei/Schneiderei).
- **Energiekette**: Wasserkraft (Staudamm) bzw. Restöl/Holz → Strom/Wärme →
  Antrieb für Werkstätten, Kühlung von Medizin, Beleuchtung.

Jede Produktionsstufe benötigt Arbeitskraft (siehe TICKET-007 für
Bevölkerungs-/Arbeitskraftmodell), eine passende Einrichtung (Mühle,
Schmiede, Werkstatt) und oft zusätzlich Energie oder Werkzeuge als
Verbrauchsgut. Diese Verzahnung erzeugt realistische Engpässe: Eine
Metallknappheit wirkt sich verzögert auch auf die Nahrungsproduktion aus,
weil Landwirtschaftsgeräte nicht ersetzt werden können.

## 4. Fraktionsspezifische Wirtschaftsprofile

**Bunker-Überlebende**: hochgradig spezialisiert auf die letzten Stufen der
Metall-, Chemie- und Energieketten (Feinmechanik, Elektronik, Medizin-
Synthese), aber strukturell schwach in Nahrungs- und Rohstoffproduktion.
Ihre interne Wirtschaft ist eine Planwirtschaft: Zentrale Lager, Rationierung
nach Zuteilungsmarken, Produktion nach Bedarfsplan des Rats der Sieben.
Überschüsse an Fertigwaren werden gezielt in den Außenhandel eingespeist, um
den strukturellen Nahrungs- und Rohstoffmangel auszugleichen.

**Mutierte**: spezialisiert auf die Rohstoffgewinnung in kontaminierten
Zonen (Stufe 1 der Ketten) sowie auf hochwertige Veredelung durch besondere
manuelle Fertigkeiten (Dritte-Hand-Handwerk: Feinmechanik, Fallenbau,
Prothesenbau) und Heilpflanzenverarbeitung. Ihre Wirtschaft ist
genossenschaftlich organisiert: Sippen teilen Erträge nach traditionellen
Verteilungsschlüsseln, es gibt keine zentrale Lagerbehörde, dafür ein starkes
Ehrensystem gegenseitiger Verpflichtung ("Wer nimmt, gibt auch").

**Wastelander**: breit aufgestellt in Nahrungsproduktion, Rohstoffgewinnung
(unkontaminierte Zonen) und einfacher Werkzeugherstellung, aber schwach in
allem, was Chemie, Elektronik oder Präzisionsfertigung erfordert. Ihre
Wirtschaft ist dezentral und marktbasiert: Dorfmärkte, Naturaltausch,
informelle Kreditbeziehungen zwischen Familien und Siedlungen.

## 5. Währungssysteme

Da eine einheitliche Währung nach dem Zusammenbruch der Staaten nicht mehr
existiert, entwickeln die Fraktionen eigene, grundverschiedene Systeme:

- **Bunker-Scrip**: ein zentral gebuchtes Kreditsystem innerhalb der Bunker,
  gedeckt durch die Zuteilungsplanung des Rats der Sieben. Scrip hat außerhalb
  der Bunker keinen direkten Wert, wird aber von Aarbrücker Händlern in
  begrenztem Umfang als Zahlungsversprechen für zukünftige Fertigwarenlieferungen
  akzeptiert.
- **Sippen-Tauschehre**: bei den Mutierten kein physisches Geld, sondern ein
  informelles Buchführungssystem über gegenseitige Verpflichtungen zwischen
  Sippen ("Schuld" und "Gefälligkeit"), das durch Älteste verwaltet und bei
  Streitigkeiten schlichtend ausgelegt wird.
- **Warengeld der Wastelander**: Salz, Munition und haltbare Konserven
  fungieren als informelle Recheneinheiten und Zahlungsmittel in
  Dorfmärkten, da sie begehrt, teilbar und über längere Zeit lagerbar sind.
- **Vorkriegsmünzen und Talons von Aarbrück**: In der neutralen Handelsstadt
  Aarbrück hat sich zusätzlich eine dritte, überfraktionelle Recheneinheit
  etabliert – ein vom Ältestenrat ausgegebener "Talon" (geprägte
  Metallmarken aus eingeschmolzenen Vorkriegsmünzen), der als einziges
  Zahlungsmittel gilt, das von allen drei Fraktionen zumindest in
  begrenztem Umfang akzeptiert wird, und damit im Handelssystem (TICKET-004)
  eine besondere Rolle als Referenzwährung einnimmt.

### 5.1 Wechselkurs-Realität statt fester Umrechnung

Wichtig ist, dass zwischen diesen vier Systemen keine feste, zentral
festgelegte Umrechnung existiert, sondern ein situativ ausgehandelter,
schwankender "Wechselkurs" je nach Vertrauen, Verfügbarkeit und Dringlichkeit.
Ein Bunker-Scrip-Kredit ist für einen hungrigen Wastelander-Bauern in einem
schlechten Erntejahr mehr wert als in einem guten, weil die Aussicht auf
zukünftige Fertigwarenlieferungen dann dringlicher erscheint. Diese bewusst
unscharfe, verhandelbare Bewertung ist ein zentrales Gameplay-Element, das
im Handelssystem (TICKET-004) als dynamische Preisbildung ausgearbeitet
wird.

## 6. Lagerung, Verderb und Logistik

Jede Ressource hat eine Verderbsrate und eine Lagerkapazitätsanforderung.
Nahrung verliert ohne Kühlung oder Konservierung innerhalb weniger Wochen
deutlich an Wert; Medizin verliert ohne kühle, trockene Lagerung ihre
Wirksamkeit; Metalle und Steine sind praktisch unbegrenzt lagerbar, benötigen
aber Platz. Energie in Form von Strom ist ohne Batteriespeicher gar nicht
lagerbar und muss in Echtzeit verbraucht oder in andere Formen (z. B.
Wärme, mechanische Arbeit) umgewandelt werden. Transport von Ressourcen ist
an die in TICKET-002 definierten Routen, Distanzen und saisonalen
Einschränkungen (Passsperrungen im Winter) gebunden und verursacht eigene
Kosten (Zugtiere, Treibstoff, Zeit, Verlustrisiko durch Überfälle).

## 7. Knappheitsereignisse und wirtschaftliche Zyklen

Um die Wirtschaft dynamisch zu halten, sind wiederkehrende und einmalige
Ereignisse vorgesehen: saisonale Ernteschwankungen, Maschinenausfälle in
Bunkeranlagen durch fehlende Ersatzteile, Kontaminationsausbrüche, die
temporär Fördergebiete unbrauchbar machen, sowie Epidemien, die
Arbeitskraft binden (siehe TICKET-007). Diese Ereignisse sollen nicht rein
zufällig, sondern teilweise durch Spielerentscheidungen beeinflussbar sein
(z. B. Investition in Wartung senkt Ausfallwahrscheinlichkeit von Maschinen).

## 8. Zusammenspiel mit anderen Systemen

Das Wirtschaftssystem bildet die Datenbasis für das Handelssystem
(TICKET-004, Preisbildung basiert auf Angebot/Nachfrage der hier definierten
Ressourcen), das Kampfsystem (TICKET-005, Waffen- und Munitionsproduktion
hängt von Metall- und Chemieketten ab), den Technologiebaum (TICKET-006,
neue Technologien schalten neue Produktionsstufen frei) und das
Bevölkerungssystem (TICKET-007, Nahrungs- und Medizinversorgung beeinflusst
Bevölkerungswachstum und Moral direkt).

## 9. Beispielhafter Wirtschaftskreislauf: Ein Handelsjahr in Aarbrück

Um die Verzahnung der Systeme greifbar zu machen, lohnt sich ein
durchgespieltes Beispiel: Im Frühjahr liefern die Kornweiler-Dörfer erste
Getreideüberschüsse nach Aarbrück, wo sie gegen Aarbrücker Talons verkauft
werden. Mit diesen Talons kaufen Wastelander-Händler bei durchreisenden
Bunker-Karawanen aus Bastion Nord Werkzeuge und Ersatzteile, die sie im
Sommer für die Erntevorbereitung benötigen. Gleichzeitig bringen
Bittergraben-Sippen ihre ersten Kräuterernten des Jahres in die Stadt, da die
Nachfrage nach Medizin nach dem Winter (in dem Krankheiten sich in den engen
Bunkerquartieren gehäuft haben) besonders hoch ist. Der Rat der Sieben in
Bastion Nord zahlt für diese Medizinlieferungen bevorzugt in
Fertigwaren statt in Bunker-Scrip, da die Sippen Scrip ohnehin nicht
verwenden können – ein konkretes Beispiel dafür, wie die in Abschnitt 5
beschriebenen, inkompatiblen Währungssysteme in der Praxis zu direktem
Naturaltausch zurückführen, sobald zwei Fraktionen ohne die vermittelnde
Talon-Wirtschaft Aarbrücks aufeinandertreffen.

Im Spätsommer, kurz vor der Sperrung des Nordpasses, verdichtet sich der
Handel: Bastion Nord kauft in großen Mengen Getreide und Fasern ein, um über
den Winter versorgt zu sein, was die Preise in Aarbrück kurzfristig deutlich
steigen lässt. Wastelander-Händler, die diesen saisonalen Zyklus kennen,
horten gezielt Getreide im Spätsommer, um es zu diesen Spitzenpreisen zu
verkaufen – ein einfaches, aber wirkungsvolles Beispiel für spekulatives
Verhalten, das im Handelssystem (TICKET-004) als Spielmechanik (Preiszyklen,
Lagerspekulation) aufgegriffen werden soll.

## 10. Arbeitskraft, Berufsbilder und Zünfte

Jede Produktionsstufe benötigt nicht nur Rohstoffe und Gebäude, sondern
qualifizierte Arbeitskraft. In den Bunkern ist Arbeit zunftartig organisiert:
Techniker, Chemiker und Mechaniker durchlaufen mehrjährige Ausbildungen in
bunkereigenen Schulen und sind in Ränge gegliedert, die sowohl den Zugang zu
besseren Quartieren als auch die Zuteilung von Werkzeugen bestimmen. Bei den
Mutierten ist Arbeit stärker an individuelle Mutationsausprägung gekoppelt:
Träger der Dritten Hand werden traditionell früh als Handwerkslehrlinge
ausgebildet, Träger des Dritten Auges eher als Späher, Wächter oder
Kräutersammler, da ihre verbesserte Wahrnehmung das Auffinden seltener
Pflanzen und das Erkennen von Gefahren erleichtert. Nicht-mutierte Mitglieder
der Sippen übernehmen komplementär eher körperlich fordernde Aufgaben wie
Bergbau und Transport. Bei den Wastelandern ist Arbeit am wenigsten formal
organisiert: Familienbetriebe, saisonale Erntehelfer-Gemeinschaften und
wenige spezialisierte Handwerker (Schmied, Müllerin, Heilerin) prägen das
Bild; Wissen wird primär innerhalb von Familien weitergegeben, was in
Krisenzeiten (Tod eines Spezialisten ohne Nachfolger) zu plötzlichen,
schwer kompensierbaren Wissensverlusten führen kann – ein Risiko, das
spielmechanisch als Ereignis relevant ist.

## 11. Schwarzmarkt und informelle Wirtschaft

Neben den offiziellen Wirtschaftskreisläufen existiert in allen drei
Fraktionen eine informelle Schattenwirtschaft. In den Bunkern entsteht sie
vor allem aus der strikten Rationierung: Einzelne Techniker oder
Verwaltungsangestellte zweigen Ersatzteile oder Medikamente ab und
verkaufen sie gegen persönliche Vorteile an Mitbewohner oder sogar an
Außenstehende – ein ständiges Politikum zwischen dem Rat der Sieben und der
Bevölkerung. Bei den Wastelandern äußert sich Schwarzmarktaktivität vor
allem im Schmuggel kontaminierter, aber wertvoller Güter aus Zone-1-Gebieten
wie Rothal, die offiziell als gesundheitsschädlich gelten, aber wegen ihres
hohen Wiederverkaufswerts trotzdem gehandelt werden. Bei den Mutierten ist
die Grenze zwischen offiziellem Sippenhandel und Schwarzmarkt am
durchlässigsten, da ihr gesamtes Wirtschaftssystem ohnehin auf informellem
Vertrauen statt auf Institutionen beruht; hier entsteht "Schwarzmarkt" eher
in Form von Handel mit Außenseitern unter Umgehung der Ältestenräte. Für die
Spielmechanik bedeutet dies, dass neben offiziellen Marktpreisen (siehe
TICKET-004) ein zweiter, riskanterer, aber oft lukrativerer Preispfad
existieren sollte.

## 12. Wirtschaftliche Risiken: Inflation, Hortung und Vertrauenskrisen

Da keine der drei Währungsformen durch eine übergreifende Zentralinstanz
gedeckt ist, sind alle Systeme grundsätzlich instabil. Bunker-Scrip kann bei
Führungskrisen im Rat der Sieben rapide an Vertrauen verlieren, wenn
Gerüchte über leere Lager die Runde machen. Die Sippen-Tauschehre ist
anfällig für Streitigkeiten über die "gerechte" Bewertung vergangener
Gefälligkeiten, was bei Führungswechseln in einer Sippe zu offenen
Konflikten führen kann. Aarbrücker Talons wiederum hängen vollständig vom
Vertrauen aller drei Fraktionen in den neutralen Ältestenrat der Stadt ab;
ein Vertrauensverlust hier (etwa durch Korruptionsvorwürfe) würde den
wichtigsten überfraktionellen Handelsmechanismus der gesamten Region
gefährden. Diese strukturellen Schwachstellen sollen im späteren Balancing
gezielt als Krisenereignisse nutzbar gemacht werden, die Spieler durch
wirtschaftliches und diplomatisches Handeln abwenden oder gezielt provozieren
können.

## 13. Skalierung über die Spielzeit

Die Wirtschaftssimulation soll nicht statisch bleiben, sondern über eine
typische Spielsitzung (mehrere In-Game-Jahre) spürbar an Komplexität
gewinnen. Zu Beginn dominieren einfache Tauschgeschäfte mit wenigen Gütern
und kurzen Produktionsketten; mit fortschreitendem technologischen
Fortschritt (siehe TICKET-006) schalten sich zusätzliche Veredelungsstufen
frei, etwa die Umwandlung von einfachem Rohmetall in legiertes,
korrosionsbeständiges Metall, das wiederum als Voraussetzung für
hochwertigere Werkzeuge und Waffen dient. Diese Erweiterung der
Produktionsketten soll bewusst asymmetrisch verlaufen: Bunker-Fraktionen
erschließen neue Veredelungsstufen primär durch Forschung und
Wiederinbetriebnahme alter Anlagen, Mutierte durch experimentelle,
risikobehaftete Anpassung an neue Kontaminationsformen, Wastelander durch
schrittweise Verbesserung bestehender, einfacher Werkzeuge und
Anbaumethoden. Dadurch bleibt die wirtschaftliche Grundcharakteristik jeder
Fraktion über die gesamte Spieldauer erkennbar, auch wenn sich die absolute
Produktionsmenge und -qualität erhöht.

## Offene Punkte für Folgetickets

- Konkrete Zahlenwerte für Produktionsraten, Verderbsraten und
  Lagerkapazitäten (Balancing, spätere Iteration).
- Umrechnungskurse zwischen Bunker-Scrip, Sippen-Tauschehre, Warengeld und
  Aarbrücker Talons (siehe TICKET-004).
- Liste konkreter Maschinen/Gebäude pro Produktionsstufe.

## 14. Abgrenzung zu klassischen Aufbauspiel-Wirtschaften

Bewusst grenzt sich dieses System von typischen Aufbauspiel-Wirtschaften ab,
in denen Ressourcen unbegrenzt gesammelt und Produktionsketten linear
skaliert werden können. Stattdessen soll spürbar bleiben, dass die
Spielwelt eine post-katastrophale, endliche Wirtschaft simuliert: Jede
Erweiterung einer Produktionskette kostet an anderer Stelle etwas
(Arbeitskraft, seltene Rohstoffe, Vertrauen zwischen Fraktionen), und
langfristiges Wachstum ist nur durch kluges Zusammenspiel von eigener
Produktion, Handel und Diplomatie erreichbar – nie durch reine
Ressourcenanhäufung einer einzelnen Fraktion allein.

Diese Unschärfe ist gewollt: Sie verhindert, dass Spieler das Wirtschaftssystem
durch simples Rechnen mit festen Umrechnungstabellen "lösen" können, und
zwingt stattdessen zu ständiger Beobachtung von Erntelagen, Vorratsständen
und politischer Großwetterlage in allen drei Fraktionen.

## Akzeptanzkriterien

- [x] Sechs Ressourcenkategorien mit Verderbs-/Lagerlogik definiert.
- [x] Mindestens fünf vollständige Produktionsketten (Rohstoff → Zwischen- →
      Endprodukt) beschrieben.
- [x] Wirtschaftsprofile aller drei Fraktionen ausgearbeitet.
- [x] Mindestens vier unterschiedliche Währungs-/Tauschsysteme definiert.
- [x] Zusammenspiel mit Handel, Kampf, Technologie und Bevölkerung skizziert.
- [x] Umfang mindestens 2000 Wörter.
