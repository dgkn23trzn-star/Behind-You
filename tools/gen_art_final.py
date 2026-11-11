"""Final art pass (run this after the other generators): black player-shaped creature texture and all mod item icons."""
import os

from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TEX = os.path.join(ROOT, "src", "main", "resources", "assets", "arkandayim", "textures")

COLORS = {
    ".": (0, 0, 0, 0),
    "O": (22, 13, 8, 255),
    "k": (16, 13, 16, 255),
    "K": (46, 40, 46, 255),
    "w": (70, 38, 16, 255),
    "W": (112, 66, 30, 255),
    "h": (162, 106, 54, 255),
    "d": (94, 52, 22, 255),
    "m": (156, 100, 42, 255),
    "l": (212, 158, 90, 255),
    "H": (250, 226, 180, 255),
    "g": (228, 180, 86, 255),
    "G": (148, 102, 34, 255),
    "s": (176, 176, 186, 255),
    "S": (110, 110, 122, 255),
    "r": (178, 38, 28, 255),
    "R": (110, 18, 12, 255),
    "b": (54, 108, 168, 255),
    "B": (26, 54, 96, 255),
    "i": (150, 220, 255, 255),
    "c": (110, 200, 190, 255),
}


def from_map(rows, name):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if x < 16 and y < 16 and ch != ".":
                img.putpixel((x, y), COLORS[ch])
    img.save(os.path.join(TEX, "item", name if name.endswith(".png") else name + ".png"))


SWORD = [
    "................",
    "...........OO...",
    "..........OHlO..",
    ".........OHlmO..",
    "........OHlmO...",
    ".......OHlmO....",
    "......OHlmO.....",
    ".....OHlmO......",
    "....OHlmO.......",
    "...OOHmOO.......",
    "..OGgggGGO......",
    "...OOgOO........",
    "....OWwO........",
    "....OwWO........",
    "....OWO.........",
    ".....O..........",
]

PICKAXE = [
    "................",
    "...OOOOOOO......",
    "..OllllmmmOO....",
    ".OlllmmmOOmmO...",
    ".OllmmO..OmmO...",
    ".OlmmO....OmO...",
    "..OOO.....OWwO..",
    "..........OWwO..",
    ".........OWwO...",
    "........OWwO....",
    ".......OWwO.....",
    "......OWwO......",
    ".....OWwO.......",
    "....OWwO........",
    "...OwWO.........",
    "...OOO..........",
]

SHOVEL = [
    "................",
    ".........OOOO...",
    "........OllllO..",
    "........OllmmO..",
    "........OlmmO...",
    "........OmmmO...",
    "........OmmO....",
    ".......OWwO.....",
    "......OWwO......",
    ".....OWwO.......",
    "....OWwO........",
    "...OWwO.........",
    "..OWwO..........",
    "..OwO...........",
    "..OO............",
    "................",
]

AXE = [
    "................",
    "......OOOO......",
    ".....OllllO.....",
    "....OlllmmmO....",
    "...OllOOmmO.....",
    "...OlO..OmmO....",
    "...OmO..OWwO....",
    "..OmmmOOWwO.....",
    "..OmmmmWWwO.....",
    "...OmmmWwO......",
    "....OOOWwO......",
    "......OWwO......",
    ".....OWwO.......",
    "....OWwO........",
    "...OwWO.........",
    "....OO..........",
]

HOE = [
    "................",
    "....OOOOOO......",
    "...OlllllmO.....",
    "...OmmmmmOO.....",
    "...OOOOWwO......",
    "......OWwO......",
    ".....OWwO.......",
    ".....OWwO.......",
    "....OWwO........",
    "....OWwO........",
    "...OWwO.........",
    "...OWwO.........",
    "...OwWO.........",
    "...OOO..........",
    "................",
    "................",
]

HELMET = [
    "................",
    "....OOOOOO......",
    "...OllllllO.....",
    "..OllllllmmO....",
    "..OllOOOOmO.....",
    "..OlO...OmO.....",
    "..OmO...OmO.....",
    "..OmOOOOmO......",
    "..OmmmmmmO......",
    "...OOOOOO.......",
    "................",
    "................",
    "................",
    "................",
    "................",
    "................",
]

CHESTPLATE = [
    "................",
    ".OOO......OOO...",
    ".OllO....OllO...",
    ".OlllOOOllllO...",
    ".OlllmmllmmmO...",
    "..OllmmmmmllO...",
    "..OllmmOmmllO...",
    "..OllmmOmmllO...",
    "..OllmmmmmllO...",
    "...OmmmmmmmO....",
    "....OOOOOOO.....",
    "................",
    "................",
    "................",
    "................",
    "................",
]

