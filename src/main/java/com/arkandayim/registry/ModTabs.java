package com.arkandayim.registry;

import com.arkandayim.ArkandayimMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ArkandayimMod.MODID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.arkandayim"))
                    .icon(() -> new ItemStack(ModItems.BROWN_INGOT.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.ARAS_SPAWN_EGG.get());
                        output.accept(ModItems.BROWN_ORE.get());
                        output.accept(ModItems.BROWN_INGOT.get());
                        output.accept(ModItems.BROWN_SWORD.get());
                        output.accept(ModItems.BROWN_PICKAXE.get());
                        output.accept(ModItems.BROWN_SHOVEL.get());
                        output.accept(ModItems.BROWN_AXE.get());
                        output.accept(ModItems.BROWN_HOE.get());
                        output.accept(ModItems.BROWN_HELMET.get());
                        output.accept(ModItems.BROWN_CHESTPLATE.get());
                        output.accept(ModItems.BROWN_LEGGINGS.get());
                        output.accept(ModItems.BROWN_BOOTS.get());
                        output.accept(ModItems.BLACK_DIRT.get());
                        output.accept(ModItems.BLACK_STONE.get());
                        output.accept(ModItems.DARK_STONE.get());
                        output.accept(ModItems.ASH_BLOCK.get());
                        output.accept(ModItems.BLACK_PLANKS.get());
                        output.accept(ModItems.BLACK_LOG.get());
                        output.accept(ModItems.BLACK_LEAVES.get());
                        output.accept(ModItems.ARKANDAYIM_COMPASS.get());
                        output.accept(ModItems.BLACK_BOOK.get());
                        output.accept(ModItems.NULL_TOTEM.get());
                        output.accept(ModItems.SHADOW_TOTEM.get());
                        output.accept(ModItems.GUARDIAN_TOTEM.get());
                        output.accept(ModItems.ARKANDAYIM_PANEL.get());
                    })
                    .build());
}
