package com.arkandayim.util;

import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * All advancements of the mod use the impossible trigger and are granted from code.
 */
public final class Advancements {

    public static final String NULL_DIED = "null_oldur";
    public static final String WATCHED = "izleniyorum";
    public static final String LEGENDARY_SWORD = "efsanevi_kilic";
    public static final String OLD_RESIDENTS = "eski_sakinler";

    private Advancements() {
    }

    public static void grant(ServerPlayer player, String path) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        Advancement advancement = server.getAdvancements()
                .getAdvancement(new ResourceLocation("arkandayim", path));
        if (advancement != null) {
            player.getAdvancements().award(advancement, "impossible");
        }
    }
}
