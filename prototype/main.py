"""
Endzeit-Wirtschaftssimulator – Bewegungs-Prototyp.

Minimaler Prototyp gemäß erster Prototyping-Anforderung: eine Spielfigur
wird mit WASD über die Hauptkarte bewegt. Keine Interaktion, keine
Animation. Rendering folgt den Regeln aus TICKET-009/-010: native
Pixelauflösung, ganzzahlige Skalierung, Nearest-Neighbor (harte Kanten).

Steuerung:
    W/A/S/D  – Figur bewegen (Pfeiltasten gehen auch)
    ESC      – Beenden

Die Karte und der Figuren-Sprite werden aus design/assets/artstyle/
geladen (die dort abgelegten Mockups sind bereits 6x skaliert; wir
rechnen sie auf die native Auflösung herunter und skalieren dann als
Ganzes wieder hoch, damit Figur und Karte im selben Pixelraster liegen).
"""

import sys
from pathlib import Path

import pygame

# Die Mockups in design/assets/artstyle wurden mit SCALE=6 exportiert.
ASSET_SCALE = 6
# Anzeige-Skalierung des nativen Bildes (ganzzahlig, TICKET-009 Abschnitt 2).
DISPLAY_SCALE = 5
# Bewegungsgeschwindigkeit in nativen Pixeln pro Sekunde.
SPEED = 60

ASSET_DIR = Path(__file__).resolve().parent.parent / "design" / "assets" / "artstyle"
MAP_FILE = ASSET_DIR / "02_hauptkarte.png"
SPRITE_FILE = ASSET_DIR / "04c_sprite_wastelander.png"


def load_native(path: Path) -> pygame.Surface:
    """Lädt ein 6x-Mockup und rechnet es verlustfrei auf native Größe herunter."""
    image = pygame.image.load(str(path))
    native_size = (image.get_width() // ASSET_SCALE, image.get_height() // ASSET_SCALE)
    # subsample statt smoothscale: harte Pixelkanten bleiben erhalten
    return pygame.transform.scale(image, native_size)


def main() -> None:
    pygame.init()
    pygame.display.set_caption("Endzeit-Prototyp – WASD bewegt die Figur")

    world = load_native(MAP_FILE).convert()
    sprite = load_native(SPRITE_FILE).convert()
    # Der Sprite-Mockup hat einen near-black Hintergrund; als transparent markieren.
    sprite.set_colorkey(sprite.get_at((0, 0)))

    screen = pygame.display.set_mode(
        (world.get_width() * DISPLAY_SCALE, world.get_height() * DISPLAY_SCALE)
    )
    canvas = pygame.Surface(world.get_size())
    clock = pygame.time.Clock()

    # Startposition: Kartenmitte, Position in floats für gleichmäßige Bewegung.
    x = world.get_width() / 2 - sprite.get_width() / 2
    y = world.get_height() / 2 - sprite.get_height() / 2

    max_x = world.get_width() - sprite.get_width()
    max_y = world.get_height() - sprite.get_height()

    running = True
    while running:
        dt = clock.tick(60) / 1000.0

        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                running = False
            elif event.type == pygame.KEYDOWN and event.key == pygame.K_ESCAPE:
                running = False

        keys = pygame.key.get_pressed()
        dx = (keys[pygame.K_d] or keys[pygame.K_RIGHT]) - (keys[pygame.K_a] or keys[pygame.K_LEFT])
        dy = (keys[pygame.K_s] or keys[pygame.K_DOWN]) - (keys[pygame.K_w] or keys[pygame.K_UP])

        x = min(max(x + dx * SPEED * dt, 0), max_x)
        y = min(max(y + dy * SPEED * dt, 0), max_y)

        canvas.blit(world, (0, 0))
        # Blit auf ganze Pixel gerundet, damit die Figur im Pixelraster bleibt.
        canvas.blit(sprite, (round(x), round(y)))
        pygame.transform.scale(canvas, screen.get_size(), screen)
        pygame.display.flip()

    pygame.quit()
    sys.exit(0)


if __name__ == "__main__":
    main()
