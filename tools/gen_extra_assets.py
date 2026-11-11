"""Extra textures: dark biome tones, armour, better tools, totems, compass, menu art."""
import os

import numpy as np
from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TEX = os.path.join(ROOT, "src", "main", "resources", "assets", "arkandayim", "textures")
RNG = np.random.default_rng(20261111)


def ensure(*parts):
    path = os.path.join(*parts)
    os.makedirs(path, exist_ok=True)
    return path


def noise_block(name, base, spread, blotch=None, blotch_count=0, palette=None):
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            v = base + int(RNG.integers(-spread, spread + 1))
            px[x, y] = palette(v) if palette else (v, v, v, 255)
    if blotch is not None:
        for _ in range(blotch_count):
            cx, cy = int(RNG.integers(0, 16)), int(RNG.integers(0, 16))
            for dy in (-1, 0, 1):
                for dx in (-1, 0, 1):
                    x, y = cx + dx, cy + dy
                    if 0 <= x < 16 and 0 <= y < 16 and abs(dx) + abs(dy) < 2:
                        px[x, y] = blotch + (255,)
    img.save(os.path.join(TEX, "block", name))


def dark_blocks():
    noise_block("dark_stone.png", 44, 7, blotch=(30, 30, 34), blotch_count=18,
                palette=lambda v: (max(0, v), max(0, v), max(0, v + 3), 255))

    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            v = 26 + int(RNG.integers(-7, 8))
            px[x, y] = (max(0, v), max(0, v + 3), max(0, v - 2), 255)
    for _ in range(30):
        px[int(RNG.integers(0, 16)), int(RNG.integers(0, 16))] = (14, 16, 12, 255)
    for _ in range(10):
        px[int(RNG.integers(0, 16)), int(RNG.integers(0, 16))] = (52, 56, 44, 255)
    img.save(os.path.join(TEX, "block", "black_leaves.png"))

    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            v = 104 + int(RNG.integers(-16, 17))
            px[x, y] = (v, v - 2, v - 4, 255)
    for _ in range(22):
        px[int(RNG.integers(0, 16)), int(RNG.integers(0, 16))] = (68, 66, 62, 255)
    for _ in range(12):
        px[int(RNG.integers(0, 16)), int(RNG.integers(0, 16))] = (148, 146, 140, 255)
    img.save(os.path.join(TEX, "block", "ash_block.png"))


ARMOR_DARK = (58, 33, 16, 255)
ARMOR_MID = (128, 84, 42, 255)
ARMOR_LIGHT = (192, 146, 88, 255)
ARMOR_HI = (234, 202, 152, 255)
ARMOR_EDGE = (34, 18, 9, 255)


def plate(px, x0, y0, x1, y1):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            if x == x0 or x == x1 or y == y0 or y == y1:
                px[x, y] = ARMOR_EDGE
            elif (x + y) % 5 == 0:
                px[x, y] = ARMOR_LIGHT
            else:
                px[x, y] = ARMOR_MID if (x * 3 + y) % 7 else ARMOR_DARK
    px[x0 + 1, y0 + 1] = ARMOR_HI


def armor_items():
    def blank():
        img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
        return img, img.load()

    img, px = blank()
    plate(px, 4, 3, 11, 8)
    for x in range(5, 11):
        px[x, 9] = ARMOR_DARK
    for x in (6, 9):
        px[x, 4] = (12, 8, 6, 255)
    img.save(os.path.join(TEX, "item", "brown_helmet.png"))

    img, px = blank()
    plate(px, 3, 3, 12, 11)
    for y in range(4, 11):
        px[3, y] = ARMOR_EDGE
        px[12, y] = ARMOR_EDGE
    for x in range(6, 10):
        px[x, 3] = (12, 8, 6, 255)
    for x in range(4, 12):
        px[x, 12] = ARMOR_DARK
    img.save(os.path.join(TEX, "item", "brown_chestplate.png"))

    img, px = blank()
    plate(px, 4, 3, 11, 12)
    for y in range(3, 13):
        px[7, y] = ARMOR_EDGE
        px[8, y] = ARMOR_EDGE
    img.save(os.path.join(TEX, "item", "brown_leggings.png"))

    img, px = blank()
    plate(px, 4, 7, 7, 13)
    plate(px, 8, 7, 11, 13)
    for x in range(4, 12):
        px[x, 6] = ARMOR_DARK
    img.save(os.path.join(TEX, "item", "brown_boots.png"))


