"""
Asset-Generator im C64-Retro-Stil (Referenz: SKALD - Against the Black Priory).

Erzeugt alle Grafik-Assets des Endzeit-Wirtschaftssimulators nach den
Regeln aus TICKET-009/-010 in der revidierten Stilrichtung:

  * strikte 16-Farben-C64-Palette (VIC-II / Pepto)
  * Schattierung ausschliesslich ueber geordnetes Bayer-Dithering
  * tiefschwarze Hintergruende, harte Kanten, keine Weichzeichnung
  * ornamentale Rahmen und Bitmap-Pixelfont fuer alle Beschriftungen

Ausgabe:
  design/assets/artstyle-c64/native/*.png   native Pixelgroesse (Spiel/Prototyp)
  design/assets/artstyle-c64/*.png          hochskalierte Vorschau (Tickets)

Aufruf:  python tools/gen_assets_c64.py
"""

from __future__ import annotations

import math
import random
from pathlib import Path

from PIL import Image, ImageDraw

REPO = Path(__file__).resolve().parent.parent
OUT = REPO / "design" / "assets" / "artstyle-c64"
NATIVE = OUT / "native"

# --------------------------------------------------------------------------
# C64-Palette (VIC-II, Pepto-Kalibrierung) - die einzige erlaubte Farbquelle
# --------------------------------------------------------------------------
C64 = {
    "black":  (0, 0, 0),
    "white":  (255, 255, 255),
    "red":    (136, 0, 0),
    "cyan":   (170, 255, 238),
    "purple": (204, 68, 204),
    "green":  (0, 204, 85),
    "blue":   (0, 0, 170),
    "yellow": (238, 238, 119),
    "orange": (221, 136, 85),
    "brown":  (102, 68, 0),
    "lred":   (255, 119, 119),
    "dgrey":  (51, 51, 51),
    "grey":   (119, 119, 119),
    "lgreen": (170, 255, 102),
    "lblue":  (0, 136, 255),
    "lgrey":  (187, 187, 187),
}

PALETTE_ORDER = [
    "black", "dgrey", "grey", "lgrey", "white", "blue", "lblue", "cyan",
    "purple", "lred", "red", "brown", "orange", "yellow", "green", "lgreen",
]

BAYER8 = (
    (0, 32, 8, 40, 2, 34, 10, 42),
    (48, 16, 56, 24, 50, 18, 58, 26),
    (12, 44, 4, 36, 14, 46, 6, 38),
    (60, 28, 52, 20, 62, 30, 54, 22),
    (3, 35, 11, 43, 1, 33, 9, 41),
    (51, 19, 59, 27, 49, 17, 57, 25),
    (15, 47, 7, 39, 13, 45, 5, 37),
    (63, 31, 55, 23, 61, 29, 53, 21),
)


def bayer(x: int, y: int) -> float:
    """Schwellwert 0..1 der 8x8-Bayer-Matrix an Position (x, y)."""
    return (BAYER8[y & 7][x & 7] + 0.5) / 64.0


def col(c) -> tuple:
    """Farbname oder RGB(A)-Tupel -> RGBA."""
    if isinstance(c, str):
        r, g, b = C64[c]
        return (r, g, b, 255)
    if len(c) == 3:
        return (c[0], c[1], c[2], 255)
    return tuple(c)


# --------------------------------------------------------------------------
# 3x5-Pixelfont (Grossbuchstaben, Ziffern, wenige Sonderzeichen)
# --------------------------------------------------------------------------
FONT = {
    "A": ("###", "#.#", "###", "#.#", "#.#"),
    "B": ("##.", "#.#", "##.", "#.#", "##."),
    "C": (".##", "#..", "#..", "#..", ".##"),
    "D": ("##.", "#.#", "#.#", "#.#", "##."),
    "E": ("###", "#..", "##.", "#..", "###"),
    "F": ("###", "#..", "##.", "#..", "#.."),
    "G": (".##", "#..", "#.#", "#.#", ".##"),
    "H": ("#.#", "#.#", "###", "#.#", "#.#"),
    "I": ("###", ".#.", ".#.", ".#.", "###"),
    "J": ("..#", "..#", "..#", "#.#", ".#."),
    "K": ("#.#", "#.#", "##.", "#.#", "#.#"),
    "L": ("#..", "#..", "#..", "#..", "###"),
    "M": ("#.#", "###", "###", "#.#", "#.#"),
    "N": ("#.#", "###", "###", "###", "#.#"),
    "O": (".#.", "#.#", "#.#", "#.#", ".#."),
    "P": ("##.", "#.#", "##.", "#..", "#.."),
    "Q": (".#.", "#.#", "#.#", "##.", ".##"),
    "R": ("##.", "#.#", "##.", "#.#", "#.#"),
    "S": (".##", "#..", ".#.", "..#", "##."),
    "T": ("###", ".#.", ".#.", ".#.", ".#."),
    "U": ("#.#", "#.#", "#.#", "#.#", "###"),
    "V": ("#.#", "#.#", "#.#", "#.#", ".#."),
    "W": ("#.#", "#.#", "###", "###", "#.#"),
    "X": ("#.#", "#.#", ".#.", "#.#", "#.#"),
    "Y": ("#.#", "#.#", ".#.", ".#.", ".#."),
    "Z": ("###", "..#", ".#.", "#..", "###"),
    "0": ("###", "#.#", "#.#", "#.#", "###"),
    "1": (".#.", "##.", ".#.", ".#.", "###"),
    "2": ("##.", "..#", ".#.", "#..", "###"),
    "3": ("##.", "..#", ".#.", "..#", "##."),
    "4": ("#.#", "#.#", "###", "..#", "..#"),
    "5": ("###", "#..", "##.", "..#", "##."),
    "6": (".##", "#..", "###", "#.#", "###"),
    "7": ("###", "..#", ".#.", ".#.", ".#."),
    "8": ("###", "#.#", "###", "#.#", "###"),
    "9": ("###", "#.#", "###", "..#", "##."),
    " ": ("...", "...", "...", "...", "..."),
    ".": ("...", "...", "...", "...", ".#."),
    ",": ("...", "...", "...", ".#.", "#.."),
    ":": ("...", ".#.", "...", ".#.", "..."),
    "-": ("...", "...", "###", "...", "..."),
    "!": (".#.", ".#.", ".#.", "...", ".#."),
    "?": ("##.", "..#", ".#.", "...", ".#."),
    "/": ("..#", "..#", ".#.", "#..", "#.."),
    "'": (".#.", ".#.", "...", "...", "..."),
    "+": ("...", ".#.", "###", ".#.", "..."),
    "%": ("#.#", "..#", ".#.", "#..", "#.#"),
}


def text_width(text: str, scale: int = 1, spacing: int = 1) -> int:
    if not text:
        return 0
    return (len(text) * (3 * scale + spacing)) - spacing


# --------------------------------------------------------------------------
# Zeichen-Canvas
# --------------------------------------------------------------------------
class Canvas:
    def __init__(self, w: int, h: int, bg=None):
        self.w, self.h = w, h
        self.img = Image.new("RGBA", (w, h), (0, 0, 0, 0) if bg is None else col(bg))
        self.px = self.img.load()

    # -- Grundprimitive ----------------------------------------------------
    def set(self, x: int, y: int, c) -> None:
        if c is not None and 0 <= x < self.w and 0 <= y < self.h:
            self.px[x, y] = col(c)

    def get(self, x: int, y: int):
        if 0 <= x < self.w and 0 <= y < self.h:
            return self.px[x, y]
        return (0, 0, 0, 0)

    def rect(self, x0, y0, x1, y1, c) -> None:
        for y in range(int(y0), int(y1) + 1):
            for x in range(int(x0), int(x1) + 1):
                self.set(x, y, c)

    def frame(self, x0, y0, x1, y1, c) -> None:
        for x in range(int(x0), int(x1) + 1):
            self.set(x, int(y0), c)
            self.set(x, int(y1), c)
        for y in range(int(y0), int(y1) + 1):
            self.set(int(x0), y, c)
            self.set(int(x1), y, c)

    def hline(self, x0, x1, y, c) -> None:
        for x in range(int(min(x0, x1)), int(max(x0, x1)) + 1):
            self.set(x, int(y), c)

    def vline(self, x, y0, y1, c) -> None:
        for y in range(int(min(y0, y1)), int(max(y0, y1)) + 1):
            self.set(int(x), y, c)

    def line(self, x0, y0, x1, y1, c) -> None:
        steps = int(max(abs(x1 - x0), abs(y1 - y0))) or 1
        for i in range(steps + 1):
            self.set(round(x0 + (x1 - x0) * i / steps),
                     round(y0 + (y1 - y0) * i / steps), c)

    # -- Dithering ---------------------------------------------------------
    def dither(self, x0, y0, x1, y1, c1, c2, t) -> None:
        """Flaeche mit Bayer-Dithering zwischen c1 (t=0) und c2 (t=1) fuellen.

        t ist entweder eine Zahl oder eine Funktion (x, y) -> 0..1.
        c1 oder c2 duerfen None sein (dann bleibt der Pixel unveraendert).
        """
        fn = t if callable(t) else (lambda _x, _y, _v=t: _v)
        for y in range(int(y0), int(y1) + 1):
            for x in range(int(x0), int(x1) + 1):
                c = c2 if fn(x, y) > bayer(x, y) else c1
                if c is not None:
                    self.set(x, y, c)

    def noise(self, x0, y0, x1, y1, c, prob, rng) -> None:
        for y in range(int(y0), int(y1) + 1):
            for x in range(int(x0), int(x1) + 1):
                if rng.random() < prob:
                    self.set(x, y, c)

    # -- Masken (Ellipsen, Polygone) ---------------------------------------
    def mask(self, drawfn):
        m = Image.new("L", (self.w, self.h), 0)
        drawfn(ImageDraw.Draw(m))
        return m.load()

    def paint(self, maskpx, x0, y0, x1, y1, c1, c2=None, t=0.0) -> None:
        """Maskierte Flaeche dithern; c2=None => Volltonfarbe c1."""
        fn = t if callable(t) else (lambda _x, _y, _v=t: _v)
        x0, y0 = max(0, int(x0)), max(0, int(y0))
        x1, y1 = min(self.w - 1, int(x1)), min(self.h - 1, int(y1))
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                if not maskpx[x, y]:
                    continue
                c = c1 if c2 is None else (c2 if fn(x, y) > bayer(x, y) else c1)
                if c is not None:
                    self.set(x, y, c)

    def ellipse(self, x0, y0, x1, y1, c1, c2=None, t=0.0) -> None:
        m = self.mask(lambda d: d.ellipse([x0, y0, x1, y1], fill=255))
        self.paint(m, x0, y0, x1, y1, c1, c2, t)

    def ellipse_frame(self, x0, y0, x1, y1, c) -> None:
        m = self.mask(lambda d: d.ellipse([x0, y0, x1, y1], outline=255))
        self.paint(m, x0, y0, x1, y1, c)

    def polygon(self, points, c1, c2=None, t=0.0) -> None:
        xs = [p[0] for p in points]
        ys = [p[1] for p in points]
        m = self.mask(lambda d: d.polygon(points, fill=255))
        self.paint(m, min(xs), min(ys), max(xs), max(ys), c1, c2, t)

    # -- Text --------------------------------------------------------------
    def text(self, x: int, y: int, s: str, c, scale: int = 1,
             spacing: int = 1, shadow=None) -> int:
        cx = x
        for ch in s.upper():
            glyph = FONT.get(ch, FONT["?"])
            for gy, row in enumerate(glyph):
                for gx, bit in enumerate(row):
                    if bit != "#":
                        continue
                    for sy in range(scale):
                        for sx in range(scale):
                            px_, py_ = cx + gx * scale + sx, y + gy * scale + sy
                            if shadow is not None:
                                self.set(px_ + scale, py_ + scale, shadow)
                            self.set(px_, py_, c)
            cx += 3 * scale + spacing
        return cx

    def text_centered(self, cx: int, y: int, s: str, c, scale: int = 1,
                      spacing: int = 1, shadow=None) -> None:
        self.text(cx - text_width(s, scale, spacing) // 2, y, s, c, scale,
                  spacing, shadow)

    # -- Export ------------------------------------------------------------
    def save(self, name: str, preview_scale: int = 3) -> None:
        NATIVE.mkdir(parents=True, exist_ok=True)
        OUT.mkdir(parents=True, exist_ok=True)
        self.img.save(NATIVE / name)
        big = self.img.resize(
            (self.w * preview_scale, self.h * preview_scale), Image.NEAREST)
        big.save(OUT / name)
        print(f"  {name:38s} native {self.w:3d}x{self.h:3d}  "
              f"preview {big.width}x{big.height}")


