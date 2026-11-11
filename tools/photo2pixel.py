"""
Foto -> Pixelgrafik im C64-Retro-Stil (Referenz: SKALD - Against the Black Priory).

Wandelt beliebige Fotos in Assets um, die den Stilregeln aus
design/assets/artstyle-c64/README.md folgen:

  * strikte 16-Farben-C64-Palette (dieselbe Tabelle wie gen_assets_c64.py)
  * Schattierung ueber geordnetes Bayer-Dithering zwischen zwei Palettenfarben
  * tiefes Schwarz, harte Kanten, optionale schwarze Kontur um Figuren
  * native Pixelgroessen der Asset-Typen (Portraet 40x48, Sprite 16x24, ...)

Ausgabe je Foto:
  <name>_c64.png      native Groesse, INDIZIERTES PNG mit eingebetteter Palette
                      (direkt editierbar in Aseprite, LibreSprite, GIMP, Krita,
                      Photoshop, Piskel ...), Transparenz als eigener Index
  <name>_c64_x<N>.png ganzzahlig hochskalierte Vorschau

Beispiele:
  python tools/photo2pixel.py foto.jpg --typ portrait
  python tools/photo2pixel.py figur.png --typ sprite --freistellen
  python tools/photo2pixel.py landschaft.jpg --typ szene --dither 0.8
  python tools/photo2pixel.py foto.jpg --typ portrait --varianten
  python tools/photo2pixel.py *.jpg --groesse 64x64 -o out/
"""

from __future__ import annotations

import argparse
import glob
import sys
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageEnhance, ImageFilter, ImageOps

sys.path.insert(0, str(Path(__file__).resolve().parent))
from gen_assets_c64 import C64, PALETTE_ORDER  # noqa: E402  (zentrale Palette)

# Asset-Typen: native Groesse, Transparenz, Kontur, vertikale Ausrichtung,
# Dither-Staerke und Vorschau-Faktor. Groessen laut artstyle-c64/README.md.
TYPEN = {
    "szene":    dict(size=(320, 200), alpha=False, kontur=False, unten=False, dither=1.0, scale=3),
    "karte":    dict(size=(240, 152), alpha=False, kontur=False, unten=False, dither=1.0, scale=4),
    "portrait": dict(size=(40, 48),   alpha=False, kontur=False, unten=False, dither=0.6, scale=8),
    "gebaeude": dict(size=(32, 32),   alpha=True,  kontur=True,  unten=True,  dither=0.7, scale=8),
    "sprite":   dict(size=(16, 24),   alpha=True,  kontur=True,  unten=True,  dither=0.4, scale=12),
    "gegner":   dict(size=(16, 24),   alpha=True,  kontur=True,  unten=True,  dither=0.4, scale=12),
    "item":     dict(size=(16, 16),   alpha=True,  kontur=True,  unten=False, dither=0.4, scale=12),
}

TRANSPARENT = len(PALETTE_ORDER)          # Palettenindex 16 = durchsichtig
MAGIC = (255, 0, 254)                     # Markerfarbe fuers Freistellen


# --------------------------------------------------------------------------
# Farbraum: Oklab (Abstaende entsprechen der Wahrnehmung besser als RGB)
# --------------------------------------------------------------------------
def srgb_to_oklab(rgb: np.ndarray) -> np.ndarray:
    """rgb: float-Array (..., 3) in 0..1 -> Oklab (..., 3)."""
    lin = np.where(rgb <= 0.04045, rgb / 12.92, ((rgb + 0.055) / 1.055) ** 2.4)
    m1 = np.array([[0.4122214708, 0.5363325363, 0.0514459929],
                   [0.2119034982, 0.6806995451, 0.1073969566],
                   [0.0883024619, 0.2817188376, 0.6299787005]])
    m2 = np.array([[0.2104542553, 0.7936177850, -0.0040720468],
                   [1.9779984951, -2.4285922050, 0.4505937099],
                   [0.0259040371, 0.7827717662, -0.8086757660]])
    lms = np.cbrt(lin @ m1.T)
    return lms @ m2.T


def bayer_matrix(n: int) -> np.ndarray:
    """Bayer-Schwellwertmatrix n x n (n = 2, 4, 8), Werte in 0..1."""
    m = np.array([[0]])
    while m.shape[0] < n:
        m = np.block([[4 * m, 4 * m + 2], [4 * m + 3, 4 * m + 1]])
    return (m + 0.5) / m.size