LEGGINGS = [
    "................",
    "..OOOOOOOOOO....",
    "..OGGGGGGGGO....",
    "..OllOOOOllO....",
    "..OllO..OllO....",
    "..OllO..OllO....",
    "..OllO..OllO....",
    "..OllO..OllO....",
    "..OmmO..OmmO....",
    "..OmmO..OmmO....",
    "..OOOO..OOOO....",
    "................",
    "................",
    "................",
    "................",
    "................",
]

BOOTS = [
    "................",
    "................",
    "................",
    "................",
    "..OOOO..OOOO....",
    ".OlllO..OlllO...",
    ".OlllO..OlllO...",
    ".OmmmO..OmmmO...",
    ".OmmmO..OmmmO...",
    ".OmmmmOOmmmmO...",
    ".OOOOOOOOOOO....",
    "................",
    "................",
    "................",
    "................",
    "................",
]

BOOK = [
    "................",
    "..OOOOOOOOOO....",
    ".OkkkkkkkkkkO...",
    ".OkskkkkkkskO...",
    ".OkkkkkkkkkkO...",
    ".OkkkOOOOkkkO...",
    ".OkkOsrrrOkkO...",
    ".OkkOsrrkOkkO...",
    ".OkkkOOOOkkkO...",
    ".OkskkkkkkskO...",
    ".OkKkkkkkKkkO...",
    "..OOOOOOOOOO....",
    "................",
    "................",
    "................",
    "................",
]

INGOT = [
    "................",
    "................",
    "................",
    "................",
    "....OOOOOO......",
    "...OlllllHO.....",
    "..OllllHHmmO....",
    ".OlllHHmmmmO....",
    ".OlllmmmmmmO....",
    "..OOOOOOOOOO....",
    "................",
    "................",
    "................",
    "................",
    "................",
    "................",
]


def compass():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.ellipse((1, 1, 14, 14), fill=COLORS["G"], outline=COLORS["O"])
    d.ellipse((2, 2, 13, 13), fill=COLORS["g"], outline=COLORS["G"])
    d.ellipse((3, 3, 12, 12), fill=(238, 232, 210, 255), outline=COLORS["O"])
    d.ellipse((4, 4, 11, 11), fill=(206, 198, 174, 255))
    for x, y in ((8, 3), (12, 8), (8, 12), (3, 8)):
        img.putpixel((x, y), COLORS["S"])
    d.polygon([(8, 4), (10, 8), (8, 9)], fill=COLORS["r"])
    d.polygon([(8, 12), (6, 8), (8, 7)], fill=COLORS["K"])
    img.putpixel((8, 8), COLORS["O"])
    img.save(os.path.join(TEX, "item", "arkandayim_compass.png"))


def totem_figure(name, body, eye, accent):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle((5, 1, 10, 15), fill=body, outline=COLORS["O"])
    d.rectangle((3, 4, 4, 9), fill=body, outline=COLORS["O"])
    d.rectangle((11, 4, 12, 9), fill=body, outline=COLORS["O"])
    d.rectangle((6, 5, 7, 6), fill=eye)
    d.rectangle((8, 5, 9, 6), fill=eye)
    d.rectangle((6, 10, 9, 11), fill=accent)
    d.rectangle((6, 12, 9, 13), fill=(max(0, accent[0] - 40), max(0, accent[1] - 30), max(0, accent[2] - 20), 255))
    img.save(os.path.join(TEX, "item", name if name.endswith(".png") else name + ".png"))


def null_totem():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.ellipse((4, 0, 11, 6), fill=(24, 22, 26, 255), outline=COLORS["O"])
    d.rectangle((5, 1, 6, 2), fill=(250, 248, 240, 255))
    d.rectangle((8, 1, 9, 2), fill=(250, 248, 240, 255))
    d.rectangle((3, 6, 12, 12), fill=(18, 16, 20, 255), outline=COLORS["O"])
    d.rectangle((4, 7, 11, 8), fill=(150, 26, 18, 255))
    d.rectangle((2, 7, 3, 12), fill=(20, 18, 22, 255), outline=COLORS["O"])
    d.rectangle((12, 7, 13, 12), fill=(20, 18, 22, 255), outline=COLORS["O"])
    d.rectangle((5, 12, 6, 15), fill=(16, 14, 18, 255), outline=COLORS["O"])
    d.rectangle((9, 12, 10, 15), fill=(16, 14, 18, 255), outline=COLORS["O"])
    img.save(os.path.join(TEX, "item", "null_totem.png"))


def shadow_totem():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.ellipse((2, 1, 13, 15), fill=COLORS["B"], outline=COLORS["O"])
    d.ellipse((5, 3, 12, 13), fill=(0, 0, 0, 0))
    d.ellipse((6, 5, 9, 9), fill=COLORS["i"])
    d.ellipse((7, 6, 8, 8), fill=COLORS["k"])
    d.arc((2, 2, 13, 14), 120, 300, fill=COLORS["b"])
    img.save(os.path.join(TEX, "item", "shadow_totem.png"))


