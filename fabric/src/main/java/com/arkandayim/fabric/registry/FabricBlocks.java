package com.arkandayim.fabric.registry;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * The blocks of the mod. They reuse the forge build's models and textures, so every
 * blockstate json under assets/arkandayim is valid here as well.
 */
public final class FabricBlocks {

    public static final Block BROWN_ORE = block("brown_ore",
            AbstractBlock.Settings.copy(Blocks.IRON_ORE).strength(4.5F, 6.0F));
    public static final Block BLACK_DIRT = block("black_dirt",
            AbstractBlock.Settings.copy(Blocks.DIRT).strength(0.6F));
    public static final Block BLACK_STONE = block("black_stone",
            AbstractBlock.Settings.copy(Blocks.STONE).strength(1.8F, 8.0F).requiresTool());
    public static final Block DARK_STONE = block("dark_stone",
            AbstractBlock.Settings.copy(Blocks.DEEPSLATE).strength(2.4F, 9.0F).requiresTool());
    public static final Block ASH_BLOCK = block("ash_block",
            AbstractBlock.Settings.copy(Blocks.SAND).strength(0.5F));
    public static final Block BLACK_PLANKS = block("black_planks",
            AbstractBlock.Settings.copy(Blocks.OAK_PLANKS));
    public static final Block BLACK_LOG = block("black_log",
            AbstractBlock.Settings.copy(Blocks.OAK_LOG));
    public static final Block BLACK_LEAVES = block("black_leaves",
            AbstractBlock.Settings.copy(Blocks.OAK_LEAVES));

    private FabricBlocks() {
    }

    private static Block block(String name, AbstractBlock.Settings settings) {
        return Registry.register(Registries.BLOCK, Identifier.of("arkandayim", name), new Block(settings));
    }

    public static void init() {
    }
}
