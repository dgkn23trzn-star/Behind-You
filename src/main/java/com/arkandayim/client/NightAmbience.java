package com.arkandayim.client;

import com.arkandayim.ArkandayimMod;
import com.arkandayim.registry.ModSounds;
import com.arkandayim.util.DarkZone;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Night ambience: distant howls, cave wind, the occasional whisper from the trees
 * and, inside the dark biome, something breathing close by.
 */
@Mod.EventBusSubscriber(modid = ArkandayimMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class NightAmbience {

    private static final RandomSource RANDOM = RandomSource.create();
    private static int cooldown = 220;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.isPaused()) {
            return;
        }
        if (mc.screen != null) {
            return;
        }
        boolean night = NightDarkness.isNight(mc);
        boolean unlit = NightDarkness.isUnlit(mc);
        if (!night && !unlit) {
            cooldown = 260;
            return;
        }
        if (--cooldown > 0) {
            return;
        }
        cooldown = 260 + RANDOM.nextInt(520);

        boolean inBiome = DarkZone.isDark(mc.level, mc.player.blockPosition());
        SoundEvent sound;
        float volume;
        float pitch;
        if (inBiome) {
            int roll = RANDOM.nextInt(10);
            if (roll < 4) {
                sound = ModSounds.WHISPER.get();
                volume = 1.1F;
                pitch = 0.55F;
            } else if (roll < 7) {
                sound = ModSounds.STEP.get();
                volume = 1.0F;
                pitch = 0.5F;
            } else {
                sound = SoundEvents.AMBIENT_CAVE.value();
                volume = 0.5F;
                pitch = 0.6F;
            }
        } else if (unlit) {
            sound = SoundEvents.AMBIENT_CAVE.value();
            volume = 0.35F;
            pitch = 0.55F + RANDOM.nextFloat() * 0.3F;
        } else {
            int roll = RANDOM.nextInt(10);
            if (roll < 3) {
                sound = SoundEvents.WOLF_HOWL;
                volume = 0.45F;
                pitch = 0.75F;
            } else if (roll < 6) {
                sound = SoundEvents.AMBIENT_CAVE.value();
                volume = 0.28F;
                pitch = 0.5F;
            } else if (roll < 8) {
                sound = ModSounds.WHISPER.get();
                volume = 0.55F;
                pitch = 0.7F;
            } else {
                sound = SoundEvents.WARDEN_HEARTBEAT;
                volume = 0.25F;
                pitch = 0.8F;
            }
        }
        double angle = RANDOM.nextDouble() * Math.PI * 2.0D;
        double distance = 6.0D + RANDOM.nextDouble() * 14.0D;
        double x = mc.player.getX() + Math.cos(angle) * distance;
        double z = mc.player.getZ() + Math.sin(angle) * distance;
        double y = mc.player.getY() + (RANDOM.nextDouble() - 0.3D) * 4.0D;
        mc.level.playLocalSound(x, y, z, sound, SoundSource.AMBIENT, volume, pitch, false);
    }
}
