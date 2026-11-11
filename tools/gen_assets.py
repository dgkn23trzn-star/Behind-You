import math
import os
import wave

import numpy as np
from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", "arkandayim")
TEX = os.path.join(ASSETS, "textures")
SOUNDS = os.path.join(ASSETS, "sounds")

RNG = np.random.default_rng(20261109)


def ensure(*parts):
    path = os.path.join(*parts)
    os.makedirs(path, exist_ok=True)
    return path


def mix(c1, c2, t):
    return tuple(int(c1[i] + (c2[i] - c1[i]) * t) for i in range(3)) + (255,)


# ---------------------------------------------------------------- block / item

def brown_ore():
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            base = 118 + RNG.integers(-14, 15)
            px[x, y] = (int(base), int(base - 2), int(base - 4), 255)
    for _ in range(26):
        x, y = RNG.integers(0, 16), RNG.integers(0, 16)
        shade = 150 + RNG.integers(-18, 18)
        px[x, y] = (int(shade), int(shade - 3), int(shade - 6), 255)
    for _ in range(9):
        cx, cy = RNG.integers(1, 15), RNG.integers(1, 15)
        radius = RNG.integers(1, 3)
        for dy in range(-radius, radius + 1):
            for dx in range(-radius, radius + 1):
                if dx * dx + dy * dy > radius * radius + 0.4:
                    continue
                x, y = cx + dx, cy + dy
                if not (0 <= x < 16 and 0 <= y < 16):
                    continue
                t = RNG.random()
                color = (214, 120, 26) if t > 0.45 else (168, 84, 14)
                if t > 0.9:
                    color = (246, 158, 52)
                px[x, y] = color + (255,)
    for _ in range(6):
        x, y = RNG.integers(0, 16), RNG.integers(0, 16)
        px[x, y] = (74, 42, 22, 255)
    img.save(os.path.join(TEX, "block", "brown_ore.png"))


def brown_ingot():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    cx, cy = 7.5, 7.5
    for y in range(16):
        for x in range(16):
            dx = (x + 0.5 - cx) / 7.4
            dy = (y + 0.5 - cy) / 6.6
            d = math.sqrt(dx * dx + dy * dy)
            if d > 1.0:
                continue
            if d > 0.90:
                px[x, y] = (52, 26, 11, 255)
                continue
            if d > 0.80:
                px[x, y] = (92, 46, 18, 255)
                continue
            t = (1.0 - d) * 0.85 + (1.0 - dy) * 0.25
            t = max(0.0, min(1.0, t))
            color = mix((150, 78, 20), (238, 158, 56), t)
            if t > 0.72:
                color = mix(color[:3], (252, 196, 118), (t - 0.72) * 2.2)
            px[x, y] = color
    for _ in range(9):
        x = int(cx + RNG.normal(0, 2.4))
        y = int(cy + RNG.normal(0, 2.0))
        if 0 <= x < 16 and 0 <= y < 16 and px[x, y][3] > 0:
            px[x, y] = (116, 58, 16, 255)
    img.save(os.path.join(TEX, "item", "brown_ingot.png"))


def brown_sword():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()

    def put(x, y, color):
        if 0 <= x < 16 and 0 <= y < 16:
            px[x, y] = color

    outline = (38, 18, 7, 255)
    grip_dark = (48, 24, 12, 255)
    grip_light = (86, 46, 22, 255)

    # a single thick diagonal band: grip at the lower left, glowing blade to the upper right
    for t in range(0, 12):
        x = 3 + t
        y = 12 - t
        if t < 3:
            put(x, y, grip_light)
            put(x + 1, y - 1, grip_light)
            put(x + 1, y, grip_dark)
            put(x, y - 1, outline)
        elif t == 3:
            # crossguard, perpendicular to the blade
            for k in range(-2, 3):
                put(x + k, y + k, outline)
                put(x + k, y + k + 1, grip_light if abs(k) < 2 else outline)
            put(x, y, (255, 96, 20, 255))
            put(x + 1, y - 1, (255, 150, 48, 255))
        else:
            heat = (t - 4) / 7.0
            core = mix((216, 112, 20), (255, 236, 178), heat)
            put(x, y, core)
            put(x + 1, y - 1, mix(core[:3], (255, 252, 238), 0.45 + 0.45 * heat))
            put(x + 1, y, outline)
            put(x, y - 1, outline)

    put(2, 13, outline)
    put(1, 14, outline)
    put(2, 14, grip_light)
    put(14, 0, (255, 252, 240, 255))
    img.save(os.path.join(TEX, "item", "brown_sword.png"))