# --------------------------------------------------------------------------
# Vorverarbeitung
# --------------------------------------------------------------------------
def freistellen(img: Image.Image, toleranz: int) -> Image.Image:
    """Einfarbigen Hintergrund von den Bildecken aus transparent machen."""
    rgb = img.convert("RGB")
    w, h = rgb.size
    for seed in ((0, 0), (w - 1, 0), (0, h - 1), (w - 1, h - 1)):
        if rgb.getpixel(seed) != MAGIC:
            ImageDraw.floodfill(rgb, seed, MAGIC, thresh=toleranz)
    hintergrund = np.all(np.asarray(rgb) == MAGIC, axis=-1)
    out = img.convert("RGBA")
    a = np.asarray(out.getchannel("A")).copy()
    a[hintergrund] = 0
    out.putalpha(Image.fromarray(a))
    return out


def tonwerte(img: Image.Image, a) -> Image.Image:
    """Kontrast, Saettigung, Helligkeit, Schwarzpunkt - auf dem RGB-Anteil."""
    rgb = img.convert("RGB")
    if a.autokontrast:
        rgb = ImageOps.autocontrast(rgb, cutoff=1, preserve_tone=True)
    rgb = ImageEnhance.Brightness(rgb).enhance(a.helligkeit)
    rgb = ImageEnhance.Contrast(rgb).enhance(a.kontrast)
    rgb = ImageEnhance.Color(rgb).enhance(a.saettigung)
    if a.schwarzpunkt > 0:
        b = a.schwarzpunkt
        lut = [int(max(0.0, (v / 255 - b) / (1 - b)) * 255 + 0.5) for v in range(256)]
        rgb = rgb.point(lut * 3)
    out = rgb.convert("RGBA")
    out.putalpha(img.getchannel("A"))
    return out