def guardian_totem():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.polygon([(8, 1), (14, 3), (13, 11), (8, 15), (3, 11), (2, 3)], fill=COLORS["g"], outline=COLORS["O"])
    d.polygon([(8, 3), (12, 4), (11, 10), (8, 12), (5, 10), (4, 4)], fill=COLORS["G"])
    d.ellipse((5, 5, 11, 10), fill=(250, 232, 170, 255), outline=COLORS["O"])
    d.ellipse((7, 6, 9, 9), fill=COLORS["R"])
    img.save(os.path.join(TEX, "item", "guardian_totem.png"))


def panel():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle((1, 1, 14, 14), fill=COLORS["K"], outline=COLORS["O"])
    d.rectangle((1, 1, 14, 2), fill=COLORS["m"])
    d.rectangle((1, 13, 14, 14), fill=COLORS["G"])
    d.rectangle((2, 3, 13, 12), fill=(12, 10, 14, 255))
    for y in (4, 6, 8, 10):
        d.line((3, y, 12, y), fill=(96, 44, 22, 255))
    d.point((4, 4), fill=COLORS["g"])
    d.point((11, 4), fill=COLORS["g"])
    d.point((4, 10), fill=COLORS["g"])
    d.point((11, 10), fill=COLORS["g"])
    d.ellipse((6, 5, 9, 9), fill=(150, 40, 18, 255))
    img.save(os.path.join(TEX, "item", "arkandayim_panel.png"))


# ------------------------------------------------------------------- creature


def creature_body():
    """Player shaped 64x64 skin, pitch black with faint ash cracks."""
    img = Image.new("RGBA", (64, 64), (9, 9, 11, 255))
    px = img.load()
    for y in range(64):
        for x in range(64):
            v = 9 + ((x * 7 + y * 13) % 5) - 2
            px[x, y] = (v, v, v + 2, 255)
    # head: face stays deepest black so the white eyes cut out of it
    for y in range(8, 16):
        for x in range(8, 16):
            px[x, y] = (6, 6, 8, 255)
    # faint cracks on the torso and limbs
    for cx, cy, steps in ((22, 22, 8), (26, 26, 6), (45, 24, 7), (5, 22, 6)):
        x, y = cx, cy
        for i in range(steps):
            if 0 <= x < 64 and 0 <= y < 64:
                px[x, y] = (24, 24, 27, 255)
            x += 1 if i % 2 == 0 else 0
            y += 1
    img.save(os.path.join(TEX, "entity", "aras.png"))


def creature_eyes():
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    px = img.load()
    for y in range(0, 3):
        for x in range(0, 7):
            px[x, y] = (255, 255, 255, 255)
    for y in range(0, 3):
        for x in range(8, 21):
            px[x, y] = (150, 24, 16, 255)
    img.save(os.path.join(TEX, "entity", "aras_eyes.png"))


def preview():
    names = ["brown_sword", "brown_pickaxe", "brown_shovel", "brown_axe", "brown_hoe",
             "brown_helmet", "brown_chestplate", "brown_leggings", "brown_boots",
             "arkandayim_compass", "null_totem", "shadow_totem", "guardian_totem",
             "arkandayim_panel", "black_book", "brown_ingot"]
    scale = 6
    sheet = Image.new("RGBA", (len(names) * 16 * scale // 2 + 20, 16 * scale * 2 + 20), (28, 30, 34, 255))
    for i, name in enumerate(names):
        art = Image.open(os.path.join(TEX, "item", name + ".png")).convert("RGBA")
        art = art.resize((16 * scale, 16 * scale), Image.NEAREST)
        col = i % 8
        row = i // 8
        sheet.alpha_composite(art, (col * 16 * scale, row * 16 * scale))
    sheet.save(os.path.join(ROOT, "art_preview2.png"))

    creature = Image.open(os.path.join(TEX, "entity", "aras.png")).convert("RGBA")
    creature = creature.resize((64 * 4, 64 * 4), Image.NEAREST)
    creature.save(os.path.join(ROOT, "art_preview_skin.png"))


def main():
    for rows, name in ((SWORD, "brown_sword"), (PICKAXE, "brown_pickaxe"), (SHOVEL, "brown_shovel"),
                       (AXE, "brown_axe"), (HOE, "brown_hoe"), (HELMET, "brown_helmet"),
                       (CHESTPLATE, "brown_chestplate"), (LEGGINGS, "brown_leggings"),
                       (BOOTS, "brown_boots"), (BOOK, "black_book"), (INGOT, "brown_ingot")):
        from_map(rows, name)
    compass()
    null_totem()
    shadow_totem()
    guardian_totem()
    panel()
    creature_body()
    creature_eyes()
    preview()
    print("art regenerated")


if __name__ == "__main__":
    main()
