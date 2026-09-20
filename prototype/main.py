"""
Endzeit-Wirtschaftssimulator - Bewegungs-Prototyp.

Minimaler Prototyp: eine Spielfigur wird mit WASD ueber die Hauptkarte
bewegt. Keine Interaktion, keine Animation. Rendering folgt den Regeln aus
TICKET-009/-010 in der C64-Stilrevision: native Pixelaufloesung, striktes
Nearest-Neighbor, ganzzahlige Skalierung.

Steuerung:
    W/A/S/D  - Figur bewegen (Pfeiltasten gehen auch)
    ESC      - Beenden

Karte und Figur werden direkt in nativer Aufloesung aus
design/assets/artstyle-c64/native/ geladen.
"""

import sys
from pathlib import Path

import pygame

# Anzeige-Skalierung des nativen Bildes (ganzzahlig, TICKET-009 Abschnitt 2).
DISPLAY_SCALE = 4
# Bewegungsgeschwindigkeit in nativen Pixeln pro Sekunde.
SPEED = 48

ASSET_DIR = (Path(__file__).resolve().parent.parent
             / "design" / "assets" / "artstyle-c64" / "native")
MAP_FILE = ASSET_DIR / "02_hauptkarte.png"
SPRITE_FILE = ASSET_DIR / "07c_sprite_wastelander.png"


def main() -> None:
    pygame.init()
    pygame.display.set_caption("Endzeit-Prototyp - WASD bewegt die Figur")

    world = pygame.image.load(str(MAP_FILE))
    sprite = pygame.image.load(str(SPRITE_FILE))

    screen = pygame.display.set_mode(
        (world.get_width() * DISPLAY_SCALE, world.get_height() * DISPLAY_SCALE))
    world = world.convert()
    sprite = sprite.convert_alpha()   # Assets tragen einen echten Alphakanal

    canvas = pygame.Surface(world.get_size())
    clock = pygame.time.Clock()

    # Startposition: Kartenmitte, Position in floats fuer gleichmaessige Bewegung.
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