# --------------------------------------------------------------------------
# Wiederverwendbare Bausteine
# --------------------------------------------------------------------------
def ornate_frame(c: Canvas, x0, y0, x1, y1, base="brown", lite="orange",
                 gem="lred", edge="dgrey") -> None:
    """Ornamentaler 6px-Rahmen im Stil klassischer 8-Bit-RPG-Oberflaechen."""
    c.frame(x0, y0, x1, y1, edge)
    c.frame(x0 + 1, y0 + 1, x1 - 1, y1 - 1, lite)
    for i in (2, 3, 4):
        c.frame(x0 + i, y0 + i, x1 - i, y1 - i, base)
    c.frame(x0 + 5, y0 + 5, x1 - 5, y1 - 5, edge)

    # Motiv: alle 6 Pixel ein heller Nagelkopf auf dem Band
    for x in range(x0 + 4, x1 - 3, 6):
        c.set(x, y0 + 3, lite)
        c.set(x, y1 - 3, lite)
    for y in range(y0 + 4, y1 - 3, 6):
        c.set(x0 + 3, y, lite)
        c.set(x1 - 3, y, lite)

    # Eckornamente
    for (ox, oy, sx, sy) in ((x0, y0, 1, 1), (x1, y0, -1, 1),
                             (x0, y1, 1, -1), (x1, y1, -1, -1)):
        c.rect(ox + sx * 1, oy + sy * 1, ox + sx * 4, oy + sy * 4, base)
        c.rect(ox + sx * 2, oy + sy * 2, ox + sx * 3, oy + sy * 3, gem)
        c.set(ox + sx * 2, oy + sy * 2, "yellow")


def plate(c: Canvas, cx: int, y: int, label: str, scale: int = 1,
          bg="red", fg="yellow", border="lgrey") -> None:
    """Beschriftetes Schild (Titelleiste) mittig auf x=cx."""
    w = text_width(label, scale) + 10
    h = 5 * scale + 6
    x0, x1 = cx - w // 2, cx + w // 2
    c.rect(x0, y, x1, y + h, bg)
    c.frame(x0, y, x1, y + h, border)
    c.frame(x0 + 1, y + 1, x1 - 1, y + h - 1, "dgrey")
    c.text_centered(cx, y + 3, label, fg, scale, shadow="black")


def starfield(c: Canvas, x0, y0, x1, y1, rng, density=0.012) -> None:
    for y in range(int(y0), int(y1) + 1):
        for x in range(int(x0), int(x1) + 1):
            r = rng.random()
            if r < density * 0.25:
                c.set(x, y, "white")
            elif r < density:
                c.set(x, y, "lgrey")



# --------------------------------------------------------------------------
# Handgesetzte Gesichtszuege
# --------------------------------------------------------------------------
# Brauen, Augen, Nase und Mund werden nicht berechnet, sondern als feste
# Pixelvorlage gesetzt - nur so bekommen die Gesichter bei 16x16..24x26
# Kopfgroesse einen lebendigen, nicht "puppenhaften" Ausdruck.
#   K = schwarz      S = Hautschatten   H = Lichtkante
#   E = Sklera       P = Pupille        . = unveraendert
FACE_FEATURES = (
    ".KKKK.......KKKK.",   # Brauen, aussen abfallend
    "KSSSK.......KSSSK",   # Oberlidschatten
    "KEPEK.......KEPEK",   # Augen
    ".SSK.........KSS.",   # Unterlid / Traenensack
    "........H........",   # Nasenruecken
    ".......HS........",
    ".......HS........",
    "......KHSK.......",   # Nasenfluegel
    ".......SS........",   # Schatten unter der Nase
    ".................",
    "......KKKKK......",   # Mundlinie
    "......SHHHS......",   # Unterlippe im Licht
    ".......SSS.......",   # Schatten darunter
)
FEATURE_W = len(FACE_FEATURES[0])
FEATURE_EYE_ROW = 2        # Zeile, die auf die Augenhoehe ausgerichtet wird


def stamp_features(c: Canvas, cx: int, ey: int, skin, squint=False) -> None:
    """Setzt die Gesichtsvorlage mittig auf (cx, ey)."""
    tone = {"K": "black", "S": skin[1], "H": "yellow",
            "E": "lgrey", "P": "black"}
    x0 = cx - FEATURE_W // 2
    y0 = ey - FEATURE_EYE_ROW
    for gy, row in enumerate(FACE_FEATURES):
        for gx, ch in enumerate(row):
            if ch == ".":
                continue
            if squint and ch in ("E", "P"):
                ch = "K" if ch == "E" else "P"
            c.set(x0 + gx, y0 + gy, tone[ch])
    if not squint:      # ein einzelner Lichtpunkt im linken Auge
        c.set(x0 + 1, y0 + FEATURE_EYE_ROW, "white")


