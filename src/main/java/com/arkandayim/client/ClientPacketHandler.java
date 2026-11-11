package com.arkandayim.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

public class ClientPacketHandler {

    public static void onAnger(int anger, boolean shattered) {
        float previous = ClientState.anger;
        ClientState.anger = Mth.clamp(anger, 0, 100);
        boolean wasShattered = ClientState.shattered;
        ClientState.shattered = shattered;
        if (shattered && !wasShattered) {
            ClientState.shatterAnim = ClientState.SHATTER_LEN;
            ClientState.flash = 0.85F;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.player.playSound(SoundEvents.GLASS_BREAK, 2.0F, 0.45F);
            }
        }
        if (ClientState.anger < previous) {
            ClientState.displayAnger = ClientState.anger;
        }
    }

    /** The creature is about to appear over there, so turn the head that way. */
    public static void onLook(double x, double y, double z, int ticks) {
        ClientState.gazeX = x;
        ClientState.gazeY = y;
        ClientState.gazeZ = z;
        ClientState.gazeTicks = Math.max(ClientState.gazeTicks, ticks);
    }

    public static void onFx(int type, double x, double y, double z) {
        Minecraft mc = Minecraft.getInstance();
        if (type != 0) {
            return;
        }
        ClientState.flash = 1.0F;
        ClientState.shakeTicks = 18;
        if (mc.player != null) {
            mc.player.playSound(SoundEvents.GENERIC_EXPLODE, 3.0F, 0.55F);
            mc.player.playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 2.5F, 0.8F);
        }
        if (mc.level != null) {
            for (int i = 0; i < 10; i++) {
                mc.level.addParticle(ParticleTypes.EXPLOSION_EMITTER,
                        x + (Math.random() - 0.5D) * 3.5D,
                        y + Math.random() * 2.0D,
                        z + (Math.random() - 0.5D) * 3.5D,
                        0.0D, 0.0D, 0.0D);
            }
        }
    }
}
