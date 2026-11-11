"""Writes the JSON resources for the new blocks, items and the dark biome."""
import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src", "main", "resources")
ASSETS = os.path.join(RES, "assets", "arkandayim")
DATA = os.path.join(RES, "data")


def write(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as handle:
        json.dump(obj, handle, indent=2)
        handle.write("\n")


def simple_block(name):
    write(os.path.join(ASSETS, "blockstates", name + ".json"),
          {"variants": {"": {"model": "arkandayim:block/" + name}}})
    write(os.path.join(ASSETS, "models", "block", name + ".json"),
          {"parent": "minecraft:block/cube_all",
           "textures": {"all": "arkandayim:block/" + name}})
    write(os.path.join(ASSETS, "models", "item", name + ".json"),
          {"parent": "arkandayim:block/" + name})


LOOT_SELF = {
    "type": "minecraft:block",
    "pools": [{
        "rolls": 1.0,
        "bonus_rolls": 0.0,
        "entries": [{"type": "minecraft:item", "name": None}],
        "conditions": [{"condition": "minecraft:survives_explosion"}]
    }]
}


def loot(name, item=None, count=None):
    table = json.loads(json.dumps(LOOT_SELF))
    table["pools"][0]["entries"][0]["name"] = "arkandayim:" + (item or name)
    if count:
        table["pools"][0]["entries"][0]["functions"] = [
            {"function": "minecraft:set_count",
             "count": {"type": "minecraft:uniform", "min": count[0], "max": count[1]}},
            {"function": "minecraft:apply_bonus", "enchantment": "minecraft:fortune",
             "formula": "minecraft:ore_drops"}
        ]
    write(os.path.join(DATA, "arkandayim", "loot_tables", "blocks", name + ".json"), table)


def tag(name, folder, values):
    path = os.path.join(DATA, "minecraft", "tags", folder, name + ".json")
    existing = []
    if os.path.exists(path):
        with open(path, encoding="utf-8") as handle:
            existing = json.load(handle).get("values", [])
    merged = sorted(set(existing) | set(values))
    write(path, {"replace": False, "values": merged})


def item_model(name, parent="minecraft:item/generated"):
    write(os.path.join(ASSETS, "models", "item", name + ".json"),
          {"parent": parent, "textures": {"layer0": "arkandayim:item/" + name}})


def main():
    for name in ("black_leaves", "dark_stone", "ash_block"):
        simple_block(name)

    for name in ("black_dirt", "black_stone", "black_planks", "black_log",
                 "black_leaves", "dark_stone", "ash_block"):
        loot(name)

    for name in ("brown_helmet", "brown_chestplate", "brown_leggings", "brown_boots",
                 "arkandayim_compass", "shadow_totem", "guardian_totem"):
        item_model(name)

    tag("pickaxe", "blocks/mineable",
        ["arkandayim:brown_ore", "arkandayim:black_stone", "arkandayim:dark_stone"])
    tag("shovel", "blocks/mineable",
        ["arkandayim:black_dirt", "arkandayim:ash_block"])
    tag("axe", "blocks/mineable",
        ["arkandayim:black_planks", "arkandayim:black_log"])
    tag("hoe", "blocks/mineable", ["arkandayim:black_leaves"])
    tag("leaves", "blocks", ["arkandayim:black_leaves"])
    tag("dirt", "blocks", ["arkandayim:black_dirt"])

    biome = {
        "temperature": 0.15,
        "downfall": 0.0,
        "has_precipitation": False,
        "effects": {
            "sky_color": 921102,
            "fog_color": 1184274,
            "water_color": 1968655,
            "water_fog_color": 328965,
            "grass_color": 1644825,
            "foliage_color": 1118481,
            "mood_sound": {
                "sound": "arkandayim:whisper",
                "tick_delay": 1600,
                "block_search_extent": 8,
                "offset": 2.0
            }
        },
        "spawners": {},
        "spawn_costs": {},
        "carvers": {},
        "features": [[] for _ in range(11)]
    }
    write(os.path.join(DATA, "arkandayim", "worldgen", "biome", "black_forest.json"), biome)
    print("json resources generated")


if __name__ == "__main__":
    main()