def draw_face(c: Canvas, x, y, w, h, skin=("orange", "brown"),
              hair="brown", cloth=("blue", "lblue"), accent="lgrey",
              headgear=None, third_eye=False, bg=("blue", "black"),
              aged=False, gaunt=1.0, stubble=False, scarred=False,
              mottled=False, pallor=0.0, squint=False, seed=0) -> None:
    """Portraet-Bueste im Stil von SKALD / Burntime.

    Kein glatter Puppenkopf, sondern ein Schaedel mit Struktur: harte
    Licht-/Schattentrennung, tief liegende Augen in dunklen Hoehlen,
    eingefallene Wangen (``gaunt``), Verwitterung ueber Stoppeln, Narben,
    Flecken und Falten. Detailgrad skaliert mit der Portraetgroesse.
    """
    x, y, w, h = int(x), int(y), int(w), int(h)
    x1, y1 = x + w - 1, y + h - 1
    cx = x + w // 2
    detail = w >= 34           # Einzelportraet und groesser
    small = w < 50             # wenig Platz -> Effekte ausduennen
    fine = w >= 70             # Dialogformat
    rng = random.Random(seed or (w * 131 + h * 17))

    def clamp01(v):
        return 0.0 if v < 0 else (1.0 if v > 1 else v)

    # --- Hintergrund: Kopf taucht aus dem Schwarz auf ---------------------
    c.dither(x, y, x1, y1, bg[0], bg[1],
             lambda px, py: clamp01((py - y) / max(1, h) * 1.7 - 0.1))
    c.dither(x, y, x1, y1, None, "black",
             lambda px, py: clamp01((abs(px - cx) / (w / 2.0)) ** 2 * 1.25 - 0.12))

    # --- Schaedelgeometrie -------------------------------------------------
    hw, hh = int(w * 0.56), int(h * 0.60)
    hx0 = cx - hw // 2
    hy0 = y + int(h * 0.08)
    hx1, hy1 = hx0 + hw - 1, hy0 + hh - 1
    chin = hy1
    jaw_y = hy0 + int(hh * 0.56)
    jaw_w = max(4, int(hw * (0.78 - 0.13 * gaunt)))

    def head_shape(d):
        # Hirnschaedel breit, Kiefer schmal zulaufend
        d.ellipse([hx0, hy0, hx1, jaw_y + int(hh * 0.40)], fill=255)
        d.polygon([(hx0 + 1, jaw_y - 2), (hx1 - 1, jaw_y - 2),
                   (cx + jaw_w // 2, chin - int(hh * 0.12)),
                   (cx + jaw_w // 3, chin),
                   (cx - jaw_w // 3, chin),
                   (cx - jaw_w // 2, chin - int(hh * 0.12))], fill=255)

    head = c.mask(head_shape)
    HB = (hx0 - 2, hy0 - 2, hx1 + 2, chin + 2)

    def anat(drawfn, color, t, box=None):
        """Anatomiedetail, hart auf die Kopfflaeche begrenzt."""
        m = c.mask(drawfn)
        fn = t if callable(t) else (lambda _a, _b, _v=t: _v)
        bx0, by0, bx1, by1 = box or HB
        for yy in range(max(0, int(by0)), min(c.h - 1, int(by1)) + 1):
            for xx in range(max(0, int(bx0)), min(c.w - 1, int(bx1)) + 1):
                if m[xx, yy] and head[xx, yy] and fn(xx, yy) > bayer(xx, yy):
                    c.set(xx, yy, color)

    # --- Schultern / Kleidung ---------------------------------------------
    sh_top = y + int(h * 0.76)
    c.polygon([(x, y1), (x + int(w * 0.14), sh_top - int(h * 0.05)),
               (cx - int(w * 0.11), sh_top - int(h * 0.08)),
               (cx + int(w * 0.11), sh_top - int(h * 0.08)),
               (x1 - int(w * 0.14), sh_top - int(h * 0.05)), (x1, y1)],
              cloth[0], cloth[1],
              lambda px, py: max(0.0, 0.62 - 0.95 * (px - x) / w))
    c.polygon([(x, y1), (x + int(w * 0.14), sh_top - int(h * 0.05)),
               (cx - int(w * 0.11), sh_top - int(h * 0.08)),
               (cx + int(w * 0.11), sh_top - int(h * 0.08)),
               (x1 - int(w * 0.14), sh_top - int(h * 0.05)), (x1, y1)],
              None, "black",
              lambda px, py: max(0.0, (px - cx) / (w / 2.0) * 0.9 - 0.25))
    for fx in (x + int(w * 0.20), x1 - int(w * 0.20)):
        c.line(fx, y1, fx + int(w * 0.04), sh_top, "black")
    c.line(cx - int(w * 0.12), sh_top - int(h * 0.07), cx, sh_top + int(h * 0.05), accent)
    c.line(cx + int(w * 0.12), sh_top - int(h * 0.07), cx, sh_top + int(h * 0.05), accent)

    # --- Hals: sehnig, tief im Schatten ------------------------------------
    nk = max(2, int(w * 0.11))
    c.rect(cx - nk, chin - 2, cx + nk, sh_top, skin[0])
    c.dither(cx - nk, chin - 2, cx + nk, sh_top, None, skin[1], 0.55)
    c.dither(cx - nk, chin - 2, cx + nk, chin + max(2, int(h * 0.07)),
             "black", None,
             lambda px, py: max(0.0, 1.0 - (py - (chin - 2)) / max(1.0, h * 0.09)))
    c.dither(cx, chin - 2, cx + nk, sh_top, None, "black", 0.5)
    if detail:   # Sehnen
        c.vline(cx - nk + 1, chin + 2, sh_top - 1, skin[1])
        c.vline(cx + 1, chin + 2, sh_top - 1, "black")

    # --- Grundton und harte Lichttrennung ----------------------------------
    def lum(px, py):
        nx = (px - cx) / (hw / 2.0)
        ny = (py - (hy0 + hh / 2.0)) / (hh / 2.0)
        return 0.44 + 0.42 * nx + 0.16 * ny

    c.paint(head, *HB, skin[0])
    if pallor:
        c.paint(head, *HB, None, "lgrey", pallor)
    c.paint(head, *HB, None, "yellow", lambda px, py: clamp01((0.28 - lum(px, py)) * 7.0))
    c.paint(head, *HB, None, skin[1], lambda px, py: clamp01((lum(px, py) - 0.52) * 7.0))
    c.paint(head, *HB, None, "black", lambda px, py: clamp01((lum(px, py) - 0.74) * 7.0))

    # Kieferkante: dunkle Silhouettenlinie gegen den Hintergrund
    for yy in range(hy0, chin + 1):
        row = [xx for xx in range(hx0 - 2, hx1 + 3) if head[xx, yy]]
        if row:
            c.set(row[0], yy, "black")
            c.set(row[-1], yy, "black")

    # --- Merkpunkte --------------------------------------------------------
    brow_y = hy0 + int(hh * 0.40)
    ey = hy0 + int(hh * 0.47)
    eoff = max(2, int(hw * 0.22))
    ew = max(0, int(hw * 0.10))
    nose_b = ey + max(2, int(hh * 0.17))
    mouth_y = hy0 + int(hh * 0.75)
    mw = max(1, int(hw * 0.11))
    tilt = 1 if (seed % 2 == 0) else -1        # leichte Asymmetrie

    # Schlaefenhoehlen (nur wenn genug Platz ist)
    for sx in ((-1, 1) if not small else ()):
        tx = cx + sx * int(hw * 0.40)
        anat(lambda d, _t=tx: d.ellipse(
            [_t - int(hw * 0.14), hy0 + int(hh * 0.18),
             _t + int(hw * 0.14), brow_y + 1], fill=255),
            skin[1], 0.55)

    # Augenhoehlen - der wichtigste Strukturschatten
    for sx in (-1, 1):
        exc = cx + sx * eoff
        anat(lambda d, _e=exc: d.ellipse(
            [_e - ew - max(1, int(hw * 0.10)), ey - max(1, int(hh * 0.06)),
             _e + ew + max(1, int(hw * 0.08)), ey + max(2, int(hh * 0.08))], fill=255),
            "black", 0.55 if detail else 0.45)

    # Eingefallene Wangen: schraege Hoehle unter dem Jochbein
    for sx in (-1, 1):
        chx = cx + sx * int(hw * 0.30)
        chy = ey + int(hh * 0.20)
        anat(lambda d, _x=chx, _y=chy, _s=sx: d.polygon(
            [(_x - _s * int(hw * 0.20), _y),
             (_x + _s * int(hw * 0.16), _y + int(hh * 0.06)),
             (cx + _s * int(hw * 0.16), mouth_y + int(hh * 0.04)),
             (cx + _s * int(hw * 0.06), mouth_y + int(hh * 0.02))], fill=255),
            skin[1], clamp01(0.60 * gaunt))
        if not small:
            anat(lambda d, _x=chx, _y=chy, _s=sx: d.polygon(
                [(_x - _s * int(hw * 0.14), _y + 1),
                 (_x + _s * int(hw * 0.10), _y + int(hh * 0.06)),
                 (cx + _s * int(hw * 0.14), mouth_y),
                 (cx + _s * int(hw * 0.08), mouth_y - int(hh * 0.02))], fill=255),
                "black", clamp01(0.42 * gaunt))

    # Jochbeinkante darueber aufhellen
    if detail:
        for sx in (-1, 1):
            jx = cx + sx * int(hw * 0.30)
            anat(lambda d, _x=jx: d.ellipse(
                [_x - int(hw * 0.16), ey + int(hh * 0.08),
                 _x + int(hw * 0.16), ey + int(hh * 0.20)], fill=255),
                "yellow", 0.45 if sx < 0 else 0.15)

    # Kinnlicht und Kieferschatten
    if detail:
        anat(lambda d: d.ellipse([cx - int(hw * 0.14), chin - int(hh * 0.13),
                                  cx + int(hw * 0.10), chin - int(hh * 0.04)],
                                 fill=255), "yellow", 0.4)
        anat(lambda d: d.rectangle([hx0, chin - int(hh * 0.05), hx1, chin + 2],
                                   fill=255), "black", 0.45)

    # --- Verwitterung ------------------------------------------------------
    if aged and detail:
        for i, fy_ in enumerate((0.24,)):
            yy = hy0 + int(hh * fy_)
            off = rng.randint(-2, 2)
            c.hline(cx - int(hw * (0.22 - i * 0.05)) + off,
                    cx + int(hw * (0.14 - i * 0.04)) + off, yy, skin[1])
            c.set(cx - int(hw * (0.22 - i * 0.05)) + off - 1, yy, "black")
        for sx in (-1, 1):   # Nasolabialfalten
            c.line(cx + sx * int(hw * 0.14), nose_b,
                   cx + sx * int(hw * 0.26), mouth_y + int(hh * 0.04), skin[1])

    if stubble and detail and not small:
        anat(lambda d: d.ellipse(
            [cx - int(hw * 0.42), mouth_y + 1,
             cx + int(hw * 0.42), chin + 2], fill=255), "black", 0.22)

    if mottled and detail:
        for _ in range(max(2, int(hw * 0.09))):
            mx = rng.randint(hx0 + 1, hx1 - 1)
            my = rng.randint(brow_y, mouth_y)
            r = rng.randint(1, max(1, int(hw * 0.05)))
            tone = "green" if rng.random() < 0.5 else "dgrey"
            anat(lambda d, _x=mx, _y=my, _r=r: d.ellipse(
                [_x - _r, _y - _r, _x + _r, _y + _r], fill=255), tone, 0.22)

    if scarred and detail:
        sx0 = cx + tilt * int(hw * 0.12)
        sy0 = brow_y - int(hh * 0.10)
        sx1_ = cx + tilt * int(hw * 0.40)
        sy1_ = ey + int(hh * 0.18)
        c.line(sx0, sy0, sx1_, sy1_, "lred")
        c.line(sx0 + tilt, sy0, sx1_ + tilt, sy1_, "black")

    # Hautstruktur: sparsames Rauschen statt glatter Flaeche
    if detail:
        anat(lambda d: d.rectangle([hx0, hy0, hx1, chin], fill=255),
             skin[1], 0.04)

    # --- Gesichtszuege aus der handgesetzten Vorlage ---------------------
    stamp_features(c, cx, ey, skin, squint=squint)

    # --- Drittes Auge (Mutierte): eigene Hoehle, senkrechte Pupille --------
    if third_eye:
        ty = hy0 + int(hh * 0.25)
        tw = max(1, int(hw * 0.06))
        anat(lambda d: d.ellipse([cx - tw - 2, ty - tw - 1, cx + tw + 2, ty + tw + 1],
                                 fill=255), "black", 0.75)
        c.hline(cx - tw, cx + tw, ty, "black")
        c.set(cx, ty, "green")
        if detail:
            c.vline(cx, ty - 1, ty + 1, "black")
            c.set(cx, ty, "lgreen")
            c.hline(cx - tw - 1, cx + tw + 1, ty - tw - 1, hair)
            c.hline(cx - tw, cx + tw, ty + 1, skin[1])

    # --- Haare: unregelmaessige Masse, bei Alter zurueckweichend ----------
    if headgear not in ("helmet", "hood"):
        hair_col = "grey" if aged else hair
        top = hy0 + int(hh * (0.30 if not aged else 0.20))
        if third_eye:
            top = hy0 + int(hh * 0.15)
        cap = c.mask(lambda d: d.ellipse(
            [hx0 - 2, hy0 - max(2, int(hh * 0.09)), hx1 + 2, hy0 + int(hh * 0.95)],
            fill=255))
        c.paint(cap, hx0 - 2, hy0 - 3, hx1 + 2, top, hair_col, "black",
                0.50 if aged else 0.40)
        c.paint(cap, hx0 - 2, top - max(1, int(hh * 0.05)), hx1 + 2, top,
                None, "black", 0.6)
        c.paint(cap, hx0 - 2, hy0 - 3, hx1 + 2, top, None,
                "lgrey" if aged else "black",
                lambda px, py: clamp01((0.30 - lum(px, py)) * 2.0))
        # Geheimratsecken
        if aged and detail:
            for sx in (-1, 1):
                ax = cx + sx * int(hw * 0.14)
                bx = cx + sx * int(hw * 0.52)
                anat(lambda d, _a=min(ax, bx), _b=max(ax, bx): d.ellipse(
                    [_a, hy0 - 2, _b, hy0 + int(hh * 0.22)], fill=255),
                    skin[1], 0.7)
        # Schlaefenpartien
        for sx in (-1, 1):
            sxx = cx + sx * int(hw * 0.44)
            c.paint(cap, min(sxx, sxx + sx * 3), top - 2,
                    max(sxx, sxx + sx * 3), hy0 + int(hh * (0.70 if not aged else 0.58)),
                    hair_col, "black", 0.5)
        if fine:   # Straehnen, ungleichmaessig
            for i in range(7):
                sx0 = hx0 + int(hw * (0.10 + i * 0.13))
                c.line(sx0, hy0 - 2, sx0 - rng.randint(1, 3), top - 1, "black")

    # --- Kopfbedeckung ------------------------------------------------------
    if headgear == "helmet":
        brim = hy0 + int(hh * 0.22)
        hm = c.mask(lambda d: d.ellipse(
            [hx0 - 3, hy0 - max(2, int(hh * 0.10)), hx1 + 3, hy0 + int(hh * 0.48)],
            fill=255))
        c.paint(hm, hx0 - 3, hy0 - 5, hx1 + 3, brim, "grey")
        c.paint(hm, hx0 - 3, hy0 - 5, hx1 + 3, brim, None, "lgrey",
                lambda px, py: clamp01((0.32 - lum(px, py)) * 2.6))
        c.paint(hm, hx0 - 3, hy0 - 5, hx1 + 3, brim, None, "dgrey",
                lambda px, py: clamp01((lum(px, py) - 0.55) * 2.8))
        c.paint(hm, hx0 - 3, hy0 - 5, hx1 + 3, brim, None, "black",
                lambda px, py: clamp01((lum(px, py) - 0.80) * 3.4))
        c.hline(hx0 - 3, hx1 + 3, brim, "black")
        c.hline(hx0 - 2, hx1 + 2, brim - 1, "lgrey")
        # Schatten des Helmrands auf der Stirn
        anat(lambda d: d.rectangle([hx0, brim + 1, hx1, brim + max(2, int(hh * 0.09))],
                                   fill=255), "black", 0.5)
        if detail:
            c.set(cx + int(hw * 0.26), hy0 + int(hh * 0.10), "lred")
    elif headgear == "hood":
        # Gesicht sichern, Kapuze darueber zeichnen, Oeffnung wiederherstellen
        snap = c.img.copy().load()
        c.polygon([(hx0 - 5, y1), (hx0 - 5, hy0 + int(hh * 0.26)),
                   (cx - int(hw * 0.26), hy0 - int(hh * 0.20)),
                   (cx + int(hw * 0.26), hy0 - int(hh * 0.20)),
                   (hx1 + 5, hy0 + int(hh * 0.26)), (hx1 + 5, y1)],
                  "brown", "black",
                  lambda px, py: clamp01((px - hx0) / hw * 1.2 - 0.20))
        opening = c.mask(lambda d: d.ellipse(
            [hx0 + 1, hy0 + int(hh * 0.08), hx1 - 1, chin + 1], fill=255))
        for yy in range(max(0, hy0), min(c.h - 1, chin + 2) + 1):
            for xx in range(max(0, hx0), min(c.w - 1, hx1) + 1):
                if opening[xx, yy] and head[xx, yy]:
                    c.px[xx, yy] = snap[xx, yy]
        # Kapuze wirft Schatten auf die obere Gesichtshaelfte
        for yy in range(max(0, hy0), min(c.h - 1, brow_y + 1) + 1):
            for xx in range(max(0, hx0), min(c.w - 1, hx1) + 1):
                if opening[xx, yy] and head[xx, yy] and \
                        clamp01(0.70 - (yy - hy0) / max(1.0, hh * 0.40)) > bayer(xx, yy):
                    c.set(xx, yy, "black")
        # Dunkler Innenrand der Kapuze
        rim = c.mask(lambda d: d.ellipse(
            [hx0 - 1, hy0 + int(hh * 0.05), hx1 + 1, chin + 2], outline=255, width=2))
        for yy in range(max(0, hy0), min(c.h - 1, chin + 3) + 1):
            for xx in range(max(0, hx0 - 2), min(c.w - 1, hx1 + 2) + 1):
                if rim[xx, yy]:
                    c.set(xx, yy, "black")
        stamp_features(c, cx, ey, skin, squint=squint)
        # Kapuzensaum als Lichtkante
        c.line(hx0 - 2, hy0 + int(hh * 0.30), cx, hy0 - int(hh * 0.16), "orange")
        c.line(hx1 + 2, hy0 + int(hh * 0.30), cx, hy0 - int(hh * 0.16), "brown")


# --------------------------------------------------------------------------
# 01 - Palettenblatt
# --------------------------------------------------------------------------
def gen_palette() -> None:
    cell, cols = 18, 8
    w = cols * cell + 8
    h = 2 * cell + 8 + 30
    c = Canvas(w, h, "black")
    c.text(5, 4, "C64 BASISPALETTE", "yellow")
    for i, name in enumerate(PALETTE_ORDER):
        cx = 4 + (i % cols) * cell
        cy = 12 + (i // cols) * cell
        c.rect(cx, cy, cx + cell - 3, cy + cell - 3, name)
        c.frame(cx, cy, cx + cell - 3, cy + cell - 3, "dgrey")

    # Dither-Rampen als Beleg fuer die Mischtoene
    y = 12 + 2 * cell + 4
    c.text(5, y, "DITHER RAMPEN", "yellow")
    ramps = [("black", "blue"), ("blue", "lblue"), ("lblue", "cyan"),
             ("brown", "orange"), ("orange", "yellow"), ("green", "lgreen"),
             ("red", "lred"), ("dgrey", "lgrey")]
    ry = y + 8
    seg = (w - 8) // len(ramps)
    for i, (a, b) in enumerate(ramps):
        x0 = 4 + i * seg
        c.dither(x0, ry, x0 + seg - 2, ry + 9, a, b,
                 lambda px, py, _x0=x0: (px - _x0) / max(1, seg - 2))
    c.save("01_palette.png", preview_scale=4)


# --------------------------------------------------------------------------
# 02 - Hauptkarte (Weltkarte im SKALD-Stil)
# --------------------------------------------------------------------------
def gen_map() -> None:
    w, h = 240, 152
    c = Canvas(w, h, "black")
    rng = random.Random(1993)

    # --- Meer: dunkles Blau auf Schwarz, mit Wellenstrichen ---------------
    c.dither(0, 0, w - 1, h - 1, "black", "blue", 0.45)
    for y in range(2, h, 5):
        for x in range(0, w, 11):
            if rng.random() < 0.55:
                ox = rng.randint(0, 5)
                c.hline(x + ox, x + ox + rng.randint(2, 4), y, "lblue")

    # --- Landmasse --------------------------------------------------------
    coast = [(20, 44), (38, 26), (70, 18), (104, 24), (132, 16), (168, 20),
             (198, 34), (216, 58), (220, 88), (204, 114), (178, 130),
             (146, 138), (112, 134), (80, 138), (52, 126), (30, 106), (16, 74)]
    land = c.mask(lambda d: d.polygon(coast, fill=255))
    # Grundton: schwarze Basis, darauf ein gedithertes Vegetationsraster
    c.paint(land, 0, 0, w - 1, h - 1, "black")
    c.paint(land, 0, 0, w - 1, h - 1, None, "green", 0.44)
    # Aufgehellte Kuppen (sparsam)
    c.paint(land, 0, 0, w - 1, h - 1, None, "lgreen",
            lambda x, y: max(0.0, 0.22 * math.sin(x * 0.09) * math.cos(y * 0.11)))
    # Trockene / abgeerntete Flecken
    c.paint(land, 0, 0, w - 1, h - 1, None, "brown",
            lambda x, y: max(0.0, 0.34 * math.sin(x * 0.045 + 1.7)
                             * math.sin(y * 0.052 + 0.4)))
    # Kuestensaum + Brandung
    for y in range(h):
        for x in range(w):
            if land[x, y]:
                continue
            if any(land[min(w - 1, max(0, x + dx)), min(h - 1, max(0, y + dy))]
                   for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1),
                                  (1, 1), (-1, -1), (1, -1), (-1, 1))):
                c.set(x, y, "cyan" if (x * 3 + y) % 4 == 0 else "lblue")

    # --- Gebirge: einzeln stehende Gipfel ---------------------------------
    peaks = [(44, 52, 9), (60, 46, 12), (80, 50, 10), (100, 44, 13),
             (124, 49, 9), (146, 45, 12), (170, 51, 10), (192, 56, 8),
             (36, 92, 7), (206, 92, 7)]
    for (px_, py_, ph) in peaks:
        base = py_ + ph
        c.polygon([(px_ - ph, base), (px_, py_ - 2), (px_ + ph, base)], "black")
        c.polygon([(px_ - ph + 1, base - 1), (px_, py_ - 1), (px_ + ph - 1, base - 1)],
                  "dgrey", "grey",
                  lambda x, y, _p=px_: max(0.0, 0.9 - (x - (_p - 12)) / 22.0))
        c.polygon([(px_ - ph // 2, py_ + ph // 3), (px_, py_ - 1),
                   (px_ + ph // 2, py_ + ph // 3)], "lgrey", "white", 0.55)

    # --- Waelder: Baumgruppen statt Flaechen ------------------------------
    def tree(tx, ty):
        c.set(tx, ty + 3, "brown")
        c.polygon([(tx - 2, ty + 3), (tx, ty - 2), (tx + 2, ty + 3)], "black")
        c.polygon([(tx - 2, ty + 2), (tx, ty - 1), (tx + 2, ty + 2)], "green", "lgreen", 0.4)

    for (fx, fy, fw, fh) in ((50, 72, 30, 18), (150, 66, 28, 16), (98, 110, 36, 16)):
        for _ in range(int(fw * fh * 0.035)):
            tree(rng.randint(fx, fx + fw), rng.randint(fy, fy + fh))

    # --- Fluss Aar --------------------------------------------------------
    fy = 86.0
    for x in range(24, 214):
        fy += math.sin(x * 0.065) * 0.62
        c.vline(x, int(fy) - 1, int(fy) + 2, "black")
        c.vline(x, int(fy), int(fy) + 1, "lblue")
        if x % 4 == 0:
            c.set(x, int(fy), "cyan")

    # --- Kontaminationszone Aschenfeld ------------------------------------
    zone = c.mask(lambda d: d.ellipse([32, 96, 92, 132], fill=255))
    c.paint(zone, 32, 96, 92, 132, None, "yellow", 0.30)
    c.paint(zone, 32, 96, 92, 132, None, "lgreen", 0.35)
    inner = c.mask(lambda d: d.ellipse([50, 106, 76, 124], fill=255))
    c.paint(inner, 50, 106, 76, 124, "lgreen", "yellow", 0.5)
    c.ellipse(58, 111, 69, 119, "black")
    c.ellipse(59, 112, 68, 118, "red", "lred", 0.45)
    # Warnring gestrichelt
    for a in range(0, 360, 12):
        rx = 62 + int(31 * math.cos(math.radians(a)))
        ry = 114 + int(19 * math.sin(math.radians(a)))
        c.set(rx, ry, "yellow")

    # --- Ackerland Osthuegelland ------------------------------------------
    for sx in range(168, 208, 5):
        c.dither(sx, 96, sx + 3, 122, "brown", "yellow", 0.4)
        c.vline(sx + 4, 96, 122, "black")

    # --- Handelsrouten ----------------------------------------------------
    def route(pts):
        for i in range(len(pts) - 1):
            (ax, ay), (bx, by) = pts[i], pts[i + 1]
            steps = int(max(abs(bx - ax), abs(by - ay))) or 1
            for s in range(0, steps + 1, 4):
                rx = round(ax + (bx - ax) * s / steps)
                ry = round(ay + (by - ay) * s / steps)
                c.set(rx, ry + 1, "black")
                c.set(rx, ry, "white")

    route([(70, 62), (94, 70), (114, 80), (150, 92), (188, 102)])
    route([(114, 80), (104, 104), (74, 116)])

    # --- Siedlungsmarker ---------------------------------------------------
    def settlement(x, y, kind, big=False):
        s = 5 if big else 4
        c.rect(x - s - 1, y - s - 1, x + s + 1, y + s + 1, "black")
        if kind == "bunker":
            c.dither(x - s, y - s, x + s, y + s, "grey", "lgrey", 0.5)
            c.hline(x - s, x + s, y - s, "white")
            c.rect(x - 1, y + 1, x + 1, y + s, "black")
            c.vline(x, y - s - 4, y - s - 1, "lgrey")
            c.set(x, y - s - 5, "lred")
        elif kind == "mutant":
            c.polygon([(x - s, y + s), (x, y - s), (x + s, y + s)], "green", "lgreen", 0.55)
            c.set(x, y - s + 2, "white")
            c.rect(x - 1, y + 2, x + 1, y + s, "black")
        elif kind == "waste":
            c.polygon([(x - s, y + s), (x, y - s), (x + s, y + s)], "brown", "orange", 0.55)
            c.rect(x - 1, y + 2, x + 1, y + s, "black")
        else:  # neutral / Handelsstadt
            c.dither(x - s, y - s + 2, x + s, y + s, "orange", "yellow", 0.5)
            c.polygon([(x - s - 1, y - s + 2), (x, y - s - 3), (x + s + 1, y - s + 2)],
                      "red", "lred", 0.4)
            c.rect(x - 1, y + 2, x + 1, y + s, "black")
        c.frame(x - s - 1, y - s - 1, x + s + 1, y + s + 1,
                "white" if big else "lgrey")

    settlement(70, 62, "bunker", big=True)     # Bastion Nord
    settlement(188, 62, "bunker")              # Silo 12
    settlement(114, 80, "neutral", big=True)   # Aarbrueck
    settlement(74, 116, "mutant")              # Aschenfeld-Sippen
    settlement(190, 108, "waste")              # Kornweiler

    # --- Beschriftungen mit dunkler Unterlegung ---------------------------
    def label(x, y, s, colr):
        c.rect(x - 2, y - 2, x + text_width(s) + 1, y + 6, "black")
        c.text(x, y, s, colr)

    label(44, 70, "BASTION NORD", "lgrey")
    label(160, 70, "SILO 12", "lgrey")
    label(92, 90, "AARBRUECK", "yellow")
    label(34, 136, "ASCHENFELD", "lgreen")
    label(160, 126, "KORNWEILER", "orange")
    label(6, 6, "DER TALKESSEL", "cyan")
    c.save("02_hauptkarte.png", preview_scale=4)


# --------------------------------------------------------------------------
# 03 - Spielbildschirm (Rahmen, Viewport, Portraetspalte, Statuspanel, Leiste)
# --------------------------------------------------------------------------
def draw_icon(c: Canvas, x, y, kind) -> None:
    c.rect(x, y, x + 13, y + 13, "dgrey")
    c.hline(x, x + 13, y, "grey")
    c.vline(x, y, y + 13, "grey")
    c.hline(x, x + 13, y + 13, "black")
    c.vline(x + 13, y, y + 13, "black")
    cx, cy = x + 7, y + 7
    if kind == "sword":
        c.line(cx - 3, cy + 4, cx + 3, cy - 4, "lgrey")
        c.line(cx - 2, cy - 2, cx + 2, cy + 2, "orange")
    elif kind == "eye":
        c.ellipse(cx - 4, cy - 2, cx + 4, cy + 2, "white")
        c.ellipse(cx - 1, cy - 1, cx + 1, cy + 1, "lblue")
        c.set(cx, cy, "black")
    elif kind == "hand":
        c.rect(cx - 3, cy - 1, cx + 2, cy + 4, "orange")
        for i in range(4):
            c.vline(cx - 3 + i, cy - 4, cy - 1, "orange")
    elif kind == "flask":
        c.rect(cx - 2, cy - 5, cx + 1, cy - 3, "lgrey")
        c.polygon([(cx - 3, cy + 4), (cx - 2, cy - 3), (cx + 2, cy - 3), (cx + 3, cy + 4)],
                  "lgreen", "green", 0.5)
    elif kind == "book":
        c.rect(cx - 4, cy - 4, cx + 3, cy + 4, "red")
        c.vline(cx, cy - 4, cy + 4, "yellow")
        c.frame(cx - 4, cy - 4, cx + 3, cy + 4, "brown")
    elif kind == "bag":
        c.polygon([(cx - 4, cy + 4), (cx - 3, cy - 2), (cx + 3, cy - 2), (cx + 4, cy + 4)],
                  "brown", "orange", 0.4)
        c.hline(cx - 2, cx + 2, cy - 3, "yellow")
    elif kind == "coin":
        c.ellipse(cx - 4, cy - 4, cx + 3, cy + 3, "orange", "yellow", 0.5)
        c.set(cx - 1, cy, "brown")
        c.set(cx, cy, "brown")
    elif kind == "map":
        c.rect(cx - 4, cy - 4, cx + 4, cy + 3, "yellow")
        c.dither(cx - 4, cy - 4, cx + 4, cy + 3, "yellow", "orange", 0.35)
        c.line(cx - 3, cy + 2, cx + 2, cy - 3, "red")


def gen_screen() -> None:
    w, h = 320, 200
    c = Canvas(w, h, "black")
    rng = random.Random(7)
    ornate_frame(c, 0, 0, w - 1, h - 1)

    # --- Viewport ---------------------------------------------------------
    vx0, vy0, vx1, vy1 = 9, 16, 214, 170
    c.rect(vx0, vy0, vx1, vy1, "black")
    c.frame(vx0 - 1, vy0 - 1, vx1 + 1, vy1 + 1, "dgrey")

    # Bunkerhalle: Boden
    c.dither(vx0, vy0, vx1, vy1, "black", "dgrey", 0.25)
    floor = (vx0 + 12, vy0 + 14, vx1 - 14, vy1 - 12)
    c.dither(*floor, "dgrey", "grey", 0.45)
    # Fliesenraster
    for x in range(floor[0], floor[2], 12):
        c.vline(x, floor[1], floor[3], "black")
    for y in range(floor[1], floor[3], 12):
        c.hline(floor[0], floor[2], y, "black")
    # Teppich
    c.dither(floor[0] + 36, floor[1] + 30, floor[0] + 96, floor[3] - 24, "red", "lred", 0.35)
    c.frame(floor[0] + 36, floor[1] + 30, floor[0] + 96, floor[3] - 24, "yellow")

    # Waende
    c.dither(vx0, vy0, vx1, floor[1] - 1, "blue", "black", 0.5)
    for x in range(vx0 + 4, vx1, 16):
        c.dither(x, vy0 + 2, x + 9, floor[1] - 2, "grey", "lgrey", 0.4)
        c.vline(x + 10, vy0 + 2, floor[1] - 2, "black")
    # Leuchten an der Wand
    for x in range(vx0 + 20, vx1 - 10, 48):
        c.rect(x, vy0 + 6, x + 4, vy0 + 9, "yellow")
        c.dither(x - 5, vy0 + 10, x + 9, vy0 + 22, None, "yellow", 0.18)

    # Seitenwaende
    c.dither(vx0, floor[1], floor[0] - 1, vy1, "dgrey", "black", 0.5)
    c.dither(floor[2] + 1, floor[1], vx1, vy1, "dgrey", "black", 0.5)
    c.dither(floor[0], floor[3] + 1, floor[2], vy1, "dgrey", "black", 0.5)

    # Figuren in der Halle (kleine Sprites)
    def mini(x, y, cloth, accent, skin="orange", hat=None):
        c.rect(x + 2, y + 9, x + 3, y + 12, "black")
        c.rect(x + 5, y + 9, x + 6, y + 12, "black")
        c.dither(x + 1, y + 4, x + 7, y + 9, cloth, accent, 0.4)
        c.rect(x + 2, y, x + 6, y + 4, skin)
        c.set(x + 3, y + 2, "black")
        c.set(x + 5, y + 2, "black")
        if hat == "helmet":
            c.rect(x + 1, y - 1, x + 7, y + 1, "lgrey")
        elif hat == "hood":
            c.rect(x + 1, y - 1, x + 7, y + 1, "brown")
        elif hat == "eye":
            c.set(x + 4, y + 1, "lgreen")

    # Kisten / Faesser als Inventarandeutung
    for (bx, by) in ((floor[0] + 6, floor[3] - 30), (floor[0] + 6, floor[3] - 20),
                     (floor[2] - 18, floor[1] + 8)):
        c.dither(bx, by, bx + 9, by + 8, "brown", "orange", 0.4)
        c.frame(bx, by, bx + 9, by + 8, "black")
        c.hline(bx + 1, bx + 8, by + 4, "yellow")

    # Tragpfeiler mit Schattenwurf
    for px_ in (floor[0] + 16, floor[0] + 104):
        c.dither(px_, floor[1], px_ + 7, floor[3] - 8, "grey", "lgrey",
                 lambda x, y, _p=px_: max(0.0, 0.9 - (x - _p) / 7.0 * 1.4))
        c.frame(px_, floor[1], px_ + 7, floor[3] - 8, "black")
        c.hline(px_ - 1, px_ + 8, floor[1], "lgrey")
        c.hline(px_ - 1, px_ + 8, floor[3] - 8, "lgrey")
        c.dither(px_ + 8, floor[1] + 2, px_ + 13, floor[3] - 8, None, "black", 0.55)

    # Werkbank mit Werkzeugen
    tbx, tby = floor[2] - 42, floor[3] - 26
    c.dither(tbx, tby, tbx + 30, tby + 6, "brown", "orange", 0.45)
    c.frame(tbx, tby, tbx + 30, tby + 6, "black")
    c.vline(tbx + 2, tby + 6, tby + 14, "brown")
    c.vline(tbx + 28, tby + 6, tby + 14, "brown")
    for (ix, colr) in ((4, "lgrey"), (11, "lgreen"), (18, "yellow"), (24, "lblue")):
        c.rect(tbx + ix, tby - 3, tbx + ix + 3, tby - 1, colr)
        c.set(tbx + ix, tby - 3, "white")

    # Hydrokultur-Becken an der Rueckwand
    for hx in (floor[0] + 30, floor[0] + 60):
        c.dither(hx, floor[1] + 2, hx + 22, floor[1] + 9, "dgrey", "grey", 0.5)
        c.frame(hx, floor[1] + 2, hx + 22, floor[1] + 9, "black")
        c.dither(hx + 1, floor[1] + 3, hx + 21, floor[1] + 6, "green", "lgreen", 0.55)

    # Figuren zuletzt, damit sie vor Pfeilern und Moebeln stehen
    mini(floor[0] + 60, floor[1] + 42, "blue", "lblue", hat="helmet")
    mini(floor[0] + 26, floor[1] + 22, "brown", "orange", hat="hood")
    mini(floor[0] + 116, floor[1] + 18, "green", "lgreen", hat="eye")
    mini(floor[0] + 134, floor[3] - 40, "purple", "lred")
    mini(floor[0] + 86, floor[3] - 30, "brown", "yellow", hat="hood")

    plate(c, (vx0 + vx1) // 2, 10, "BASTION NORD  HAUPTHALLE")

    # --- Portraetspalte ---------------------------------------------------
    px0, py0 = 220, 16
    cellw, cellh = 45, 32
    faces = [
        dict(cloth=("blue", "lblue"), hair="dgrey", headgear="helmet",
             accent="lgrey", pallor=0.2, squint=True, seed=1),
        dict(cloth=("blue", "lblue"), hair="brown", accent="lgrey",
             pallor=0.25, gaunt=1.1, scarred=True, seed=2),
        dict(cloth=("green", "lgreen"), hair="dgrey", third_eye=True,
             accent="yellow", gaunt=1.4, mottled=True, seed=3),
        dict(cloth=("green", "lgreen"), hair="brown", third_eye=True,
             accent="yellow", gaunt=1.3, aged=True, seed=4),
        dict(cloth=("brown", "orange"), hair="brown", headgear="hood",
             accent="red", gaunt=1.2, stubble=True, seed=5),
        dict(cloth=("brown", "orange"), hair="black", accent="red",
             gaunt=1.2, stubble=True, scarred=True, squint=True, seed=6),
    ]
    for i, cfg in enumerate(faces):
        fx = px0 + (i % 2) * (cellw + 3)
        fy = py0 + (i // 2) * (cellh + 3)
        draw_face(c, fx + 1, fy + 1, cellw - 2, cellh - 2, **cfg)
        c.frame(fx, fy, fx + cellw - 1, fy + cellh - 1, "lgrey")
        c.frame(fx + 1, fy + 1, fx + cellw - 2, fy + cellh - 2, "dgrey")
        # kleine Lebensleiste
        c.rect(fx + 3, fy + cellh - 4, fx + cellw - 4, fy + cellh - 3, "dgrey")
        c.rect(fx + 3, fy + cellh - 4, fx + 3 + int((cellw - 8) * (0.9 - i * 0.12)),
               fy + cellh - 3, "lgreen" if i < 4 else "yellow")

    # --- Statuspanel ------------------------------------------------------
    sx0, sy0, sx1, sy1 = 220, py0 + 3 * (cellh + 3) + 1, 312, 170
    c.rect(sx0, sy0, sx1, sy1, "orange")          # ruhige Volltonflaeche
    c.hline(sx0, sx1, sy1 - 1, "brown")           # Kantenschattierung
    c.vline(sx1 - 1, sy0, sy1, "brown")
    c.hline(sx0 + 1, sx1 - 1, sy0 + 1, "yellow")
    c.frame(sx0, sy0, sx1, sy1, "black")
    c.frame(sx0 - 1, sy0 - 1, sx1 + 1, sy1 + 1, "lgrey")
    lines = [("ZEIT", "08:45"), ("TAG", "152"), ("X-POS", "62"), ("Y-POS", "32")]
    ty = sy0 + 4
    for k, v in lines:
        c.text(sx0 + 4, ty, k, "black")
        c.text(sx1 - 5 - text_width(v), ty, v, "black")
        ty += 6
    c.hline(sx0 + 3, sx1 - 4, ty, "brown")
    ty += 3
    c.text(sx0 + 4, ty, "BEWOELKT", "red")
    c.text(sx0 + 4, ty + 6, "VORRAT 12 TAGE", "black")

    # --- Icon-Leiste ------------------------------------------------------
    icons = ["sword", "eye", "hand", "flask", "book", "bag", "coin", "map"]
    ix = 10
    for kind in icons:
        draw_icon(c, ix, 176, kind)
        ix += 16
    # Meldungszeile rechts der Leiste
    c.rect(ix + 4, 176, 312, 189, "black")
    c.frame(ix + 4, 176, 312, 189, "dgrey")
    c.text(ix + 8, 180, "EIN BOTE TRIFFT EIN", "lgreen")
    c.save("03_spielbildschirm.png", preview_scale=3)


# --------------------------------------------------------------------------
# 04 - Dialogszene
# --------------------------------------------------------------------------
def gen_dialog() -> None:
    w, h = 320, 200
    c = Canvas(w, h, "black")
    ornate_frame(c, 0, 0, w - 1, h - 1, base="dgrey", lite="lgrey", gem="lblue")

    plate(c, w // 2, 10, "SIPPENAELTESTE DER BITTERGRABEN", bg="brown", fg="yellow")

    # Grosses Portraet links: dasselbe 40x48-Bildnis wie die Portraetdateien,
    # ganzzahlig auf 2x vergroessert (Nahaufnahme, identische Kunst)
    bust = Canvas(40, 48, "black")
    draw_face(bust, 1, 1, 38, 46,
              skin=("orange", "brown"), hair="dgrey",
              cloth=("green", "lgreen"), accent="yellow", third_eye=True,
              bg=("blue", "black"), aged=True, gaunt=1.5, mottled=True,
              scarred=True, seed=11)
    big = bust.img.resize((80, 96), Image.NEAREST)
    c.img.paste(big, (14, 30))
    c.frame(12, 28, 95, 127, "lgrey")
    c.frame(13, 29, 94, 126, "black")
    # Namensschild unter dem Portraet
    c.rect(12, 131, 95, 143, "brown")
    c.frame(12, 131, 95, 143, "lgrey")
    c.text_centered(53, 135, "MUTTER KATRAN", "yellow", shadow="black")

    # Textfeld rechts
    tx0, ty0, tx1, ty1 = 100, 28, 308, 154
    c.dither(tx0, ty0, tx1, ty1, "black", "blue", 0.18)
    c.frame(tx0, ty0, tx1, ty1, "lgrey")
    c.frame(tx0 + 1, ty0 + 1, tx1 - 1, ty1 - 1, "dgrey")
    text_lines = [
        "IHR KOMMT VOM NORDBERG, DAS",
        "RIECHT MAN. EURE WERKZEUGE",
        "HALTEN LAENGER ALS UNSERE",
        "HAENDE - UND UNSERE HAENDE",
        "SIND BESSER ALS EURE.",
        "",
        "WIR GEBEN EUCH KRAUT GEGEN",
        "STAHL. ZWEI KISTEN FUER",
        "ZEHN KLINGEN, NICHT WENIGER.",
        "",
        "DER GRABEN VERGISST NICHT,",
        "WER FAIR TEILT.",
    ]
    ty = ty0 + 8
    for ln in text_lines:
        c.text(tx0 + 7, ty, ln, "lgreen" if ln.startswith("DER") else "lgrey")
        ty += 9

    # Antwortoptionen
    opts = [("1", "WIR NEHMEN DEN HANDEL AN", "yellow"),
            ("2", "ZEHN KLINGEN SIND ZU VIEL", "orange"),
            ("3", "WIR KOMMEN SPAETER WIEDER", "lgrey")]
    oy = 158
    for num, label, colr in opts:
        c.rect(12, oy, 308, oy + 11, "black")
        c.frame(12, oy, 308, oy + 11, "dgrey")
        c.rect(14, oy + 2, 22, oy + 9, "brown")
        c.text(17, oy + 3, num, "yellow")
        c.text(28, oy + 3, label, colr)
        oy += 13
    c.save("04_dialogszene.png", preview_scale=3)


# --------------------------------------------------------------------------
# 05 - Szenenbild: Bastion Nord bei Nacht
# --------------------------------------------------------------------------
def gen_scene_bastion() -> None:
    w, h = 320, 200
    c = Canvas(w, h, "black")
    rng = random.Random(42)

    # Himmel: schwarz -> blau -> purpur zum Horizont
    c.dither(0, 0, w - 1, 118, "black", "blue",
             lambda x, y: min(1.0, (y / 118.0) ** 1.4 * 1.5))
    c.dither(0, 72, w - 1, 118, None, "purple",
             lambda x, y: max(0.0, (y - 72) / 46.0 * 0.55))
    starfield(c, 0, 0, w - 1, 80, rng, density=0.016)

    # Mond
    c.ellipse(246, 20, 276, 50, "lgrey", "white", 0.65)
    c.ellipse(252, 28, 260, 36, "lgrey", "grey", 0.5)
    c.ellipse(262, 38, 268, 44, "lgrey", "grey", 0.45)
    c.dither(236, 10, 286, 60, None, "lblue", 0.05)

    # Hintere Bergkette
    ridge_back = [(0, 118)]
    for x in range(0, w + 1, 16):
        ridge_back.append((x, 92 + int(14 * math.sin(x * 0.05)) - (8 if x % 48 == 0 else 0)))
    ridge_back.append((w, 118))
    c.polygon(ridge_back, "dgrey", "blue", 0.45)

    # Vordere Bergkette
    ridge_front = [(0, 132)]
    for x in range(0, w + 1, 20):
        ridge_front.append((x, 108 + int(12 * math.cos(x * 0.043))))
    ridge_front.append((w, 132))
    c.polygon(ridge_front, "black", "dgrey", 0.30)

    # Bunkerblock
    bx0, by0, bx1, by1 = 84, 104, 236, 172
    c.dither(bx0, by0, bx1, by1, "dgrey", "grey",
             lambda x, y: 0.62 - 0.4 * (y - by0) / (by1 - by0))
    c.hline(bx0, bx1, by0, "lgrey")
    c.frame(bx0, by0, bx1, by1, "black")
    # Betonfugen
    for y in range(by0 + 8, by1, 10):
        c.hline(bx0 + 1, bx1 - 1, y, "black")
    for x in range(bx0 + 14, bx1, 22):
        c.vline(x, by0 + 1, by1 - 1, "black")
    # Verwitterungsstreifen
    for _ in range(90):
        sx = rng.randint(bx0 + 2, bx1 - 2)
        sy = rng.randint(by0 + 2, by1 - 6)
        c.vline(sx, sy, sy + rng.randint(1, 5), "black" if rng.random() < 0.6 else "brown")

    # Antennenmast mit Warnlicht
    c.vline(196, 58, by0, "grey")
    c.vline(197, 58, by0, "dgrey")
    for yy in range(64, by0, 10):
        c.line(192, yy + 6, 197, yy, "dgrey")
        c.line(202, yy + 6, 197, yy, "dgrey")
    c.rect(195, 56, 198, 58, "lred")
    c.dither(190, 50, 204, 64, None, "red", 0.12)

    # Torbogen
    gx0, gx1 = 140, 180
    gy0, gy1 = 122, by1
    c.polygon([(gx0, gy1), (gx0, gy0 + 10), (gx0 + 6, gy0), (gx1 - 6, gy0),
               (gx1, gy0 + 10), (gx1, gy1)], "black")
    c.frame(gx0 - 2, gy0 - 2, gx1 + 2, gy1, "lgrey")
    # Warmes Licht aus dem Tor: nach unten hin in die Volltonflaeche uebergehend
    c.dither(gx0 + 2, gy1 - 38, gx1 - 2, gy1 - 1, "black", "orange",
             lambda x, y: max(0.0, ((y - (gy1 - 38)) / 38.0) ** 1.4 * 1.35))
    c.dither(gx0 + 2, gy1 - 18, gx1 - 2, gy1 - 1, "orange", "yellow",
             lambda x, y: max(0.0, (y - (gy1 - 18)) / 18.0 * 1.3))
    c.rect(gx0 + 4, gy1 - 6, gx1 - 4, gy1 - 1, "yellow")
    c.hline(gx0 + 2, gx1 - 2, gy1 - 1, "white")

    # Lichtkegel auf den Vorplatz
    c.polygon([(gx0 + 2, gy1), (gx1 - 2, gy1), (gx1 + 26, h - 1), (gx0 - 26, h - 1)],
              None, "orange", 0.16)

    # Wachen: Silhouetten direkt vor dem erleuchteten Tor
    def guard(x, base, flip=1):
        c.rect(x - 3, base - 19, x + 3, base - 7, "black")     # Torso
        c.rect(x - 4, base - 18, x - 3, base - 10, "black")    # Arme
        c.rect(x + 3, base - 18, x + 4, base - 10, "black")
        c.rect(x - 3, base - 7, x - 1, base, "black")          # Beine
        c.rect(x + 1, base - 7, x + 3, base, "black")
        c.ellipse(x - 3, base - 25, x + 3, base - 19, "black")  # Kopf
        c.hline(x - 4, x + 4, base - 20, "black")              # Schultern
        # Randlicht auf der dem Tor zugewandten Seite
        c.vline(x + flip * 4, base - 24, base - 4, "orange")
        c.set(x + flip * 3, base - 25, "orange")
        c.set(x + flip * 4, base - 26, "yellow")
        # Gewehr / Stange
        c.line(x + flip * 6, base - 26, x + flip * 5, base + 1, "dgrey")
        c.set(x + flip * 6, base - 26, "lgrey")

    guard(151, 172, flip=-1)
    guard(169, 172, flip=1)

    # Vorplatz / Schutt
    c.dither(0, 172, w - 1, h - 1, "black", "dgrey", 0.35)
    for _ in range(260):
        rx = rng.randint(0, w - 1)
        ry = rng.randint(173, h - 2)
        c.set(rx, ry, rng.choice(["dgrey", "grey", "brown", "black"]))
    for (rx, ry, rw) in ((20, 186, 14), (54, 178, 9), (268, 182, 16), (240, 192, 11)):
        c.dither(rx, ry, rx + rw, ry + 4, "dgrey", "grey", 0.5)
        c.hline(rx, rx + rw, ry, "lgrey")

    # Schiefes Strassenschild
    c.line(36, 196, 40, 166, "dgrey")
    c.dither(24, 156, 58, 168, "grey", "lgrey", 0.4)
    c.frame(24, 156, 58, 168, "black")
    c.text(27, 160, "SPERRZONE", "red")

    # Titel
    plate(c, w // 2, 6, "BASTION NORD", bg="black", fg="yellow", border="lgrey")
    c.save("05_szene_bastion.png", preview_scale=3)


# --------------------------------------------------------------------------
# 06 - Portraets (einzeln, 40x48)
# --------------------------------------------------------------------------
def gen_portraits() -> None:
    specs = [
        # Bunker: blass vom Leben unter Tage, diszipliniert, hartaeugig
        ("06a_portrait_bunker.png", dict(
            skin=("orange", "brown"), hair="dgrey", cloth=("blue", "lblue"),
            accent="lgrey", headgear="helmet", bg=("blue", "black"),
            pallor=0.22, gaunt=0.9, scarred=True, seed=3)),
        # Mutierte: ausgezehrt, fleckige Haut, drittes Auge
        ("06b_portrait_mutant.png", dict(
            skin=("orange", "brown"), hair="dgrey", cloth=("green", "lgreen"),
            accent="yellow", third_eye=True, bg=("blue", "black"),
            gaunt=1.5, mottled=True, seed=6)),
        # Wastelander: sonnenverbrannt, stoppelig, vernarbt
        ("06c_portrait_wastelander.png", dict(
            skin=("orange", "brown"), hair="brown", cloth=("brown", "orange"),
            accent="red", headgear="hood", bg=("blue", "black"),
            gaunt=1.2, stubble=True, scarred=True, seed=9)),
    ]
    for name, cfg in specs:
        c = Canvas(40, 48, "black")
        draw_face(c, 1, 1, 38, 46, **cfg)
        c.frame(0, 0, 39, 47, "lgrey")
        c.frame(1, 1, 38, 46, "dgrey")
        c.save(name, preview_scale=6)


# --------------------------------------------------------------------------
# 07 - Spielfiguren (16x24, transparent)
# --------------------------------------------------------------------------
def draw_sprite(c: Canvas, cloth, accent, skin="orange", skin_sh="brown",
                headgear=None, third_eye=False, boots="black") -> None:
    # Beine
    c.dither(5, 18, 7, 23, boots, "dgrey", 0.3)
    c.dither(9, 18, 11, 23, boots, "dgrey", 0.3)
    c.hline(4, 7, 23, "black")
    c.hline(9, 12, 23, "black")
    # Torso
    c.dither(4, 9, 12, 18, cloth, accent,
             lambda x, y: 0.62 - 0.5 * abs(x - 8) / 5.0)
    c.frame(4, 9, 12, 18, "black")
    # Guertel
    c.hline(4, 12, 16, "black")
    c.set(8, 16, "yellow")
    # Arme
    c.dither(2, 10, 3, 17, cloth, "black", 0.35)
    c.dither(13, 10, 14, 17, cloth, "black", 0.35)
    c.rect(2, 17, 3, 18, skin_sh)
    c.rect(13, 17, 14, 18, skin_sh)
    # Kopf
    c.ellipse(5, 2, 11, 9, skin, skin_sh, lambda x, y: (x - 5) / 6.0 * 0.8)
    c.set(6, 5, "black")
    c.set(10, 5, "black")
    c.set(7, 5, "white")
    c.hline(7, 9, 7, skin_sh)
    if third_eye:
        c.set(8, 3, "lgreen")
        c.set(8, 4, "white")
    if headgear == "helmet":
        c.dither(4, 1, 12, 3, "grey", "lgrey", 0.5)
        c.hline(4, 12, 4, "dgrey")
        c.hline(5, 11, 1, "white")
    elif headgear == "hood":
        c.polygon([(3, 10), (4, 3), (8, 0), (12, 3), (13, 10)], "brown", "dgrey", 0.4)
        c.ellipse(5, 3, 11, 9, skin, skin_sh, 0.4)
        c.set(6, 5, "black")
        c.set(10, 5, "black")
    elif headgear == "hair":
        c.dither(5, 1, 11, 3, "brown", "black", 0.4)


def gen_sprites() -> None:
    c = Canvas(16, 24)
    draw_sprite(c, "blue", "lblue", headgear="helmet")
    c.save("07a_sprite_bunker.png", preview_scale=8)

    c = Canvas(16, 24)
    draw_sprite(c, "green", "lgreen", skin="orange", skin_sh="green",
                third_eye=True, headgear="hair")
    c.save("07b_sprite_mutant.png", preview_scale=8)

    c = Canvas(16, 24)
    draw_sprite(c, "brown", "orange", headgear="hood")
    c.save("07c_sprite_wastelander.png", preview_scale=8)


# --------------------------------------------------------------------------
# 08 - Gebaeude (32x32, transparent)
# --------------------------------------------------------------------------
def gen_buildings() -> None:
    # Bunkeranlage
    c = Canvas(32, 32)
    c.dither(2, 14, 29, 29, "grey", "dgrey",
             lambda x, y: 0.3 + 0.5 * (y - 14) / 15.0)
    c.frame(2, 14, 29, 29, "black")
    c.hline(3, 28, 14, "lgrey")
    for y in range(18, 29, 5):
        c.hline(3, 28, y, "black")
    c.dither(11, 4, 20, 14, "grey", "lgrey", 0.45)   # Aufbau
    c.frame(11, 4, 20, 14, "black")
    c.vline(15, 0, 4, "lgrey")
    c.set(15, 0, "lred")
    c.rect(13, 21, 18, 29, "black")                  # Tor
    c.dither(14, 25, 17, 29, "black", "orange", 0.6)
    c.frame(13, 21, 18, 29, "lgrey")
    for x in (5, 24):                                 # Lueftungsschaechte
        c.dither(x, 17, x + 3, 20, "dgrey", "lblue", 0.35)
    c.save("08a_gebaeude_bunker.png", preview_scale=6)

    # Sippenhaus der Mutierten
    c = Canvas(32, 32)
    c.polygon([(1, 16), (16, 3), (31, 16)], "green", "black", 0.45)
    c.polygon([(4, 15), (16, 5), (28, 15)], "lgreen", "green", 0.5)
    c.dither(4, 16, 28, 29, "green", "brown", 0.4)
    c.frame(4, 16, 28, 29, "black")
    c.rect(13, 22, 18, 29, "black")
    c.frame(13, 22, 18, 29, "brown")
    c.dither(6, 19, 10, 22, "yellow", "orange", 0.5)   # Fenster
    c.dither(22, 19, 26, 22, "yellow", "orange", 0.5)
    c.vline(16, 0, 4, "brown")                          # Sippenzeichen
    c.hline(13, 19, 1, "lgreen")
    c.set(16, 2, "white")
    c.save("08b_gebaeude_mutant.png", preview_scale=6)

    # Wastelander-Hütte
    c = Canvas(32, 32)
    c.polygon([(0, 17), (15, 5), (31, 18)], "brown", "dgrey", 0.4)
    c.hline(0, 31, 17, "black")
    c.dither(3, 17, 28, 29, "brown", "orange", 0.45)
    c.frame(3, 17, 28, 29, "black")
    for x in range(5, 28, 6):                           # Flickenbretter
        c.vline(x, 18, 28, "black")
    c.rect(12, 22, 17, 29, "black")
    c.hline(11, 18, 21, "orange")
    c.dither(20, 20, 25, 24, "red", "lred", 0.4)        # Flicken
    c.line(24, 5, 29, 17, "dgrey")                       # Antenne/Stange
    c.set(24, 4, "red")
    c.save("08c_gebaeude_wastelander.png", preview_scale=6)

    # Neutraler Marktstand Aarbrueck
    c = Canvas(32, 32)
    awn = c.mask(lambda d: d.polygon([(0, 14), (16, 3), (31, 14)], fill=255))
    c.paint(awn, 0, 3, 31, 14, "red")
    c.paint(awn, 0, 3, 31, 14, None, "white",
            lambda x, y: 1.0 if ((x + (14 - y)) // 4) % 2 == 0 else 0.0)
    c.paint(awn, 0, 3, 31, 14, None, "black",
            lambda x, y: max(0.0, (y - 3) / 11.0 * 0.5 - 0.15))
    c.line(0, 14, 16, 3, "yellow")
    c.line(31, 14, 16, 3, "brown")
    c.hline(0, 31, 14, "black")
    c.dither(4, 15, 27, 24, "brown", "orange", 0.4)     # Theke
    c.frame(4, 15, 27, 24, "black")
    c.vline(5, 15, 29, "brown")
    c.vline(26, 15, 29, "brown")
    for (ix, colr) in ((8, "lgreen"), (13, "yellow"), (18, "lblue"), (22, "lred")):
        c.dither(ix, 17, ix + 3, 20, colr, "black", 0.35)   # Waren
    c.dither(6, 25, 25, 29, "dgrey", "black", 0.4)
    c.save("08d_gebaeude_neutral.png", preview_scale=6)


# --------------------------------------------------------------------------
# 09 - Gegner
# --------------------------------------------------------------------------
def gen_enemies() -> None:
    c = Canvas(16, 24)
    draw_sprite(c, "dgrey", "grey", skin="brown", skin_sh="black", boots="black")
    c.dither(4, 1, 12, 4, "red", "black", 0.45)      # Lumpen-Kopftuch
    c.set(6, 5, "lred")
    c.set(10, 5, "black")
    c.line(14, 3, 14, 16, "lgrey")                    # Improvisierte Waffe
    c.line(13, 4, 15, 4, "brown")
    c.save("09a_gegner_raider.png", preview_scale=8)

    # Verwilderter Hund
    c = Canvas(24, 16)
    c.dither(4, 6, 17, 12, "brown", "black",
             lambda x, y: 0.35 + 0.4 * (y - 6) / 6.0)
    c.frame(4, 6, 17, 12, "black")
    c.polygon([(17, 5), (23, 3), (22, 9), (17, 11)], "brown", "dgrey", 0.4)  # Kopf
    c.set(21, 6, "yellow")
    c.set(20, 5, "black")
    c.polygon([(18, 5), (19, 1), (21, 4)], "brown")   # Ohr
    c.polygon([(4, 8), (0, 3), (3, 10)], "brown", "black", 0.4)  # Rute
    for lx in (6, 9, 12, 15):
        c.vline(lx, 12, 15, "black")
        c.set(lx, 15, "dgrey")
    c.dither(6, 7, 15, 9, None, "orange", 0.25)       # Fellzeichnung
    c.save("09b_gegner_wildhund.png", preview_scale=8)


# --------------------------------------------------------------------------
# 10 - Items / Waren (16x16, transparent)
# --------------------------------------------------------------------------
def item_canvas() -> Canvas:
    return Canvas(16, 16)


def gen_items() -> None:
    # Werkzeug (Hammer/Schluessel-Kombination)
    c = item_canvas()
    c.line(4, 12, 11, 4, "brown")
    c.line(5, 12, 12, 4, "dgrey")
    c.dither(9, 2, 14, 6, "lgrey", "grey", 0.5)
    c.frame(9, 2, 14, 6, "black")
    c.set(3, 13, "black")
    c.save("10a_item_werkzeug.png", preview_scale=8)

    # Waffe (Klinge)
    c = item_canvas()
    c.polygon([(11, 2), (13, 4), (6, 12), (4, 10)], "lgrey", "white", 0.4)
    c.line(4, 10, 6, 12, "grey")
    c.line(3, 11, 6, 14, "brown")
    c.rect(2, 12, 4, 14, "brown")
    c.set(12, 3, "white")
    c.save("10b_item_waffe.png", preview_scale=8)

    # Medizin (Flakon)
    c = item_canvas()
    c.rect(6, 2, 9, 4, "lgrey")
    c.polygon([(4, 13), (5, 4), (10, 4), (11, 13)], "cyan", "lblue", 0.4)
    c.dither(5, 8, 10, 12, "lgreen", "green", 0.45)
    c.frame(4, 4, 11, 13, "black")
    c.set(6, 6, "white")
    c.save("10c_item_medizin.png", preview_scale=8)

    # Nahrung (Brot)
    c = item_canvas()
    c.ellipse(2, 5, 13, 12, "brown", "orange",
              lambda x, y: 0.7 - 0.5 * (y - 5) / 7.0)
    c.frame(2, 5, 13, 12, "black")
    for sx in (5, 8, 11):
        c.line(sx, 6, sx - 1, 8, "yellow")
    c.save("10d_item_nahrung.png", preview_scale=8)

    # Erz / Altmetall
    c = item_canvas()
    c.polygon([(3, 12), (2, 7), (7, 3), (12, 5), (13, 12)], "grey", "dgrey", 0.45)
    c.polygon([(5, 9), (7, 5), (10, 8), (8, 11)], "lgrey", "white", 0.4)
    c.line(3, 12, 13, 12, "black")
    c.save("10e_item_erz.png", preview_scale=8)

    # Talon (Muenze)
    c = item_canvas()
    c.ellipse(3, 3, 12, 12, "orange", "yellow", 0.5)
    c.ellipse(5, 5, 10, 10, "brown", "orange", 0.4)
    c.set(7, 7, "yellow")
    c.set(8, 8, "yellow")
    c.ellipse_frame(3, 3, 12, 12, "black")
    c.save("10f_item_talon.png", preview_scale=8)

    # Treibstoff (Kanister)
    c = item_canvas()
    c.dither(3, 4, 12, 13, "red", "lred", 0.35)
    c.frame(3, 4, 12, 13, "black")
    c.rect(6, 2, 9, 4, "dgrey")
    c.hline(4, 11, 7, "black")
    c.text(5, 9, "T", "yellow")
    c.save("10g_item_treibstoff.png", preview_scale=8)

    # Chemikalien (Fass)
    c = item_canvas()
    c.dither(3, 3, 12, 13, "green", "lgreen", 0.35)
    c.frame(3, 3, 12, 13, "black")
    c.hline(3, 12, 6, "black")
    c.hline(3, 12, 10, "black")
    c.dither(5, 7, 10, 9, "yellow", "black", 0.4)
    c.save("10h_item_chemie.png", preview_scale=8)


# --------------------------------------------------------------------------
# 11 - Stilblatt: alles auf einer Tafel
# --------------------------------------------------------------------------
def gen_styleboard() -> None:
    w, h = 320, 200
    c = Canvas(w, h, "black")
    ornate_frame(c, 0, 0, w - 1, h - 1, base="blue", lite="lblue", gem="cyan")
    plate(c, w // 2, 10, "STILBLATT  ENDZEIT", bg="blue", fg="cyan")

    def load_native(name):
        img = Image.open(NATIVE / name).convert("RGBA")
        return img

    def paste(name, x, y):
        img = load_native(name)
        c.img.paste(img, (x, y), img)

    # Portraets
    c.text(12, 24, "PORTRAETS", "yellow")
    for i, n in enumerate(("06a_portrait_bunker.png", "06b_portrait_mutant.png",
                           "06c_portrait_wastelander.png")):
        paste(n, 12 + i * 44, 32)

    # Figuren
    c.text(150, 24, "FIGUREN", "yellow")
    for i, n in enumerate(("07a_sprite_bunker.png", "07b_sprite_mutant.png",
                           "07c_sprite_wastelander.png", "09a_gegner_raider.png")):
        paste(n, 150 + i * 20, 34)
    paste("09b_gegner_wildhund.png", 232, 42)

    # Gebaeude
    c.text(12, 86, "GEBAEUDE", "yellow")
    for i, n in enumerate(("08a_gebaeude_bunker.png", "08b_gebaeude_mutant.png",
                           "08c_gebaeude_wastelander.png", "08d_gebaeude_neutral.png")):
        paste(n, 12 + i * 36, 94)

    # Items
    c.text(160, 86, "ITEMS", "yellow")
    items = ("10a_item_werkzeug.png", "10b_item_waffe.png", "10c_item_medizin.png",
             "10d_item_nahrung.png", "10e_item_erz.png", "10f_item_talon.png",
             "10g_item_treibstoff.png", "10h_item_chemie.png")
    for i, n in enumerate(items):
        paste(n, 160 + (i % 4) * 20, 94 + (i // 4) * 20)

    # Palette unten
    c.text(12, 136, "PALETTE  16 FARBEN  NUR DITHERING", "yellow")
    for i, name in enumerate(PALETTE_ORDER):
        x = 12 + i * 18
        c.rect(x, 146, x + 15, 161, name)
        c.frame(x, 146, x + 15, 161, "dgrey")
    ramps = [("black", "blue"), ("blue", "lblue"), ("brown", "orange"),
             ("orange", "yellow"), ("green", "lgreen"), ("dgrey", "lgrey"),
             ("red", "lred"), ("lblue", "cyan")]
    for i, (a, b) in enumerate(ramps):
        x = 12 + i * 37
        c.dither(x, 166, x + 34, 178, a, b,
                 lambda px, py, _x=x: (px - _x) / 34.0)
        c.frame(x, 166, x + 34, 178, "black")
    c.text(12, 184, "AUFLOESUNG 320X200   SPRITE 16X24   ITEM 16X16   PORTRAET 40X48",
           "lgrey")
    c.save("11_stilblatt.png", preview_scale=3)


# --------------------------------------------------------------------------
def main() -> None:
    print("Erzeuge C64-Stil-Assets ...")
    gen_palette()
    gen_map()
    gen_screen()
    gen_dialog()
    gen_scene_bastion()
    gen_portraits()
    gen_sprites()
    gen_buildings()
    gen_enemies()
    gen_items()
    gen_styleboard()
    print(f"Fertig. Native: {NATIVE}   Vorschau: {OUT}")


if __name__ == "__main__":
    main()
