# TICKET-005: Kampf- & Konfliktsystem (Raids, Verteidigung, Diplomatie)

**Typ:** Design / Gameplay-System
**Epic:** Endzeit-Wirtschaftssimulator – Militär & Diplomatie
**Priorität:** Mittel-Hoch
**Status:** Entwurf fertig, bereit für Review
**Abhängigkeiten:** TICKET-001 (Fraktionen), TICKET-002 (Karte/Gelände),
TICKET-003 (Wirtschaft/Ausrüstung), TICKET-004 (Handel/Eskalationspfad)
**Blockiert:** TICKET-007 (Bevölkerungsverluste durch Konflikte)

## Ziel des Tickets

Definition des Kampf- und Konfliktsystems: wie militärische Auseinandersetzungen
zwischen den drei Fraktionen ablaufen, welche Rolle Raids, Verteidigung und
Diplomatie im Vergleich zu offenem Krieg spielen, und wie sich die
unterschiedlichen militärischen Profile aus TICKET-001 (Bunker-Feuerkraft,
Mutierten-Guerilla, Wastelander-Miliz) konkret in Spielmechaniken übersetzen.
Konflikt soll im Simulator stets die kostspieligere Alternative zu Handel und
Diplomatie sein, aber eine glaubwürdige, manchmal notwendige Option bleiben.

## 1. Grundphilosophie des Konfliktsystems

Anders als in reinen Strategiespielen mit großen, klar entscheidbaren
Schlachten liegt der Fokus hier auf **Abnutzung, Asymmetrie und begrenzten
Ressourcen**. Keine Fraktion kann sich einen langen, offenen Krieg leisten,
ohne ihre Wirtschaft (TICKET-003) massiv zu schädigen. Kämpfe sind daher
meist klein, lokal begrenzt und stark von Gelände, Vorwarnzeit und
Ausrüstungszustand abhängig. Ziel ist es, dass militärische Stärke immer im
Verhältnis zu wirtschaftlicher Tragfähigkeit gedacht werden muss: Ein
Bunkerkontingent mit überlegener Feuerkraft, das seine Munitionsreserven
verbraucht, ist Wochen später verwundbarer als eine zahlenmäßig unterlegene,
aber gut versorgte Wastelander-Miliz.

## 2. Eskalationsleiter

Aufbauend auf dem in TICKET-004 (Abschnitt 9.1) skizzierten Übergang von
Handelskonflikten zu offener Gewalt wird eine klare Eskalationsleiter
definiert:

1. **Spannung**: diplomatische Verstimmung, Grenzzwischenfälle, verschärfte
   Zollkontrollen – keine Gewalt, aber erhöhte Wachsamkeit.
2. **Embargo/Wirtschaftskonflikt** (siehe TICKET-004): Handel wird
   eingeschränkt oder ausgesetzt.
3. **Raid**: begrenzter, meist nicht offiziell erklärter Überfall auf
   Vorräte, Karawanen oder Außenposten, mit dem Ziel materiellen Gewinns
   oder gezielter Schwächung, nicht territorialer Eroberung.
4. **Offener Grenzkonflikt**: wiederholte Raids, Gegenschläge, erste
   dauerhafte Frontlinien, hohe Aufmerksamkeit aller drei Fraktionen.
5. **Belagerung/Eroberungsversuch**: seltenster, teuerster Fall – der
   Versuch, eine gegnerische Kernsiedlung oder Bunkeranlage dauerhaft
   einzunehmen, mit erheblichem Risiko für alle Beteiligten und starken
   Auswirkungen auf die Gesamtwirtschaft der Region.

Spieler sollen jederzeit die Möglichkeit haben, die Eskalation zu
deeskalieren (Verhandlung, Tribut, Waffenstillstand), aber jede Stufe der
Eskalationsleiter erschwert eine Rückkehr zur vorherigen Stufe zunehmend
(sinkende Reputation, wachsendes Misstrauen).

## 2.1 Deeskalation als aktive Spielhandlung

Deeskalation ist keine passive Rückkehr zum Ausgangszustand, sondern eine
eigenständige, ressourcenintensive Handlung: Sie erfordert diplomatische
Gesten (Tributzahlungen, Freilassung von Gefangenen, öffentliche
Entschuldigungen über Aarbrücker Vermittler), die je nach Eskalationsstufe
unterschiedlich teuer sind. Je länger ein Konflikt auf einer hohen Stufe
verharrt, desto teurer und unsicherer wird eine erfolgreiche Deeskalation,
da sich auf beiden Seiten Groll und wirtschaftlicher Schaden ansammeln, die
in Verhandlungen berücksichtigt werden müssen.

