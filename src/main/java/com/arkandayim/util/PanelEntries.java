package com.arkandayim.util;

import com.arkandayim.registry.ModItems;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * The items the panel hands out, in the same order on both sides.
 */
public final class PanelEntries {

    private PanelEntries() {
    }

    public static List<ItemStack> stacks() {
        return List.of(
                new ItemStack(ModItems.ARAS_SPAWN_EGG.get()),
                new ItemStack(ModItems.BROWN_INGOT.get(), 8),
                new ItemStack(ModItems.BROWN_ORE.get(), 4),
                new ItemStack(ModItems.BROWN_SWORD.get()),
                new ItemStack(ModItems.BROWN_PICKAXE.get()),
                new ItemStack(ModItems.BROWN_SHOVEL.get()),
                new ItemStack(ModItems.BROWN_AXE.get()),
                new ItemStack(ModItems.BROWN_HOE.get()),
                new ItemStack(ModItems.BROWN_HELMET.get()),
                new ItemStack(ModItems.BROWN_CHESTPLATE.get()),
                new ItemStack(ModItems.BROWN_LEGGINGS.get()),
                new ItemStack(ModItems.BROWN_BOOTS.get()),
                new ItemStack(ModItems.ARKANDAYIM_COMPASS.get()),
                new ItemStack(ModItems.BLACK_DIRT.get(), 16),
                new ItemStack(ModItems.BLACK_STONE.get(), 16),
                new ItemStack(ModItems.DARK_STONE.get(), 16),
                new ItemStack(ModItems.ASH_BLOCK.get(), 16),
                new ItemStack(ModItems.BLACK_PLANKS.get(), 16),
                new ItemStack(ModItems.BLACK_LOG.get(), 16),
                new ItemStack(ModItems.BLACK_LEAVES.get(), 16),
                new ItemStack(ModItems.BLACK_BOOK.get()),
                new ItemStack(ModItems.NULL_TOTEM.get()),
                new ItemStack(ModItems.SHADOW_TOTEM.get()),
                new ItemStack(ModItems.GUARDIAN_TOTEM.get()),
                new ItemStack(ModItems.ARKANDAYIM_PANEL.get()));
    }
}