# ------------------------------------------------------------ black biome blocks

def _noise_block(name, base, spread, blotch=None, blotch_count=0, palette=None):
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            v = base + int(RNG.integers(-spread, spread + 1))
            color = palette(v) if palette else (v, v, v, 255)
            px[x, y] = color
    if blotch is not None:
        for _ in range(blotch_count):
            cx, cy = RNG.integers(0, 16), RNG.integers(0, 16)
            for dy in (-1, 0, 1):
                for dx in (-1, 0, 1):
                    x, y = cx + dx, cy + dy
                    if 0 <= x < 16 and 0 <= y < 16 and abs(dx) + abs(dy) < 2:
                        px[x, y] = blotch + (255,)
    img.save(os.path.join(TEX, "block", name))


def black_dirt():
    _noise_block("black_dirt.png", 24, 7,
                 blotch=(14, 12, 13), blotch_count=22,
                 palette=lambda v: (max(0, v + 2), max(0, v), max(0, v + 1), 255))


def black_stone():
    _noise_block("black_stone.png", 20, 6,
                 blotch=(33, 32, 35), blotch_count=16,
                 palette=lambda v: (max(0, v + 1), max(0, v + 1), max(0, v + 3), 255))


def black_planks():
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            v = 26 + int(RNG.integers(-4, 5))
            px[x, y] = (max(0, v + 2), max(0, v), max(0, v + 2), 255)
    for y in (3, 7, 11, 15):
        for x in range(16):
            px[x, y] = (13, 11, 13, 255)
    for i, x in enumerate((5, 11)):
        px[x, 2] = (12, 10, 12, 255)
        px[x + 2, 6] = (12, 10, 12, 255)
        px[x + 4, 10] = (12, 10, 12, 255)
    img.save(os.path.join(TEX, "block", "black_planks.png"))


def black_log(side=True):
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            v = 18 + int(RNG.integers(-5, 6))
            px[x, y] = (max(0, v + 3), max(0, v), max(0, v + 2), 255)
    if side:
        for x in (2, 6, 10, 14):
            for y in range(16):
                if RNG.random() > 0.18:
                    px[x, y] = (9, 7, 9, 255)
    else:
        cx = cy = 7.5
        for y in range(16):
            for x in range(16):
                d = math.hypot(x - cx, y - cy)
                if d > 6.6:
                    continue
                ring = int(d) % 2
                px[x, y] = (24, 18, 20, 255) if ring else (12, 9, 11, 255)
    img.save(os.path.join(TEX, "block", "black_log.png" if side else "black_log_top.png"))


# --------------------------------------------------------------------- items

def black_book():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    for y in range(2, 15):
        for x in range(2, 14):
            px[x, y] = (16, 14, 18, 255)
    for y in range(2, 15):
        px[2, y] = (34, 30, 38, 255)
        px[13, y] = (8, 7, 9, 255)
    for x in range(2, 14):
        px[x, 2] = (34, 30, 38, 255)
        px[x, 14] = (8, 7, 9, 255)
    # a single pale sigil on the cover
    for x, y in ((7, 6), (8, 6), (7, 7), (8, 7), (7, 9), (8, 9), (6, 8), (9, 8)):
        px[x, y] = (226, 220, 210, 255)
    px[7, 8] = (198, 40, 24, 255)
    px[8, 8] = (198, 40, 24, 255)
    img.save(os.path.join(TEX, "item", "black_book.png"))


def null_totem():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    body = [(7, 1), (8, 1)]
    for y in range(2, 15):
        half = 4 if y < 8 else 3
        for x in range(8 - half, 8 + half):
            shade = 30 + int(RNG.integers(-6, 7))
            px[x, y] = (shade + 2, shade, shade + 3, 255)
    for x, y in body:
        px[x, y] = (12, 10, 13, 255)
    # pale eye slit
    for x in range(5, 11):
        px[x, 5] = (236, 232, 220, 255)
    px[7, 5] = (255, 255, 255, 255)
    px[8, 5] = (255, 255, 255, 255)
    for x in range(6, 10):
        px[x, 10] = (150, 28, 18, 255)
    for x in range(6, 10):
        px[x, 11] = (92, 14, 10, 255)
    img.save(os.path.join(TEX, "item", "null_totem.png"))