## 3. Raid-Mechanik

Raids sind die häufigste Form bewaffneter Auseinandersetzung im Simulator.
Ein Raid hat einen Angreifer, ein Ziel (Karawane, Außenposten, Lager) und
einen Verteidiger. Der Ausgang hängt von mehreren Faktoren ab: Zahl und
Ausrüstung der beteiligten Kämpfer, Geländevorteil (siehe TICKET-002:
Grauwald begünstigt Guerilla-Taktik, offenes Osthügelland begünstigt
Feuerkraft und Reichweite), Vorwarnzeit (durch Kundschafter oder
"Drittes Auge"-Träger reduziert) und Zufallselemente (Wetter, Munitionsstau,
Verrat). Ein erfolgreicher Raid bringt dem Angreifer Beute (Ressourcen,
gelegentlich Gefangene/Überläufer), schädigt aber die Reputation gegenüber
der angegriffenen Fraktion erheblich, sofern der Angriff bekannt wird oder
zugeordnet werden kann.

## 4. Verteidigungsmechanik

Verteidigung basiert auf drei Säulen: **Befestigung** (bauliche Maßnahmen wie
Mauern, Wachtürme, Fallenfelder – Rohstoff- und Zeitinvestition, siehe
TICKET-003), **Vorwarnsystem** (Kundschafter, Wachposten, bei den Mutierten
besonders effektiv durch "Drittes Auge"-Träger) und **Reaktionskraft**
(stehende Wachmannschaften vs. schnelle Mobilisierung aus der
Zivilbevölkerung). Bunker-Siedlungen haben strukturell starke
Befestigungen, aber begrenzte Reaktionskraft durch geringe Bevölkerung;
Wastelander-Siedlungen haben schwache Befestigungen, aber hohe
Mobilisierungsfähigkeit durch breite Miliztraditionen; Mutierten-Sippen
profitieren von natürlicher Tarnung und Geländekenntnis in kontaminierten
Zonen, die Außenstehenden ohne Schutzausrüstung oder Resistenz den Zugang
erschweren.

## 5. Fraktionsspezifische Kampfstile

**Bunker-Truppen** kämpfen diszipliniert, mit überlegener Ausrüstung
(Feuerwaffen, gelegentlich gepanzerte Fahrzeuge, funkgestützte Koordination),
sind aber zahlenmäßig limitiert und leiden bei längeren Konflikten unter
Munitionsknappheit (siehe TICKET-003: Metall- und Chemiekette begrenzt
Nachschub). Ihre Stärke liegt in kurzen, entscheidenden Gefechten und der
Verteidigung befestigter Stellungen; ihre Schwäche in langwierigen,
verlustreichen Abnutzungskriegen.

**Mutierten-Kämpfer** setzen auf Guerilla-Taktik: Fallen, Hinterhalte,
Rückzug in kontaminiertes Gelände, das Verfolger ohne Schutzausrüstung
abschreckt. "Dritte Hand"-Träger sind für Feldreparaturen und
Fallenbau besonders wertvoll, "Drittes Auge"-Träger für Aufklärung und
Vorwarnung. Ihre Stärke liegt in Kleingruppentaktik und Heimvorteil in
eigenem Terrain, ihre Schwäche in geringer Zahl und begrenzter Feuerkraft
bei offener Konfrontation.

**Wastelander-Milizen** bestehen aus mobilisierten Dorfbewohnern mit
improvisierten Waffen, kompensieren fehlende Ausrüstung durch Zahl,
Ortskenntnis und Verteidigungsentschlossenheit (Kampf um die eigene
Heimat). Ihre Stärke liegt in der Verteidigung des eigenen Territoriums und
in Zermürbungstaktik, ihre Schwäche in mangelnder Koordination bei
größeren, überregionalen Konflikten.

## 6. Diplomatie als primäres Konfliktvermeidungswerkzeug

