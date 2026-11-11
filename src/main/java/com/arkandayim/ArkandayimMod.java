package com.arkandayim;

import com.arkandayim.entity.ModEntities;
import com.arkandayim.network.ModNetwork;
import com.arkandayim.registry.ModBlocks;
import com.arkandayim.registry.ModItems;
import com.arkandayim.registry.ModSounds;
import com.arkandayim.registry.ModTabs;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ArkandayimMod.MODID)
public class ArkandayimMod {

    public static final String MODID = "arkandayim";

    public ArkandayimMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModEntities.ENTITIES.register(modBus);
        ModSounds.SOUNDS.register(modBus);
        ModTabs.TABS.register(modBus);
        ModNetwork.register();
    }
}
