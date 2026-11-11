package com.arkandayim.fabric.registry;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Items of the Fabric build. Tools, armour and totems come over in the next porting
 * step; the blocks, the ingot and the creative tab are already here.
 */
public final class FabricItems {

    public static final Item BROWN_INGOT = item("brown_ingot");

    public static final Item BROWN_ORE = blockItem("brown_ore", FabricBlocks.BROWN_ORE);
    public static final Item BLACK_DIRT = blockItem("black_dirt", FabricBlocks.BLACK_DIRT);
    public static final Item BLACK_STONE = blockItem("black_stone", FabricBlocks.BLACK_STONE);
    public static final Item DARK_STONE = blockItem("dark_stone", FabricBlocks.DARK_STONE);
    public static final Item ASH_BLOCK = blockItem("ash_block", FabricBlocks.ASH_BLOCK);
    public static final Item BLACK_PLANKS = blockItem("black_planks", FabricBlocks.BLACK_PLANKS);
    public static final Item BLACK_LOG = blockItem("black_log", FabricBlocks.BLACK_LOG);
    public static final Item BLACK_LEAVES = blockItem("black_leaves", FabricBlocks.BLACK_LEAVES);

    private FabricItems() {
    }

    private static Item item(String name) {
        return Registry.register(Registries.ITEM, Identifier.of("arkandayim", name),
                new Item(new Item.Settings()));
    }

    private static Item blockItem(String name, Block block) {
        return Registry.register(Registries.ITEM, Identifier.of("arkandayim", name),
                new BlockItem(block, new Item.Settings()));
    }

    public static void init() {
    }
}