def verkleinern(img: Image.Image, size, rand: int, mit_alpha: bool,
                unten: bool, einpassen: bool, schaerfe: float, glaetten: bool,
                fokus=(0.5, 0.4), zoom: float = 1.0):
    """Auf native Groesse bringen. Liefert (rgb float 0..1, deckend bool)."""
    w, h = size
    if mit_alpha:
        bbox = img.getchannel("A").getbbox()
        if bbox:
            img = img.crop(bbox)
    if mit_alpha or einpassen:
        # Motiv vollstaendig einpassen, Rest bleibt leer
        iw, ih = w - 2 * rand, h - 2 * rand
        f = min(iw / img.width, ih / img.height)
        nw, nh = max(1, round(img.width * f)), max(1, round(img.height * f))
        ox = rand + (iw - nw) // 2
        oy = rand + (ih - nh if unten else (ih - nh) // 2)
    else:
        # Bild fuellt die Flaeche; Ausschnitt im Zielformat, Lage ueber `fokus`
        # (0..1 je Achse), Groesse ueber `zoom` (1 = groesstmoeglich)
        f = min(img.width / w, img.height / h) / max(1.0, zoom)
        cw, ch = max(1, round(w * f)), max(1, round(h * f))
        x0 = round((img.width - cw) * min(1.0, max(0.0, fokus[0])))
        y0 = round((img.height - ch) * min(1.0, max(0.0, fokus[1])))
        img = img.crop((x0, y0, x0 + cw, y0 + ch))
        nw, nh, ox, oy = w, h, 0, 0

    # Alpha vormultiplizieren, damit kein Hintergrund in die Kanten blutet
    arr = np.asarray(img, dtype=np.float32) / 255.0
    pre = np.dstack([arr[..., :3] * arr[..., 3:], arr[..., 3]])
    kan = [Image.fromarray(pre[..., i]).resize((nw, nh), Image.BOX if i == 3 else Image.LANCZOS)
           for i in range(4)]
    small = np.dstack([np.asarray(k) for k in kan])
    alpha = small[..., 3]
    rgb = np.clip(small[..., :3] / np.maximum(alpha[..., None], 1e-4), 0, 1)

    if schaerfe > 0 or glaetten:
        im8 = Image.fromarray((rgb * 255 + 0.5).astype(np.uint8))
        if glaetten:
            im8 = im8.filter(ImageFilter.MedianFilter(3))
        if schaerfe > 0:
            im8 = im8.filter(ImageFilter.UnsharpMask(radius=1, percent=int(schaerfe * 100), threshold=0))
        rgb = np.asarray(im8, dtype=np.float32) / 255.0

    voll_rgb = np.zeros((h, w, 3), dtype=np.float32)
    voll_a = np.zeros((h, w), dtype=bool)
    voll_rgb[oy:oy + nh, ox:ox + nw] = rgb
    voll_a[oy:oy + nh, ox:ox + nw] = alpha >= 0.5
    if not mit_alpha:
        voll_a[:] = True                  # leere Flaeche = Schwarz
    return voll_rgb, voll_a


# --------------------------------------------------------------------------
# Kern: Palettenreduktion mit geordnetem Dithering zwischen Farbpaaren
# --------------------------------------------------------------------------
def quantisieren(rgb: np.ndarray, farben: list[str], dither: float,
                 bayer_n: int, ruhe: float) -> np.ndarray:
    """rgb (h, w, 3) float -> Palettenindizes (h, w), Index in PALETTE_ORDER.

    Fuer jeden Pixel wird das Farbpaar gesucht, dessen Mischung den Ton am
    besten trifft; die Bayer-Matrix entscheidet, welche der beiden Farben
    gesetzt wird. `ruhe` bestraft Paare aus weit auseinanderliegenden Farben
    quadratisch (sonst entsteht unruhiges Schwarz/Weiss-Rauschen statt
    sauberer Rampen); bei 0.25 mischen sich Farben bis Oklab-Abstand ~0.45.
    """
    h, w, _ = rgb.shape
    idx = np.array([PALETTE_ORDER.index(f) for f in farben])
    pal = srgb_to_oklab(np.array([C64[f] for f in farben], dtype=np.float64) / 255.0)
    i1, i2 = np.triu_indices(len(farben))
    c1, d = pal[i1], pal[i2] - pal[i1]
    dd = np.sum(d * d, axis=1)
    stufen = bayer_n * bayer_n

    px = srgb_to_oklab(rgb.reshape(-1, 3).astype(np.float64))
    schwelle = np.tile(bayer_matrix(bayer_n), (h // bayer_n + 1, w // bayer_n + 1))[:h, :w].ravel()
    schwelle = 0.5 + (schwelle - 0.5) * dither

    out = np.empty(h * w, dtype=np.uint8)
    for s in range(0, h * w, 8192):
        p = px[s:s + 8192, None, :]                                   # (n, 1, 3)
        t = np.sum((p - c1) * d, axis=2) / np.maximum(dd, 1e-12)      # (n, paare)
        t = np.round(np.clip(t, 0, 1) * stufen) / stufen
        mix = c1 + t[..., None] * d
        fehler = np.sum((p - mix) ** 2, axis=2)
        fehler += ruhe * 5.0 * dd * dd * ((t > 0) & (t < 1))
        best = np.argmin(fehler, axis=1)
        tb = t[np.arange(len(best)), best]
        zweite = tb > schwelle[s:s + 8192]
        out[s:s + 8192] = idx[np.where(zweite, i2[best], i1[best])]
    return out.reshape(h, w)


def kontur(indices: np.ndarray, deckend: np.ndarray):
    """1 Pixel schwarze Aussenkontur um die deckende Flaeche legen."""
    p = np.pad(deckend, 1)
    nachbar = p[:-2, 1:-1] | p[2:, 1:-1] | p[1:-1, :-2] | p[1:-1, 2:]
    rand = nachbar & ~deckend
    indices = indices.copy()
    indices[rand] = PALETTE_ORDER.index("black")
    return indices, deckend | rand


# --------------------------------------------------------------------------
# Ausgabe
# --------------------------------------------------------------------------
def als_png(indices: np.ndarray, deckend: np.ndarray) -> Image.Image:
    """Indiziertes Bild (Modus P) mit C64-Palette; Index 16 = transparent."""
    data = np.where(deckend, indices, TRANSPARENT).astype(np.uint8)
    img = Image.fromarray(data, mode="P")
    flat = [v for name in PALETTE_ORDER for v in C64[name]] + [0, 0, 0]
    img.putpalette(flat)
    if not deckend.all():
        img.info["transparency"] = TRANSPARENT
    return img


def speichern(img: Image.Image, pfad: Path) -> None:
    extra = {"transparency": img.info["transparency"]} if "transparency" in img.info else {}
    img.save(pfad, optimize=False, **extra)


def hochskalieren(img: Image.Image, faktor: int) -> Image.Image:
    gross = img.resize((img.width * faktor, img.height * faktor), Image.NEAREST)
    if "transparency" in img.info:
        gross.info["transparency"] = img.info["transparency"]
    return gross


def gpl_schreiben(pfad: Path) -> None:
    """Palette als GIMP-Palette (.gpl) - laden auch Aseprite, Krita, Inkscape."""
    zeilen = ["GIMP Palette", "Name: C64 (Pepto) - Endzeit", "Columns: 8", "#"]
    zeilen += [f"{r:3d} {g:3d} {b:3d}\t{name}" for name in PALETTE_ORDER
               for r, g, b in [C64[name]]]
    pfad.write_text("\n".join(zeilen) + "\n", encoding="ascii")


# --------------------------------------------------------------------------
# Ablauf
# --------------------------------------------------------------------------
def umwandeln(quelle: Image.Image, a) -> Image.Image:
    """Komplette Pipeline: Foto -> indiziertes Bild in nativer Groesse."""
    img = ImageOps.exif_transpose(quelle).convert("RGBA")
    if a.freistellen:
        img = freistellen(img, a.toleranz)
    hat_alpha = img.getchannel("A").getextrema()[0] < 255
    mit_alpha = a.alpha and hat_alpha
    img = tonwerte(img, a)
    rand = 1 if (a.kontur and mit_alpha) else 0
    rgb, deckend = verkleinern(img, a.size, rand, mit_alpha, a.unten, a.einpassen, a.schaerfe, a.glaetten,
                               (a.fokus_x, a.fokus_y), a.zoom)
    indices = quantisieren(rgb, a.farben, a.dither, a.bayer, a.ruhe)
    if a.kontur and mit_alpha:
        indices, deckend = kontur(indices, deckend)
    return als_png(indices, deckend)


def varianten(quelle: Image.Image, a, faktor: int) -> Image.Image:
    """Uebersichtsblatt: Dither-Staerke (Spalten) x Kontrast (Zeilen)."""
    dithers, kontraste = (0.0, 0.5, 1.0), (1.0, 1.3, 1.7)
    w, h = a.size[0] * faktor, a.size[1] * faktor
    pad, kopf = 8, 14
    blatt = Image.new("RGB", (pad + 3 * (w + pad), pad + 3 * (h + kopf + pad)), C64["dgrey"])
    zeichner = ImageDraw.Draw(blatt)
    for zy, k in enumerate(kontraste):
        for zx, d in enumerate(dithers):
            b = argparse.Namespace(**{**vars(a), "dither": d, "kontrast": k})
            bild = hochskalieren(umwandeln(quelle, b), faktor).convert("RGBA")
            x, y = pad + zx * (w + pad), pad + zy * (h + kopf + pad)
            zeichner.text((x, y), f"--dither {d} --kontrast {k}", fill=C64["yellow"])
            blatt.paste(bild, (x, y + kopf), bild)
    return blatt


def argumente(argv=None):
    p = argparse.ArgumentParser(
        description="Fotos in C64/SKALD-Pixelgrafik umwandeln (indiziertes PNG).",
        epilog="Asset-Typen: " + ", ".join(
            f"{k} {v['size'][0]}x{v['size'][1]}" for k, v in TYPEN.items()))
    p.add_argument("fotos", nargs="*", help="Eingabebilder (JPG, PNG, WEBP ...), auch Muster wie *.jpg")
    p.add_argument("--typ", choices=TYPEN, default="szene", help="Asset-Typ = Groesse + Voreinstellungen (Standard: szene)")
    p.add_argument("--groesse", metavar="BxH", help="eigene native Groesse, z.B. 64x64 (ueberschreibt --typ)")
    p.add_argument("-o", "--ausgabe", type=Path, help="Zielordner (Standard: neben dem Foto)")
    p.add_argument("--vorschau", type=int, metavar="N", help="Vergroesserungsfaktor der Vorschau, 0 = keine")
    p.add_argument("--varianten", action="store_true", help="statt eines Bildes ein 3x3-Vergleichsblatt (Dither x Kontrast) schreiben")
    p.add_argument("--gpl", type=Path, metavar="DATEI", help="Palette zusaetzlich als GIMP-Palette (.gpl) schreiben")

    g = p.add_argument_group("Stil")
    g.add_argument("--dither", type=float, help="Dither-Staerke 0..1 (0 = flaechig, 1 = volle Rampen)")
    g.add_argument("--bayer", type=int, choices=(2, 4, 8), default=8, help="Groesse der Bayer-Matrix (Standard: 8, laut Stilregel)")
    g.add_argument("--ruhe", type=float, default=0.25, help="hoeher = nur noch aehnliche Farben werden gemischt, ruhigeres Bild (Standard: 0.25)")
    g.add_argument("--farben", metavar="LISTE", help="nur diese Palettenfarben verwenden, kommagetrennt: " + ",".join(PALETTE_ORDER))
    g.add_argument("--kontur", action=argparse.BooleanOptionalAction, default=None, help="schwarze 1-Pixel-Kontur um freigestellte Motive")

    g = p.add_argument_group("Bildaufbereitung")
    g.add_argument("--kontrast", type=float, default=1.3, help="Standard: 1.3")
    g.add_argument("--saettigung", type=float, default=1.3, help="Standard: 1.3")
    g.add_argument("--helligkeit", type=float, default=1.0, help="Standard: 1.0")
    g.add_argument("--schwarzpunkt", type=float, default=0.06, help="dunkle Toene bis hierhin (0..1) werden reines Schwarz (Standard: 0.06)")
    g.add_argument("--schaerfe", type=float, default=0.6, help="Nachschaerfen nach dem Verkleinern, 0 = aus (Standard: 0.6)")
    g.add_argument("--glaetten", action="store_true", help="Bildrauschen/Einzelpixel vor der Farbreduktion glaetten (kostet feine Details)")
    g.add_argument("--autokontrast", action=argparse.BooleanOptionalAction, default=True, help="Tonwertumfang automatisch spreizen")

    g = p.add_argument_group("Zuschnitt / Transparenz")
    g.add_argument("--freistellen", action="store_true", help="einfarbigen Hintergrund (von den Ecken aus) entfernen")
    g.add_argument("--toleranz", type=int, default=40, help="Farbtoleranz beim Freistellen (Standard: 40)")
    g.add_argument("--alpha", action=argparse.BooleanOptionalAction, default=None, help="Transparenz des Fotos uebernehmen")
    g.add_argument("--zoom", type=float, default=1.0, help="Ausschnitt verkleinern = naeher heran (Standard: 1.0)")
    g.add_argument("--fokus-x", type=float, default=0.5, help="Lage des Ausschnitts, 0 = links, 1 = rechts (Standard: 0.5)")
    g.add_argument("--fokus-y", type=float, default=0.4, help="Lage des Ausschnitts, 0 = oben, 1 = unten (Standard: 0.4)")
    g.add_argument("--einpassen", action="store_true", help="ganzes Bild zeigen (schwarze Raender) statt zuschneiden")

    a = p.parse_args(argv)
    t = TYPEN[a.typ]
    a.size = t["size"]
    if a.groesse:
        try:
            a.size = tuple(int(v) for v in a.groesse.lower().split("x"))
            assert len(a.size) == 2 and min(a.size) > 0
        except (ValueError, AssertionError):
            p.error("--groesse erwartet BxH, z.B. 64x64")
    a.unten = t["unten"]
    for name in ("dither", "kontur", "alpha"):
        if getattr(a, name) is None:
            setattr(a, name, t[name])
    if a.freistellen:
        a.alpha = True
    if a.vorschau is None:
        a.vorschau = max(1, min(t["scale"], 960 // max(a.size))) if not a.groesse else max(1, 640 // max(a.size))
    a.farben = [f.strip() for f in a.farben.split(",")] if a.farben else list(PALETTE_ORDER)
    unbekannt = [f for f in a.farben if f not in C64]
    if unbekannt or len(a.farben) < 2:
        p.error(f"--farben: mindestens 2 Farben aus {', '.join(PALETTE_ORDER)}"
                + (f" (unbekannt: {', '.join(unbekannt)})" if unbekannt else ""))
    if not a.fotos and not a.gpl:
        p.error("kein Foto angegeben")
    return a


def main(argv=None) -> int:
    a = argumente(argv)
    if a.gpl:
        gpl_schreiben(a.gpl)
        print(f"Palette  {a.gpl}")

    pfade = []
    for muster in a.fotos:
        treffer = sorted(glob.glob(muster))
        pfade += [Path(t) for t in treffer] or [Path(muster)]

    fehler = 0
    for pfad in pfade:
        try:
            with Image.open(pfad) as quelle:
                quelle.load()
        except (OSError, ValueError) as e:
            print(f"FEHLER   {pfad}: {e}", file=sys.stderr)
            fehler += 1
            continue
        ziel = a.ausgabe or pfad.parent
        ziel.mkdir(parents=True, exist_ok=True)
        if a.varianten:
            out = ziel / f"{pfad.stem}_c64_varianten.png"
            varianten(quelle, a, max(1, min(a.vorschau or 1, 400 // max(a.size)))).save(out)
            print(f"Varianten {out}")
            continue
        bild = umwandeln(quelle, a)
        out = ziel / f"{pfad.stem}_c64.png"
        speichern(bild, out)
        genutzt = len(set(np.unique(np.asarray(bild)).tolist()) - {TRANSPARENT})
        print(f"Nativ    {out}  ({bild.width}x{bild.height}, {genutzt} Farben)")
        if a.vorschau > 1:
            out = ziel / f"{pfad.stem}_c64_x{a.vorschau}.png"
            speichern(hochskalieren(bild, a.vorschau), out)
            print(f"Vorschau {out}")
    return 1 if fehler else 0


if __name__ == "__main__":
    sys.exit(main())