Diplomatie läuft über Verhandlungen (direkt oder über Aarbrücker
Vermittlung, siehe TICKET-004), die zu Waffenstillständen, Tributzahlungen,
Gebietskompromissen oder formellen Bündnissen führen können. Ein zentrales
Beispiel ist das in TICKET-002 beschriebene Stillhalteabkommen am Staudamm
zwischen Bastion Nord und den Aschenfeld-Sippen: ein informelles, jederzeit
kündbares Arrangement, das als Vorlage für spielergesteuerte Verträge
dienen soll. Diplomatische Optionen umfassen auch befristete Allianzen
zweier Fraktionen gegen eine dritte, was angesichts der wirtschaftlichen
Komplementarität aus TICKET-004 (Abschnitt 8) strategisch reizvoll, aber
auch riskant ist, da Bündnisse die dritte Fraktion in eine wirtschaftlich
und militärisch prekäre Lage drängen können.

## 7. Kriegsfolgen und Wiederaufbau

Jeder Konflikt oberhalb der Raid-Stufe hinterlässt spürbare, langfristige
Folgen: zerstörte Produktionsstätten, verminderte Bevölkerung (siehe
TICKET-007), beschädigte Infrastruktur (z. B. Brücken, Pässe), sowie
gesunkene Reputation, die zukünftigen Handel erschwert. Wiederaufbau
benötigt gezielte Investition von Ressourcen und Zeit und ist nie
kostenlos – ein bewusstes Gegengewicht zu Spielstrategien, die Krieg als
kostengünstige Lösung wirtschaftlicher Probleme betrachten könnten.

## 8. Zufallsereignisse und Randkonflikte

Neben den drei Hauptfraktionen existieren, wie in TICKET-001 erwähnt,
kleinere Randgruppen (Deserteure, Banden im Grauwald, gelegentliche
Sektenanhänger), die als neutrale, meist aggressive Drittpartei
Zufallskonflikte auslösen können – etwa ein Überfall auf eine Karawane, der
keiner der drei Hauptfraktionen zugeordnet werden kann und daher nicht
automatisch zu diplomatischer Eskalation zwischen ihnen führt, aber
dennoch wirtschaftlichen Schaden verursacht und Investitionen in
Sicherheit rechtfertigt.

## 8.1 Umgang mit Randkonflikten in der Diplomatie

Da Randkonflikte keiner Hauptfraktion eindeutig zurechenbar sind, können sie
paradoxerweise auch diplomatisch genutzt werden: Eine Fraktion kann einen
Überfall fälschlich einer anderen Fraktion zuschreiben, um Misstrauen zu
säen (siehe Abschnitt 14, Propaganda), oder umgekehrt gemeinsam mit einer
rivalisierenden Fraktion gegen eine Bande vorgehen, um kurzfristig
Vertrauen aufzubauen, ohne die eigentliche Rivalität aufzugeben. Randkonflikte
dienen damit als flexibles narratives und mechanisches Werkzeug, um
Beziehungen zwischen den drei Hauptfraktionen zu verschieben, ohne dass
diese direkt gegeneinander kämpfen müssen, und liefern damit zusätzliche
strategische Tiefe jenseits offener Kriegsführung, ganz ohne dass eine der
drei Hauptfraktionen dafür formell zur Waffe greifen müsste.

## 9. Zusammenspiel mit anderen Systemen

Das Konfliktsystem ist eng mit der Wirtschaft (TICKET-003: Waffen- und
Munitionsproduktion, Wiederaufbaukosten), dem Handel (TICKET-004:
Eskalationspfad, Karawanenrisiko) und der Bevölkerung (TICKET-007:
Verluste, Moralwirkung von Konflikten) verzahnt. Es liefert zudem
Anwendungsfälle für den Technologiebaum (TICKET-006): militärische
Technologien (bessere Befestigungen, Waffen, Kommunikationsmittel) sind ein
zentraler Forschungspfad, dessen Nutzen sich direkt in diesem System zeigt.

## 10. Beispielhafter Konfliktverlauf: Der Zwischenfall am Bittergraben

