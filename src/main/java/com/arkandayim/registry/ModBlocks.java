package com.arkandayim.registry;

import com.arkandayim.ArkandayimMod;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, ArkandayimMod.MODID);

    public static final RegistryObject<Block> BROWN_ORE = BLOCKS.register("brown_ore",
            () -> new DropExperienceBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(4.5F, 6.0F)
                    .sound(SoundType.STONE), UniformInt.of(2, 5)));

    /** Black soil. A plain block on purpose, so grass can never spread onto it. */
    public static final RegistryObject<Block> BLACK_DIRT = BLOCKS.register("black_dirt",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(0.6F)
                    .sound(SoundType.GRAVEL)));

    public static final RegistryObject<Block> BLACK_STONE = BLOCKS.register("black_stone",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(1.6F, 6.0F)
                    .sound(SoundType.STONE)));

    public static final RegistryObject<Block> BLACK_PLANKS = BLOCKS.register("black_planks",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)));

    public static final RegistryObject<Block> BLACK_LOG = BLOCKS.register("black_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)));

    /** Burnt leaves: dark, but still leaves, so they fall when the trunk goes. */
    public static final RegistryObject<Block> BLACK_LEAVES = BLOCKS.register("black_leaves",
            () -> new LeavesBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(0.2F)
                    .randomTicks()
                    .noOcclusion()
                    .isValidSpawn((state, level, pos, type) -> false)
                    .isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false)
                    .sound(SoundType.GRASS)));

    /** The lighter tone of the dark ground, so the biome is not one flat black. */
    public static final RegistryObject<Block> DARK_STONE = BLOCKS.register("dark_stone",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GRAY)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(1.5F, 6.0F)
                    .sound(SoundType.STONE)));

    /** Pale ash patches that break up the black soil. */
    public static final RegistryObject<Block> ASH_BLOCK = BLOCKS.register("ash_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_GRAY)
                    .strength(0.5F)
                    .sound(SoundType.SAND)));
}