def armor_layers():
    ensure(TEX, "models", "armor")
    for name in ("brown_layer_1.png", "brown_layer_2.png"):
        img = Image.new("RGBA", (64, 32))
        px = img.load()
        for y in range(32):
            for x in range(64):
                v = 74 + int(RNG.integers(-12, 13))
                px[x, y] = (v, int(v * 0.66), int(v * 0.36), 255)
        for y in range(32):
            for x in range(64):
                if x % 8 == 0 or y % 8 == 0:
                    px[x, y] = (44, 26, 13, 255)
                elif (x + y) % 13 == 0:
                    px[x, y] = (196, 152, 96, 255)
        img.save(os.path.join(TEX, "models", "armor", name))


def better_tools():
    outline = (30, 14, 6, 255)
    handle_dark = (58, 30, 14, 255)
    handle_light = (108, 62, 30, 255)
    head_dark = (92, 48, 16, 255)
    head_mid = (170, 100, 32, 255)
    head_light = (238, 190, 112, 255)
    sheen = (255, 246, 220, 255)

    def blank():
        img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
        return img, img.load()

    def stick(px, length=10):
        for t in range(length):
            x, y = 2 + t, 14 - t
            if 0 <= x < 16 and 0 <= y < 16:
                px[x, y] = handle_light
            if 0 <= x + 1 < 16 and 0 <= y < 16:
                px[x + 1, y] = handle_dark
            if 0 <= x < 16 and 0 <= y - 1 < 16:
                px[x, y - 1] = outline
            if 0 <= x + 1 < 16 and 0 <= y - 1 < 16:
                px[x + 1, y - 1] = outline

    img, px = blank()
    stick(px, 10)
    for x, y in [(7, 6), (8, 5), (9, 4), (10, 3), (11, 2), (12, 2), (13, 3), (13, 4)]:
        px[x, y] = head_mid
        px[x, y + 1] = head_light if (x + y) % 2 else head_mid
        px[x, y - 1] = head_dark
        px[x, y + 2] = outline
    px[9, 4] = sheen
    px[12, 2] = sheen
    img.save(os.path.join(TEX, "item", "brown_pickaxe.png"))

    img, px = blank()
    stick(px, 9)
    for y in range(1, 6):
        for x in range(10, 14):
            px[x, y] = head_mid if (x + y) % 4 else head_light
    for x in range(10, 14):
        px[x, 0] = outline
        px[x, 6] = outline
    for y in range(1, 6):
        px[9, y] = outline
        px[14, y] = outline
    px[11, 1] = sheen
    px[12, 2] = head_light
    for x in (11, 12):
        px[x, 5] = head_dark
    img.save(os.path.join(TEX, "item", "brown_shovel.png"))

    img, px = blank()
    stick(px, 10)
    for y in range(1, 7):
        for x in range(10, 14):
            px[x, y] = head_mid if (x + y) % 5 else head_light
    for x in range(10, 14):
        px[x, 0] = outline
        px[x, 7] = outline
    for y in range(1, 7):
        px[9, y] = outline
        px[14, y] = outline
    px[11, 2] = sheen
    px[12, 3] = head_light
    px[13, 1] = head_light
    img.save(os.path.join(TEX, "item", "brown_axe.png"))

    img, px = blank()
    stick(px, 10)
    for x in range(9, 14):
        px[x, 2] = head_mid
        px[x, 3] = head_dark
        px[x, 1] = outline
        px[x, 4] = outline
    px[9, 0] = outline
    px[13, 0] = outline
    px[9, 2] = head_light
    px[12, 2] = sheen
    px[10, 4] = outline
    px[10, 5] = head_mid
    px[11, 5] = head_dark
    px[9, 5] = outline
    px[12, 5] = outline
    img.save(os.path.join(TEX, "item", "brown_hoe.png"))


