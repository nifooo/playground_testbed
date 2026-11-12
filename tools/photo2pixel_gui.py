"""
Fensteroberflaeche fuer photo2pixel.py (Foto -> C64/SKALD-Pixelgrafik).

Zeigt das Ergebnis live, waehrend die Regler bewegt werden, und speichert
dasselbe indizierte PNG wie das Kommandozeilen-Tool.

Start:   python tools/photo2pixel_gui.py [foto.jpg]
Windows: Doppelklick auf Pixelkonverter.bat im Projektordner

Benoetigt nur Pillow und numpy; Tkinter bringt Python unter Windows mit.
"""

from __future__ import annotations

import queue
import sys
import threading
import tkinter as tk
from pathlib import Path
from tkinter import filedialog, messagebox, ttk

from PIL import Image, ImageGrab, ImageOps, ImageTk

sys.path.insert(0, str(Path(__file__).resolve().parent))
import photo2pixel as p2p  # noqa: E402
from gen_assets_c64 import C64, PALETTE_ORDER  # noqa: E402

MAX_KANTE = 1600            # groessere Fotos werden beim Laden verkleinert
BILDTYPEN = [("Bilder", "*.jpg *.jpeg *.png *.webp *.bmp *.gif *.tif *.tiff"), ("Alle Dateien", "*.*")]

# (Schluessel, Beschriftung, von, bis, Schrittweite)
REGLER_STIL = [
    ("dither", "Dither-Stärke", 0.0, 1.0, 0.05),
    ("ruhe", "Ruhe (nur ähnliche Farben mischen)", 0.0, 1.0, 0.05),
]
REGLER_BILD = [
    ("kontrast", "Kontrast", 0.5, 2.5, 0.05),
    ("saettigung", "Sättigung", 0.0, 2.5, 0.05),
    ("helligkeit", "Helligkeit", 0.5, 2.0, 0.05),
    ("schwarzpunkt", "Schwarzpunkt", 0.0, 0.5, 0.01),
    ("schaerfe", "Schärfe", 0.0, 2.0, 0.1),
]
REGLER_SCHNITT = [
    ("zoom", "Zoom", 1.0, 4.0, 0.05),
    ("fokus_x", "Ausschnitt links ↔ rechts", 0.0, 1.0, 0.01),
    ("fokus_y", "Ausschnitt oben ↔ unten", 0.0, 1.0, 0.01),
    ("toleranz", "Freistell-Toleranz", 0, 150, 1),
]
SCHALTER = ["kontur", "glaetten", "autokontrast", "freistellen", "alpha", "einpassen"]


def hexfarbe(rgb) -> str:
    return "#%02x%02x%02x" % tuple(rgb)


