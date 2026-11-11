#!/usr/bin/env python
"""Generates all mod textures (entity skin, eyes, ore, ingot, sword)."""
from pathlib import Path
import random

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src" / "main" / "resources" / "assets" / "arkandayim" / "textures"
(ASSETS / "entity").mkdir(parents=True, exist_ok=True)
(ASSETS / "block").mkdir(parents=True, exist_ok=True)
(ASSETS / "item").mkdir(parents=True, exist_ok=True)

random.seed(7)

BASE = (17, 8, 9, 255)
MID = (30, 12, 13, 255)
DARK = (11, 5, 6, 255)
EYE = (255, 138, 30, 255)
EYE_HOT = (255, 196, 120, 255)

skin = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
px = skin.load()


def fill_region(x0, y0, w, h, color, noise=0):
    for x in range(x0, x0 + w):
        for y in range(y0, y0 + h):
            c = list(color)
            if noise:
                d = random.randint(-noise, noise)
                c[0] = max(0, min(255, c[0] + d))
                c[1] = max(0, min(255, c[1] + d))
                c[2] = max(0, min(255, c[2] + d))
            px[x, y] = tuple(c)


# Player-skin layout base regions
fill_region(0, 0, 32, 16, MID, 4)      # head
fill_region(16, 16, 24, 16, MID, 5)    # body
fill_region(40, 16, 16, 16, BASE, 4)   # right arm
fill_region(32, 48, 16, 16, BASE, 4)   # left arm
fill_region(0, 16, 16, 16, DARK, 3)    # right leg
fill_region(16, 48, 16, 16, DARK, 3)   # left leg

# Head front face detail (8,8 -> 16,16)
fill_region(8, 8, 8, 8, (24, 10, 11, 255), 3)
for y in range(11, 13):
    for x in (9, 10, 13, 14):
        px[x, y] = EYE
px[10, 11] = EYE_HOT
px[13, 11] = EYE_HOT
# thin mouth line
for x in range(10, 14):
    px[x, 14] = (6, 3, 4, 255)

# Body cracks / panel lines
for _ in range(14):
    x = random.randint(20, 26)
    y = random.randint(20, 30)
    px[x, y] = (48, 16, 14, 255)
for y in range(20, 32, 4):
    for x in range(20, 28):
        if random.random() < 0.4:
            px[x, y] = (10, 4, 5, 255)

# Shoulder / arm shading
for y in range(16, 32):
    px[40, y] = (12, 6, 7, 255)
    px[55, y] = (12, 6, 7, 255)

# Keep overlay regions transparent so the hat layer does not hide the eyes
for (x0, y0, w, h) in [(32, 0, 32, 16), (16, 32, 24, 16), (40, 32, 16, 16),
                       (0, 32, 16, 16), (48, 48, 16, 16), (0, 48, 16, 16)]:
    for x in range(x0, x0 + w):
        for y in range(y0, y0 + h):
            px[x, y] = (0, 0, 0, 0)

skin.save(ASSETS / "entity" / "aras.png")

eyes = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
ep = eyes.load()
for y in range(11, 13):
    for x in (9, 10, 13, 14):
        ep[x, y] = (255, 160, 60, 255)
ep[10, 11] = (255, 235, 190, 255)
ep[13, 11] = (255, 235, 190, 255)
eyes.save(ASSETS / "entity" / "aras_eyes.png")

# ---------------- brown ore ----------------
ore = Image.new("RGBA", (16, 16), (0, 0, 0, 255))
op = ore.load()
for x in range(16):
    for y in range(16):
        d = random.randint(-12, 12)
        op[x, y] = (118 + d, 108 + d, 100 + d, 255)
for _ in range(9):
    cx = random.randint(1, 14)
    cy = random.randint(1, 14)
    for dx, dy in [(0, 0), (1, 0), (0, 1), (1, 1)]:
        x, y = cx + dx, cy + dy
        if 0 <= x < 16 and 0 <= y < 16:
            op[x, y] = (92, 52, 24, 255)
    op[min(15, cx), min(15, cy)] = (146, 92, 46, 255)
draw = ImageDraw.Draw(ore)
draw.rectangle([0, 0, 15, 15], outline=(70, 62, 58, 255))
ore.save(ASSETS / "block" / "brown_ore.png")

# ---------------- brown ingot ----------------
ingot = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
ip = ingot.load()
body = [(4, 8), (11, 8), (13, 11), (3, 11)]


def bar(y_start, y_end, color):
    for y in range(y_start, y_end):
        for x in range(3 + (11 - y_start)//1, 13):
            pass


for y in range(7, 13):
    x0 = 3 + (12 - y) // 3
    x1 = 12 - (y - 7) // 4
    for x in range(x0, x1):
        if y <= 8:
            ip[x, y] = (166, 108, 56, 255)
        elif y <= 10:
            ip[x, y] = (128, 76, 34, 255)
        else:
            ip[x, y] = (92, 52, 22, 255)
for x in range(4, 12):
    ip[x, 7] = (196, 138, 78, 255)
    ip[x, 12] = (66, 36, 14, 255)
for y in range(7, 13):
    ip[3 + max(0, (12 - y) // 3), y] = (66, 36, 14, 255)
    ip[11, y] = (66, 36, 14, 255)
ingot.save(ASSETS / "item" / "brown_ingot.png")

# ---------------- brown sword ----------------
sword = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
sp = sword.load()
blade = (176, 118, 62, 255)
blade_hi = (224, 172, 112, 255)
blade_dark = (110, 68, 30, 255)
for i in range(9):
    x = 5 + i
    y = 12 - i
    if 0 <= x < 16 and 0 <= y < 16:
        sp[x, y] = blade
        if x + 1 < 16:
            sp[x + 1, y] = blade_dark
        if y + 1 < 16:
            sp[x, y + 1] = blade_dark if i > 0 else blade_hi
sp[14, 2] = blade_hi
sp[13, 2] = blade_hi
sp[14, 3] = blade
# guard
for x in range(3, 8):
    sp[x, 11] = (72, 40, 18, 255)
for y in range(9, 13):
    sp[4, y] = (86, 48, 22, 255)
# handle
for i in range(3):
    sp[3 - i, 13 + i] = (48, 26, 12, 255)
sp[1, 14] = (150, 96, 46, 255)
sword.save(ASSETS / "item" / "brown_sword.png")

print("textures written to", ASSETS)
