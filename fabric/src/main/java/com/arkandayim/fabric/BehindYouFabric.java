package com.arkandayim.fabric;

import com.arkandayim.fabric.registry.FabricBlocks;
import com.arkandayim.fabric.registry.FabricGroups;
import com.arkandayim.fabric.registry.FabricItems;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point of the Fabric build. The technical id stays "arkandayim" so the same
 * assets, language keys and item ids as the Forge build are reused.
 */
public class BehindYouFabric implements ModInitializer {

    public static final String MOD_ID = "arkandayim";
    public static final Logger LOGGER = LoggerFactory.getLogger("Behind You");

    @Override
    public void onInitialize() {
        FabricBlocks.init();
        FabricItems.init();
        FabricGroups.init();
        LOGGER.info("Behind You (Fabric) yuklendi");
    }
}
