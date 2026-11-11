package com.arkandayim.fabric.registry;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/** The "Behind You" creative tab, same layout as the forge one. */
public final class FabricGroups {

    public static final ItemGroup MAIN = Registry.register(
            Registries.ITEM_GROUP,
            Identifier.of("arkandayim", "main"),
            FabricItemGroup.builder()
                    .displayName(Text.translatable("itemGroup.arkandayim"))
                    .icon(() -> new ItemStack(FabricItems.BROWN_INGOT))
                    .entries((context, entries) -> {
                        entries.add(FabricItems.BROWN_ORE);
                        entries.add(FabricItems.BROWN_INGOT);
                        entries.add(FabricItems.BLACK_DIRT);
                        entries.add(FabricItems.BLACK_STONE);
                        entries.add(FabricItems.DARK_STONE);
                        entries.add(FabricItems.ASH_BLOCK);
                        entries.add(FabricItems.BLACK_PLANKS);
                        entries.add(FabricItems.BLACK_LOG);
                        entries.add(FabricItems.BLACK_LEAVES);
                    })
                    .build());

    private FabricGroups() {
    }

    public static void init() {
    }
}