def compass_item():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.ellipse((1, 1, 14, 14), fill=(44, 32, 22, 255), outline=(150, 104, 40, 255))
    draw.ellipse((2, 2, 13, 13), fill=(24, 18, 16, 255))
    draw.ellipse((3, 3, 12, 12), fill=(232, 226, 206, 255))
    draw.ellipse((4, 4, 11, 11), fill=(196, 188, 164, 255))
    draw.polygon([(8, 3), (10, 8), (8, 9)], fill=(180, 34, 22, 255))
    draw.polygon([(8, 13), (6, 8), (8, 7)], fill=(38, 38, 44, 255))
    draw.point((8, 8), fill=(20, 16, 14, 255))
    img.save(os.path.join(TEX, "item", "arkandayim_compass.png"))


def totem(name, body, eye, accent):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    for y in range(2, 15):
        half = 4 if y < 8 else 3
        for x in range(8 - half, 8 + half):
            shade = body + int(RNG.integers(-5, 6))
            px[x, y] = (max(0, shade), max(0, shade - 2), max(0, shade + 4), 255)
    for x in range(5, 11):
        px[x, 5] = eye
    px[7, 5] = (255, 255, 255, 255)
    px[8, 5] = (255, 255, 255, 255)
    for x in range(6, 10):
        px[x, 10] = accent
    for x in range(6, 10):
        px[x, 11] = (max(0, accent[0] - 40), max(0, accent[1] - 30), max(0, accent[2] - 20), 255)
    img.save(os.path.join(TEX, "item", name))


def menu_backgrounds():
    folder = ensure(TEX, "gui", "menu")

    def scene(index):
        w, h = 960, 540
        img = Image.new("RGBA", (w, h))
        draw = ImageDraw.Draw(img)
        top = 8 + index * 2
        for y in range(h):
            t = y / h
            draw.line([(0, y), (w, y)], fill=(int(top + 18 * t), int(top + 4 + 10 * t),
                                              int(top + 14 + 20 * t), 255))
        for _ in range(260):
            draw.point((int(RNG.integers(0, w)), int(RNG.integers(0, h // 2))),
                       fill=(230, 232, 240, int(RNG.integers(30, 150))))
        for layer in range(4):
            depth = 1.0 - layer * 0.18
            base = h - 70 - layer * 26
            for _ in range(26):
                x = int(RNG.integers(-40, w + 40))
                height = int(RNG.integers(80, 230) * depth)
                width = int(RNG.integers(18, 44) * depth)
                shade = int(6 + layer * 7)
                draw.polygon([(x, base), (x + width // 2, base - height), (x + width, base)],
                             fill=(shade, shade + 1, shade + 2, 255))
        for _ in range(9):
            gx = int(RNG.integers(60, w - 60))
            gy = int(RNG.integers(h - 260, h - 60))
            for r in range(46, 0, -6):
                draw.ellipse((gx - r, gy - r, gx + r, gy + r),
                             fill=(255, 168, 72, max(3, 22 - r // 3)))
            draw.ellipse((gx - 3, gy - 3, gx + 3, gy + 3), fill=(255, 236, 190, 210))
        if index == 2:
            for _ in range(3):
                ex = int(RNG.integers(120, w - 120))
                ey = int(RNG.integers(150, h - 160))
                draw.ellipse((ex - 4, ey - 4, ex + 4, ey + 4), fill=(240, 244, 236, 220))
        img.convert("RGB").save(os.path.join(folder, "bg_%d.png" % (index + 1)))

    for i in range(3):
        scene(i)


def main():
    ensure(TEX, "block")
    ensure(TEX, "item")
    ensure(TEX, "gui")
    dark_blocks()
    armor_items()
    armor_layers()
    better_tools()
    compass_item()
    totem("shadow_totem.png", 26, (150, 220, 255, 255), (30, 90, 160, 255))
    totem("guardian_totem.png", 54, (255, 226, 140, 255), (190, 120, 30, 255))
    menu_backgrounds()
    print("extra assets generated")


if __name__ == "__main__":
    main()