class App:
    def __init__(self, root: tk.Tk, start: str | None = None):
        self.root = root
        root.title("Pixelkonverter – C64 / SKALD-Stil")
        root.geometry("1180x800")
        root.minsize(820, 520)

        self.quelle: Image.Image | None = None
        self.quellpfad: Path | None = None
        self.ergebnis: Image.Image | None = None
        self.foto = None                      # Referenz, sonst raeumt Tk das Bild weg
        self.auftraege: queue.Queue = queue.Queue()
        self.fertig: queue.Queue = queue.Queue()
        self.nummer = 0
        self.timer = None
        self.sperre = False                   # True, waehrend Regler per Code gesetzt werden

        self.v: dict[str, tk.Variable] = {}
        self.farbe_an = {name: True for name in PALETTE_ORDER}
        self.felder: dict[str, tk.Label] = {}

        self._leiste()
        haupt = ttk.Frame(root)
        haupt.pack(fill="both", expand=True)
        self._regler(haupt)
        self._vorschau(haupt)
        self._voreinstellung("szene")

        threading.Thread(target=self._arbeiter, daemon=True).start()
        root.after(60, self._abholen)
        root.bind("<Control-o>", lambda e: self.oeffnen())
        root.bind("<Control-s>", lambda e: self.speichern())
        root.bind("<Control-v>", lambda e: self.einfuegen())
        if start:
            self.laden(Path(start))

    # ---------------------------------------------------------------- Aufbau
    def _leiste(self) -> None:
        f = ttk.Frame(self.root, padding=6)
        f.pack(fill="x")
        ttk.Button(f, text="Bild öffnen …", command=self.oeffnen).pack(side="left")
        ttk.Button(f, text="Aus Zwischenablage", command=self.einfuegen).pack(side="left", padx=4)
        ttk.Button(f, text="Speichern …", command=self.speichern).pack(side="left")
        ttk.Button(f, text="Zurücksetzen", command=lambda: self._voreinstellung(self.v["typ"].get())).pack(side="left", padx=4)
        self.status = ttk.Label(f, text="Bild öffnen (Strg+O) oder aus der Zwischenablage einfügen (Strg+V).")
        self.status.pack(side="left", padx=12)

    def _regler(self, eltern) -> None:
        """Linke Spalte mit allen Optionen, scrollbar fuer kleine Bildschirme."""
        aussen = ttk.Frame(eltern)
        aussen.pack(side="left", fill="y")
        leinwand = tk.Canvas(aussen, width=330, highlightthickness=0)
        balken = ttk.Scrollbar(aussen, orient="vertical", command=leinwand.yview)
        leinwand.configure(yscrollcommand=balken.set)
        balken.pack(side="right", fill="y")
        leinwand.pack(side="left", fill="y", expand=True)
        innen = ttk.Frame(leinwand, padding=(8, 0, 8, 8))
        leinwand.create_window((0, 0), window=innen, anchor="nw", width=330)
        innen.bind("<Configure>", lambda e: leinwand.configure(scrollregion=leinwand.bbox("all")))

        def rad(e):
            leinwand.yview_scroll(-1 if (e.delta > 0 or e.num == 4) else 1, "units")
        rad_ereignisse = ("<MouseWheel>", "<Button-4>", "<Button-5>")
        # Mausrad scrollt die Spalte, solange der Zeiger darueber steht
        aussen.bind("<Enter>", lambda e: [leinwand.bind_all(x, rad) for x in rad_ereignisse])
        aussen.bind("<Leave>", lambda e: [leinwand.unbind_all(x) for x in rad_ereignisse])

        # --- Format
        g = ttk.LabelFrame(innen, text="Format", padding=6)
        g.pack(fill="x", pady=4)
        self.v["typ"] = tk.StringVar(value="szene")
        zeile = ttk.Frame(g)
        zeile.pack(fill="x")
        ttk.Label(zeile, text="Asset-Typ").pack(side="left")
        wahl = ttk.Combobox(zeile, textvariable=self.v["typ"], values=list(p2p.TYPEN), state="readonly", width=12)
        wahl.pack(side="right")
        wahl.bind("<<ComboboxSelected>>", lambda e: self._voreinstellung(self.v["typ"].get()))
        zeile = ttk.Frame(g)
        zeile.pack(fill="x", pady=(6, 0))
        ttk.Label(zeile, text="Größe (Pixel)").pack(side="left")
        self.v["hoehe"] = tk.IntVar(value=200)
        self.v["breite"] = tk.IntVar(value=320)
        for name in ("hoehe", "breite"):
            feld = ttk.Spinbox(zeile, from_=4, to=640, width=5, textvariable=self.v[name], command=self.geaendert)
            feld.pack(side="right")
            feld.bind("<Return>", lambda e: self.geaendert())
            feld.bind("<FocusOut>", lambda e: self.geaendert())
            if name == "hoehe":
                ttk.Label(zeile, text="×").pack(side="right", padx=3)

        # --- Stil
        g = ttk.LabelFrame(innen, text="Stil", padding=6)
        g.pack(fill="x", pady=4)
        self._schieber(g, REGLER_STIL)
        zeile = ttk.Frame(g)
        zeile.pack(fill="x", pady=(4, 0))
        ttk.Label(zeile, text="Dither-Raster").pack(side="left")
        self.v["bayer"] = tk.IntVar(value=8)
        for n in (8, 4, 2):
            ttk.Radiobutton(zeile, text=f"{n}×{n}", value=n, variable=self.v["bayer"], command=self.geaendert).pack(side="right")
        self._haken(g, "kontur", "Schwarze Kontur um freigestelltes Motiv")

        # --- Palette
        g = ttk.LabelFrame(innen, text="Palette (Klick schaltet Farbe an/aus)", padding=6)
        g.pack(fill="x", pady=4)
        raster = ttk.Frame(g)
        raster.pack()
        for i, name in enumerate(PALETTE_ORDER):
            feld = tk.Label(raster, width=3, height=1, bg=hexfarbe(C64[name]), relief="raised", bd=2,
                            font=("TkDefaultFont", 10, "bold"))
            feld.grid(row=i // 8, column=i % 8, padx=1, pady=1)
            feld.bind("<Button-1>", lambda e, n=name: self._farbe(n))
            self.felder[name] = feld
        zeile = ttk.Frame(g)
        zeile.pack(fill="x", pady=(4, 0))
        ttk.Button(zeile, text="Alle", width=8, command=lambda: self._farben_alle(True)).pack(side="left")
        ttk.Button(zeile, text="Nur Grautöne", command=self._farben_grau).pack(side="left", padx=4)

        # --- Bild
        g = ttk.LabelFrame(innen, text="Bildaufbereitung", padding=6)
        g.pack(fill="x", pady=4)
        self._schieber(g, REGLER_BILD)
        self._haken(g, "autokontrast", "Automatischer Kontrast")
        self._haken(g, "glaetten", "Glätten (Rauschen entfernen)")

        # --- Zuschnitt
        g = ttk.LabelFrame(innen, text="Zuschnitt / Transparenz", padding=6)
        g.pack(fill="x", pady=4)
        self._schieber(g, REGLER_SCHNITT[:3])
        self._haken(g, "einpassen", "Ganzes Bild einpassen (Ränder schwarz)")
        self._haken(g, "alpha", "Transparenz des Bildes übernehmen")
        self._haken(g, "freistellen", "Einfarbigen Hintergrund entfernen")
        self._schieber(g, REGLER_SCHNITT[3:])

    def _schieber(self, eltern, liste) -> None:
        for name, text, von, bis, schritt in liste:
            ganz = isinstance(schritt, int)
            var = tk.IntVar() if ganz else tk.DoubleVar()
            self.v[name] = var
            ttk.Label(eltern, text=text).pack(anchor="w", pady=(4, 0))
            tk.Scale(eltern, variable=var, from_=von, to=bis, resolution=schritt, orient="horizontal",
                     showvalue=True, highlightthickness=0, bd=0, sliderlength=18,
                     command=lambda _w: self.geaendert()).pack(fill="x")

    def _haken(self, eltern, name, text) -> None:
        self.v[name] = tk.BooleanVar()
        ttk.Checkbutton(eltern, text=text, variable=self.v[name], command=self.geaendert).pack(anchor="w", pady=(4, 0))

    def _vorschau(self, eltern) -> None:
        rechts = ttk.Frame(eltern, padding=(0, 0, 6, 6))
        rechts.pack(side="left", fill="both", expand=True)
        kopf = ttk.Frame(rechts)
        kopf.pack(fill="x")
        self.v["original"] = tk.BooleanVar(value=False)
        ttk.Checkbutton(kopf, text="Original zeigen", variable=self.v["original"], command=self._zeichnen).pack(side="left")
        self.v["mit_vorschau"] = tk.BooleanVar(value=True)
        ttk.Checkbutton(kopf, text="Beim Speichern auch vergrößerte Vorschau schreiben",
                        variable=self.v["mit_vorschau"]).pack(side="left", padx=12)
        self.info = ttk.Label(kopf, text="")
        self.info.pack(side="right")
        self.bild = tk.Canvas(rechts, bg="#202020", highlightthickness=0)
        self.bild.pack(fill="both", expand=True, pady=(4, 0))
        self.bild.bind("<Configure>", lambda e: self._zeichnen())

    # ------------------------------------------------------------ Einstellungen
    def _voreinstellung(self, typ: str) -> None:
        """Alle Regler auf die Standardwerte des Asset-Typs setzen."""
        a = p2p.argumente(["-", "--typ", typ])
        self.sperre = True
        self.v["typ"].set(typ)
        self.v["breite"].set(a.size[0])
        self.v["hoehe"].set(a.size[1])
        self.v["bayer"].set(a.bayer)
        for name, *_ in REGLER_STIL + REGLER_BILD + REGLER_SCHNITT:
            self.v[name].set(getattr(a, name))
        for name in SCHALTER:
            self.v[name].set(bool(getattr(a, name)))
        self.sperre = False
        self._farben_alle(True)

    def _einstellungen(self):
        """Aktuellen Reglerstand als Parametersatz fuer photo2pixel.umwandeln."""
        typ = self.v["typ"].get()
        a = p2p.argumente(["-", "--typ", typ])
        try:
            b, h = int(self.v["breite"].get()), int(self.v["hoehe"].get())
        except (tk.TclError, ValueError):
            return None
        a.size = (min(640, max(4, b)), min(640, max(4, h)))
        a.bayer = self.v["bayer"].get()
        for name, *_ in REGLER_STIL + REGLER_BILD + REGLER_SCHNITT:
            setattr(a, name, self.v[name].get())
        for name in SCHALTER:
            setattr(a, name, bool(self.v[name].get()))
        if a.freistellen:
            a.alpha = True
        a.farben = [n for n in PALETTE_ORDER if self.farbe_an[n]]
        return a

    def _farbe(self, name: str) -> None:
        if self.farbe_an[name] and sum(self.farbe_an.values()) <= 2:
            self.status.config(text="Mindestens zwei Farben müssen aktiv bleiben.")
            return
        self.farbe_an[name] = not self.farbe_an[name]
        self._felder_zeichnen()
        self.geaendert()

    def _farben_alle(self, an: bool) -> None:
        for name in PALETTE_ORDER:
            self.farbe_an[name] = an
        self._felder_zeichnen()
        self.geaendert()

    def _farben_grau(self) -> None:
        for name in PALETTE_ORDER:
            self.farbe_an[name] = name in ("black", "dgrey", "grey", "lgrey", "white")
        self._felder_zeichnen()
        self.geaendert()

    def _felder_zeichnen(self) -> None:
        for name, feld in self.felder.items():
            an = self.farbe_an[name]
            hell = sum(C64[name]) > 380
            feld.config(text="✓" if an else "", relief="raised" if an else "sunken",
                        fg="#000000" if hell else "#ffffff")

    # ------------------------------------------------------------------ Dateien
    def oeffnen(self) -> None:
        pfad = filedialog.askopenfilename(title="Bild öffnen", filetypes=BILDTYPEN)
        if pfad:
            self.laden(Path(pfad))

    def laden(self, pfad: Path) -> None:
        try:
            with Image.open(pfad) as img:
                img.load()
                self._quelle_setzen(img, pfad)
        except (OSError, ValueError) as e:
            messagebox.showerror("Bild öffnen", f"{pfad.name} konnte nicht geladen werden:\n{e}")

    def einfuegen(self) -> None:
        try:
            inhalt = ImageGrab.grabclipboard()
        except Exception:                     # je nach System nicht verfuegbar
            inhalt = None
        if isinstance(inhalt, list) and inhalt:
            self.laden(Path(inhalt[0]))       # kopierte Datei aus dem Explorer
        elif isinstance(inhalt, Image.Image):
            self._quelle_setzen(inhalt, None)
        else:
            self.status.config(text="In der Zwischenablage liegt kein Bild.")

    def _quelle_setzen(self, img: Image.Image, pfad: Path | None) -> None:
        img = ImageOps.exif_transpose(img).convert("RGBA")
        gross = f"{img.width}×{img.height}"
        img.thumbnail((MAX_KANTE, MAX_KANTE), Image.LANCZOS)
        self.quelle, self.quellpfad = img, pfad
        self.root.title(f"Pixelkonverter – {pfad.name if pfad else 'Zwischenablage'}")
        self.status.config(text=f"Geladen: {pfad.name if pfad else 'Zwischenablage'} ({gross})")
        self.geaendert()

    def speichern(self) -> None:
        if self.quelle is None:
            self.status.config(text="Erst ein Bild öffnen.")
            return
        a = self._einstellungen()
        if a is None:
            return
        name = (self.quellpfad.stem if self.quellpfad else "bild") + "_c64.png"
        ordner = str(self.quellpfad.parent) if self.quellpfad else None
        ziel = filedialog.asksaveasfilename(title="Pixelgrafik speichern", defaultextension=".png",
                                            initialfile=name, initialdir=ordner,
                                            filetypes=[("Indiziertes PNG", "*.png")])
        if not ziel:
            return
        ziel = Path(ziel)
        try:
            bild = p2p.umwandeln(self.quelle, a)          # exakt der aktuelle Reglerstand
            p2p.speichern(bild, ziel)
            text = f"Gespeichert: {ziel.name}"
            if self.v["mit_vorschau"].get():
                faktor = max(2, min(12, 960 // max(bild.size)))
                gross = ziel.with_name(f"{ziel.stem}_x{faktor}.png")
                p2p.speichern(p2p.hochskalieren(bild, faktor), gross)
                text += f" und {gross.name}"
            self.status.config(text=text)
        except OSError as e:
            messagebox.showerror("Speichern", f"Speichern fehlgeschlagen:\n{e}")

    # --------------------------------------------------------------- Berechnung
    def geaendert(self) -> None:
        """Regler bewegt: Neuberechnung kurz verzoegert anstossen."""
        if self.sperre or self.quelle is None:
            return
        if self.timer:
            self.root.after_cancel(self.timer)
        self.timer = self.root.after(120, self._anstossen)

    def _anstossen(self) -> None:
        self.timer = None
        a = self._einstellungen()
        if a is None or self.quelle is None:
            return
        self.nummer += 1
        self.auftraege.put((self.nummer, self.quelle, a))
        self.info.config(text="rechne …")

    def _arbeiter(self) -> None:
        """Hintergrund-Thread: immer nur den neuesten Auftrag rechnen."""
        while True:
            auftrag = self.auftraege.get()
            while not self.auftraege.empty():
                auftrag = self.auftraege.get()
            nummer, quelle, a = auftrag
            try:
                self.fertig.put((nummer, p2p.umwandeln(quelle, a), None))
            except Exception as e:            # Fehler im Fenster zeigen statt still zu sterben
                self.fertig.put((nummer, None, e))

    def _abholen(self) -> None:
        try:
            while True:
                nummer, bild, fehler = self.fertig.get_nowait()
                if nummer != self.nummer:
                    continue                  # inzwischen ueberholt
                if fehler:
                    self.info.config(text="")
                    self.status.config(text=f"Fehler bei der Umwandlung: {fehler}")
                    continue
                self.ergebnis = bild
                farben = len({i for _, i in bild.getcolors()} - {p2p.TRANSPARENT})
                self.info.config(text=f"{bild.width}×{bild.height} Pixel, {farben} Farben")
                self._zeichnen()
        except queue.Empty:
            pass
        self.root.after(60, self._abholen)

    # ------------------------------------------------------------------ Anzeige
    def _zeichnen(self) -> None:
        self.bild.delete("all")
        cw, ch = self.bild.winfo_width(), self.bild.winfo_height()
        if cw < 20 or ch < 20:
            return
        if self.v["original"].get() and self.quelle is not None:
            zeig = self.quelle.copy()
            zeig.thumbnail((cw - 16, ch - 16), Image.LANCZOS)
        elif self.ergebnis is not None:
            b = self.ergebnis
            faktor = max(1, min((cw - 16) // b.width, (ch - 16) // b.height))
            zeig = b.convert("RGBA").resize((b.width * faktor, b.height * faktor), Image.NEAREST)
        else:
            self.bild.create_text(cw // 2, ch // 2, fill="#888888", text="Noch kein Bild geladen")
            return
        if zeig.getchannel("A").getextrema()[0] < 255:
            zeig = Image.alpha_composite(self._karo(zeig.size), zeig)
        self.foto = ImageTk.PhotoImage(zeig)
        self.bild.create_image(cw // 2, ch // 2, image=self.foto)

    @staticmethod
    def _karo(size, feld: int = 8) -> Image.Image:
        """Schachbrett als Hintergrund fuer transparente Flaechen."""
        klein = Image.new("RGBA", (2, 2), (70, 70, 80, 255))
        klein.putpixel((0, 0), (95, 95, 105, 255))
        klein.putpixel((1, 1), (95, 95, 105, 255))
        kachel = klein.resize((2 * feld, 2 * feld), Image.NEAREST)
        flaeche = Image.new("RGBA", size)
        for y in range(0, size[1], 2 * feld):
            for x in range(0, size[0], 2 * feld):
                flaeche.paste(kachel, (x, y))
        return flaeche


def main() -> None:
    try:                                      # scharfe Pixel auf hochaufloesenden Windows-Displays
        import ctypes
        ctypes.windll.shcore.SetProcessDpiAwareness(1)
    except Exception:
        pass
    root = tk.Tk()
    App(root, sys.argv[1] if len(sys.argv) > 1 else None)
    root.mainloop()


if __name__ == "__main__":
    main()