Ein durchgespieltes Beispiel verdeutlicht das Zusammenspiel der Systeme: Eine
Wastelander-Miliz aus dem Uferlager Rothal überfällt, ohne offizielle
Erlaubnis des Ältestenrats, einen kleinen Vorratsspeicher der
Bittergraben-Sippen, weil ein Anführer der Miliz nach einer schlechten
Ernte dringend Nahrung benötigt. Der Raid gelingt teilweise: Ein Teil der
Vorräte wird erbeutet, doch die Sippen erkennen die Angreifer anhand
zurückgelassener Ausrüstung. Auf Stufe 3 der Eskalationsleiter (Raid)
reagieren die Sippen zunächst nicht militärisch, sondern diplomatisch: Sie
fordern über einen Boten in Aarbrück Wiedergutmachung vom Ältestenrat
Rothals. Da Rothal traditionell weniger diszipliniert und gesetzestreu ist
als andere Wastelander-Siedlungen (siehe TICKET-002), verzögert sich die
Antwort, was das Misstrauen weiter erhöht. Erst als eine zweite,
diesmal erfolglose Überfallwelle stattfindet, eskaliert der Konflikt auf
Stufe 4: Die Sippen errichten Wachposten am Grenzweg zum Uferlager, was den
Handel zwischen beiden Parteien empfindlich stört und in der Folge auch
Aarbrücker Händler betrifft, die auf diese Route angewiesen waren. Erst eine
Vermittlung durch den neutralen Ältestenrat Aarbrücks (siehe TICKET-004)
führt zu einer Wiedergutmachungszahlung in Form von Werkzeugen und einer
Rückkehr zu Stufe 1 (Spannung) – jedoch mit dauerhaft gesunkener
Reputation zwischen Rothal und den Bittergraben-Sippen, die zukünftige
Handelsgeschäfte weiterhin erschwert.

## 11. Moral, Erschöpfung und Kampfbereitschaft

Kämpfende Einheiten verfügen über einen Moralwert, der von Versorgungslage
(Nahrung, Munition, siehe TICKET-003), jüngsten Erfolgen oder Niederlagen
und der wahrgenommenen Legitimität des Konflikts abhängt (ein
Verteidigungskampf um die eigene Siedlung erzeugt spürbar höhere Moral als
ein offensiver Raid auf fremdes Gebiet ohne klaren Rückhalt in der eigenen
Bevölkerung). Sinkt die Moral unter kritische Schwellen, drohen Rückzug,
Befehlsverweigerung oder sogar Überläufer – ein Mechanismus, der besonders
bei Wastelander-Milizen (geringe formale Disziplin, aber hohe intrinsische
Verteidigungsmotivation) und bei Bunker-Truppen in lang andauernden
Konflikten (sinkende Moral durch Ressourcenknappheit trotz hoher
Grunddisziplin) unterschiedlich stark ausgeprägt sein soll.

## 12. Gefangene, Überläufer und Wiedereingliederung

Konflikte erzeugen nicht nur Verluste, sondern auch Gefangene und
Überläufer. Gefangene können gegen Lösegeld (Ressourcen, Talons) oder im
Austausch gegen eigene Gefangene freigegeben, als Verhandlungsmasse in
diplomatischen Prozessen genutzt oder – mit erheblichem Reputationsrisiko –
zur Zwangsarbeit herangezogen werden. Überläufer, etwa unzufriedene
Bunker-Bewohner, die zu den Wastelandern fliehen, oder Wastelander, die
wegen Perspektivlosigkeit versuchen, in einen Bunker aufgenommen zu werden,
liefern zusätzliche Bevölkerung und mitunter wertvolles Insiderwissen über
die Herkunftsfraktion, was in TICKET-007 als Migrationsmechanik vertieft
werden soll.

## 13. Belagerung im Detail

Belagerungen, die seltenste und teuerste Eskalationsstufe, unterscheiden
sich mechanisch deutlich von Raids: Sie erfordern eine anhaltende
Truppenpräsenz über mehrere Spielwochen, wodurch die Wirtschaft des
Angreifers (Nachschub, Nahrungsversorgung der belagernden Truppen) stark
beansprucht wird, während die belagerte Siedlung mit Vorratsverbrauch,
sinkender Moral und dem Risiko interner Unruhen (siehe TICKET-007) zu
kämpfen hat. Ein erfolgreicher Verteidiger kann eine Belagerung allein
durch Aussitzen gewinnen, wenn er ausreichend bevorratet ist und der
Angreifer seine eigene Versorgungslage nicht sichern kann – ein Szenario,
das besonders für die ressourcenarmen, aber gut befestigten Bunkeranlagen
relevant ist.

## 14. Propaganda, Gerüchte und psychologische Kriegsführung