# ------------------------------------------------------------------- creature

def aras_body():
    img = Image.new("RGBA", (64, 64))
    px = img.load()
    for y in range(64):
        for x in range(64):
            v = 9 + RNG.integers(-3, 5)
            px[x, y] = (max(0, v + 2), max(0, v), max(0, v + 1), 255)
    for _ in range(120):
        x, y = RNG.integers(0, 64), RNG.integers(0, 64)
        length = RNG.integers(2, 9)
        for i in range(length):
            xx, yy = x + i, y + (i // 4)
            if 0 <= xx < 64 and 0 <= yy < 64:
                px[xx, yy] = (22, 12, 14, 255)
    for _ in range(40):
        x, y = RNG.integers(0, 64), RNG.integers(0, 64)
        px[x, y] = (34, 16, 18, 255)
    img.save(os.path.join(TEX, "entity", "aras.png"))


def aras_eyes():
    # The eye cubes map the top left corner of this texture, so the hard white dot
    # must sit exactly at (0,0). The grey halo only adds a soft rim of glow.
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    for ox in (0, 8):
        draw.rectangle((ox, 0, ox + 7, 7), fill=(92, 90, 96, 255))
        draw.rectangle((ox + 1, 1, ox + 6, 6), fill=(206, 204, 208, 255))
        draw.rectangle((ox, 0, ox + 3, 3), fill=(255, 255, 255, 255))
    img.save(os.path.join(TEX, "entity", "aras_eyes.png"))


def eye_dot():
    # A single glowing pinprick pair, projected onto the screen when the world is black.
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    for cx in (10, 22):
        draw.ellipse((cx - 5, 11, cx + 5, 21), fill=(120, 118, 124, 160))
        draw.ellipse((cx - 3, 13, cx + 3, 19), fill=(255, 255, 255, 255))
        draw.ellipse((cx - 1, 15, cx + 1, 18), fill=(255, 255, 255, 255))
    img.save(os.path.join(TEX, "entity", "eye_dot.png"))


def flashlight_mask():
    # Screen overlay used to punch a lit cone out of the darkness. The centre is
    # fully transparent, the rim is solid black, and the edge falls off softly.
    size = 256
    img = Image.new("RGBA", (size, size), (0, 0, 0, 255))
    px = img.load()
    centre = (size - 1) / 2.0
    inner = 34.0
    outer = 118.0
    for y in range(size):
        for x in range(size):
            d = ((x - centre) ** 2 + (y - centre) ** 2) ** 0.5
            if d <= inner:
                a = 0
            elif d >= outer:
                a = 255
            else:
                t = (d - inner) / (outer - inner)
                a = int(255 * (t ** 1.5))
            dot = ((x - centre) ** 2 + (y - centre) ** 2) ** 0.5
            warm = (26, 22, 14) if dot < inner else (0, 0, 0)
            px[x, y] = (warm[0], warm[1], warm[2], a)
    img.save(os.path.join(TEX, "gui", "flashlight_mask.png"))


def brown_tools():
    outline = (38, 18, 7, 255)
    grip_dark = (48, 24, 12, 255)
    grip_light = (92, 50, 24, 255)
    head_dark = (96, 52, 18, 255)
    head_mid = (176, 104, 34, 255)
    head_light = (240, 196, 120, 255)

    def handle(px, put, length=9):
        for t in range(length):
            x = 3 + t
            y = 13 - t
            put(x, y, grip_light)
            put(x + 1, y, grip_dark)
            put(x, y - 1, outline)
            put(x + 1, y - 1, outline)

    def blank():
        img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
        return img, img.load()

    # pickaxe: a wide curved head across the top right
    img, px = blank()
    put = lambda x, y, c: px.__setitem__((x, y), c) if 0 <= x < 16 and 0 <= y < 16 else None
    handle(px, put, 10)
    for i, (x, y) in enumerate([(8, 5), (9, 4), (10, 3), (11, 2), (12, 1), (13, 1), (14, 2)]):
        put(x, y - 1, outline)
        put(x, y, head_mid)
        put(x + 1, y, head_mid)
        put(x, y + 1, head_dark)
    for x, y in [(9, 3), (11, 2), (13, 2), (14, 3)]:
        put(x, y, head_light)
    put(8, 4, head_mid)
    put(8, 6, outline)
    put(9, 5, outline)
    img.save(os.path.join(TEX, "item", "brown_pickaxe.png"))

    # shovel: a spade blade at the tip
    img, px = blank()
    put = lambda x, y, c: px.__setitem__((x, y), c) if 0 <= x < 16 and 0 <= y < 16 else None
    handle(px, put, 9)
    for x, y in [(11, 2), (12, 1), (13, 2), (12, 3)]:
        put(x, y, head_mid)
        put(x + 1, y, head_light if (x + y) % 2 else head_mid)
    put(11, 1, outline)
    put(13, 1, outline)
    put(11, 3, outline)
    put(13, 3, outline)
    put(12, 4, outline)
    put(12, 2, head_light)
    img.save(os.path.join(TEX, "item", "brown_shovel.png"))

    # axe: a chunky blade on the upper right
    img, px = blank()
    put = lambda x, y, c: px.__setitem__((x, y), c) if 0 <= x < 16 and 0 <= y < 16 else None
    handle(px, put, 10)
    for y in range(1, 7):
        for x in range(10, 14):
            put(x, y, head_mid if (x + y) % 3 else head_light)
    for x in range(10, 14):
        put(x, 0, outline)
        put(x, 7, outline)
    for y in range(1, 7):
        put(9, y, outline)
        put(14, y, outline)
    put(11, 2, head_light)
    put(12, 3, head_light)
    img.save(os.path.join(TEX, "item", "brown_axe.png"))

    # hoe: a small L shaped blade
    img, px = blank()
    put = lambda x, y, c: px.__setitem__((x, y), c) if 0 <= x < 16 and 0 <= y < 16 else None
    handle(px, put, 10)
    for x in range(9, 14):
        put(x, 2, head_mid)
        put(x, 3, head_dark)
    for x, y in [(9, 2), (13, 2), (9, 3), (13, 3), (10, 1), (12, 1), (11, 1)]:
        put(x, y, outline)
    put(10, 2, head_light)
    put(12, 2, head_light)
    put(9, 4, outline)
    put(10, 4, head_mid)
    img.save(os.path.join(TEX, "item", "brown_hoe.png"))


def panel_item():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.rectangle((2, 1, 13, 14), fill=(16, 14, 18, 255))
    draw.rectangle((2, 1, 13, 2), fill=(120, 66, 26, 255))
    draw.rectangle((2, 13, 13, 14), fill=(120, 66, 26, 255))
    draw.rectangle((2, 4, 3, 11), fill=(84, 46, 20, 255))
    draw.rectangle((12, 4, 13, 11), fill=(84, 46, 20, 255))
    for x in (5, 9):
        draw.rectangle((x, 6, x + 1, 8), fill=(255, 255, 255, 255))
    draw.rectangle((6, 10, 9, 10), fill=(180, 60, 30, 255))
    img.save(os.path.join(TEX, "item", "arkandayim_panel.png"))


def distant_eyes():
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    for offset_x in (3, 18):
        draw.ellipse((offset_x, 11, offset_x + 10, 21), fill=(255, 252, 240, 255))
        draw.ellipse((offset_x + 2, 13, offset_x + 8, 19), fill=(255, 214, 160, 255))
        draw.ellipse((offset_x + 4, 15, offset_x + 6, 18), fill=(255, 255, 255, 255))
    img.save(os.path.join(TEX, "entity", "distant_eyes.png"))


# --------------------------------------------------------------------- sounds

SR = 44100


def bandpass(signal, low, high):
    spectrum = np.fft.rfft(signal)
    freqs = np.fft.rfftfreq(len(signal), 1.0 / SR)
    mask = np.exp(-((freqs - high) ** 2) / (2.0 * (high * 0.55) ** 2))
    mask += 0.6 * np.exp(-((freqs - low) ** 2) / (2.0 * (low * 0.5) ** 2))
    return np.fft.irfft(spectrum * mask, len(signal))


def whisper():
    duration = 2.9
    n = int(duration * SR)
    noise = bandpass(RNG.normal(0.0, 1.0, n), 620.0, 2300.0)
    envelope = np.zeros(n)
    position = 0
    while position < n:
        length = int(RNG.uniform(0.11, 0.33) * SR)
        length = min(length, n - position)
        if length <= 4:
            break
        envelope[position:position + length] = np.maximum(
            envelope[position:position + length], np.hanning(length))
        position += int(length * 1.2)
    breath = bandpass(RNG.normal(0.0, 1.0, n), 300.0, 5200.0) * 0.35
    signal = (noise + breath) * envelope
    signal *= 1.0 / max(1e-6, np.max(np.abs(signal)))
    signal *= 0.85
    fade = int(0.05 * SR)
    signal[:fade] *= np.linspace(0.0, 1.0, fade)
    signal[-fade:] *= np.linspace(1.0, 0.0, fade)
    return signal


def thump(freq, duration, amp, decay):
    n = int(duration * SR)
    t = np.arange(n) / SR
    body = np.sin(2.0 * np.pi * freq * t) * np.exp(-t * decay)
    click = RNG.normal(0.0, 1.0, n) * np.exp(-t * 120.0) * 0.6
    knock = np.sin(2.0 * np.pi * freq * 2.4 * t) * np.exp(-t * decay * 2.2) * 0.4
    return (body + click + knock) * amp


def knock():
    total = np.zeros(int(1.7 * SR))
    for i, offset in enumerate((0.0, 0.24, 0.47)):
        hit = thump(64.0, 0.42, 1.0 - i * 0.11, 15.0)
        start = int(offset * SR)
        total[start:start + len(hit)] += hit
    return total


def step():
    total = np.zeros(int(0.65 * SR))
    hit = thump(88.0, 0.38, 1.0, 20.0)
    gravel = bandpass(RNG.normal(0.0, 1.0, len(hit)), 900.0, 4200.0) * np.exp(
        -np.arange(len(hit)) / SR * 26.0) * 0.35
    total[:len(hit)] += hit + gravel
    return total


def flashlight():
    total = np.zeros(int(0.34 * SR))
    for offset, amp, freq in ((0.0, 1.0, 2600.0), (0.058, 0.55, 3400.0)):
        n = int(0.10 * SR)
        t = np.arange(n) / SR
        noise = RNG.normal(0.0, 1.0, n) * np.exp(-t * 120.0)
        noise = bandpass(noise, freq, freq * 2.2)
        clack = np.sin(2.0 * np.pi * 640.0 * t) * np.exp(-t * 95.0) * 0.35
        hit = (noise * 0.9 + clack) * amp
        start = int(offset * SR)
        total[start:start + n] += hit
    return total


def heartbeat():
    total = np.zeros(int(1.35 * SR))
    first = thump(48.0, 0.36, 1.0, 13.0)
    second = thump(42.0, 0.30, 0.72, 15.0)
    total[:len(first)] += first
    start = int(0.30 * SR)
    total[start:start + len(second)] += second
    return total


def write_ogg(name, signal):
    peak = np.max(np.abs(signal))
    if peak > 1e-6:
        signal = signal / peak * 0.88
    stereo = np.stack([signal, signal], axis=1)
    import soundfile as sf
    sf.write(os.path.join(SOUNDS, name), stereo, SR, format="OGG", subtype="VORBIS")


def main():
    ensure(TEX, "block")
    ensure(TEX, "item")
    ensure(TEX, "entity")
    ensure(TEX, "gui")
    ensure(SOUNDS)
    brown_ore()
    brown_ingot()
    brown_sword()
    black_dirt()
    black_stone()
    black_planks()
    black_log(True)
    black_log(False)
    black_book()
    null_totem()
    aras_body()
    aras_eyes()
    distant_eyes()
    eye_dot()
    flashlight_mask()
    brown_tools()
    panel_item()
    write_ogg("whisper.ogg", whisper())
    write_ogg("knock.ogg", knock())
    write_ogg("step.ogg", step())
    write_ogg("heartbeat.ogg", heartbeat())
    write_ogg("flashlight.ogg", flashlight())
    print("assets generated")


if __name__ == "__main__":
    main()
