package com.arkandayim.util;

import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Explosion helper that guarantees no damage is dealt to players or blocks.
 */
public class ArasFx {

    public static boolean NO_DAMAGE = false;

    public static void explodeNoDamage(Level level, double x, double y, double z, float power) {
        NO_DAMAGE = true;
        try {
            level.explode(null, x, y, z, power, Level.ExplosionInteraction.NONE);
        } finally {
            NO_DAMAGE = false;
        }
    }

    @SubscribeEvent
    public static void onDetonate(ExplosionEvent.Detonate event) {
        if (NO_DAMAGE) {
            event.getAffectedEntities().clear();
            event.getAffectedBlocks().clear();
        }
    }
}