Neben direkter Gewalt sollen auch nicht-tödliche Konfliktformen abgebildet
werden: gezielte Gerüchte über die Schwäche oder Unzuverlässigkeit einer
gegnerischen Fraktion (etwa die Behauptung, die Bittergraben-Sippen hätten
verseuchte Heilkräuter geliefert), die Reputation schädigen, ohne dass ein
einziger Schuss fällt. Solche Propagandaaktionen sind riskant: Werden sie
als Lüge entlarvt, schlägt der Reputationsschaden auf den Urheber zurück.
Dieses Element verzahnt das Konfliktsystem eng mit der Reputationsmechanik
aus TICKET-004 und bietet Spielern eine kostengünstigere, aber unsicherere
Alternative zu offener militärischer Eskalation.

## 15. Neutralität und Schutzzonen

Bestimmte Orte, allen voran Aarbrück (siehe TICKET-002 und TICKET-004),
gelten als faktische Schutzzonen, in denen Gewalt zwischen den Fraktionen
gesellschaftlich geächtet ist. Ein Angriff auf neutralem Boden zieht
automatisch eine deutlich stärkere Reputationsstrafe nach sich als ein
vergleichbarer Angriff im umstrittenen Grauwald, da er als Bruch eines
grundlegenden, überfraktionellen Stillhalteabkommens gilt. Diese abgestufte
Bewertung von Gewalt je nach Ort soll Spielern vermitteln, dass Krieg in
dieser Welt nicht überall gleich "kostet", sondern stark vom sozialen und
geografischen Kontext abhängt.

## 16. Langfristige Machtverschiebungen

Über viele Konflikte hinweg soll sich das militärische Kräfteverhältnis der
Region graduell verschieben können, ohne dass eine Fraktion die anderen
jemals vollständig eliminiert – ein Design-Ziel, das die drei Fraktionen
als dauerhafte, sich gegenseitig bedingende Pole der Spielwelt erhält.
Schwächt sich eine Fraktion durch wiederholte verlustreiche Konflikte,
verschieben sich Handelsabhängigkeiten (TICKET-004) und
Bevölkerungsverhältnisse (TICKET-007) entsprechend, was wiederum neue
diplomatische und wirtschaftliche Gleichgewichte erzeugt, statt in einem
klaren "Sieger nimmt alles"-Szenario zu enden.

## Offene Punkte für Folgetickets

- Konkrete Kampfauflösungsformeln (Truppenstärke, Gelände- und
  Ausrüstungsmodifikatoren) für spätere Balancing-Iterationen.
- UI-Darstellung von Bedrohungslagen, Eskalationsstufen und
  Verhandlungsdialogen (TICKET-008).
- Liste konkreter militärischer Technologien im Forschungsbaum
  (TICKET-006).

## 17. Design-Zusammenfassung

Das Konfliktsystem soll insgesamt vermitteln, dass Gewalt in dieser
Endzeitwelt selten, teuer und selten eindeutig gewinnbringend ist. Jede
militärische Option – vom kleinen Raid bis zur Belagerung – steht in
direkter Konkurrenz zu den günstigeren, aber langsameren Werkzeugen aus
Handel und Diplomatie (TICKET-004). Spieler, die Konflikte als letztes statt
erstes Mittel einsetzen, sollen langfristig wirtschaftlich und militärisch
stabiler dastehen als solche, die auf schnelle militärische Lösungen setzen
– ohne dass Konflikt jemals gänzlich vermeidbar oder bedeutungslos wird,
da Randgruppen, knappe Ressourcen und divergierende Interessen der drei
Fraktionen (siehe TICKET-001) strukturell immer wieder neue Reibungspunkte
erzeugen. Damit bleibt militärische Stärke ein relevanter, aber nie
dominanter Baustein der Gesamtstrategie, eingebettet in das größere
Zusammenspiel aus Wirtschaft, Handel und Diplomatie, das die übrigen Tickets
dieses Design-Dokuments beschreiben.

## Akzeptanzkriterien

- [x] Eskalationsleiter von Spannung bis Belagerung definiert.
- [x] Raid- und Verteidigungsmechanik ausgearbeitet.
- [x] Fraktionsspezifische Kampfstile mit Stärken/Schwächen beschrieben.
- [x] Diplomatie als primäres Konfliktvermeidungswerkzeug verankert.
- [x] Kriegsfolgen und Wiederaufbau berücksichtigt.
- [x] Umfang mindestens 2000 Wörter.
