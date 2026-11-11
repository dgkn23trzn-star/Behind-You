package com.arkandayim.util;

import com.arkandayim.entity.ArasEntity;
import com.arkandayim.entity.ModEntities;
import com.arkandayim.network.AngerSyncPacket;
import com.arkandayim.network.ModNetwork;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AngerManager {

    public static final int MAX = 100;

    private static final Map<UUID, Integer> ANGER = new HashMap<>();
    private static final Map<UUID, Boolean> SHATTERED = new HashMap<>();

    public static int get(UUID id) {
        return ANGER.getOrDefault(id, 0);
    }

    public static boolean isShattered(UUID id) {
        return SHATTERED.getOrDefault(id, false);
    }

    public static void set(ServerPlayer player, int value) {
        UUID id = player.getUUID();
        int old = get(id);
        int clamped = Math.max(0, Math.min(MAX, value));
        ANGER.put(id, clamped);
        if (clamped >= MAX && old < MAX && !isShattered(id)) {
            SHATTERED.put(id, true);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.8F, 0.55F);
            startHunt(player);
        }
        sync(player);
    }

    /** A full bar sends it straight at the player, no staring and no waiting for night. */
    public static void startHunt(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        if (!ArkandayimData.get(level).isChaseEnabled()) {
            return;
        }
        ArasEntity aras = null;
        for (ArasEntity candidate : level.getEntitiesOfClass(ArasEntity.class,
                player.getBoundingBox().inflate(170.0D))) {
            if (candidate.getState() != ArasEntity.STATE_DYING) {
                aras = candidate;
                break;
            }
        }
        if (aras == null) {
            aras = ModEntities.ARAS.get().create(level);
            if (aras == null) {
                return;
            }
            aras.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), 0.0F);
            level.addFreshEntity(aras);
        }
        aras.beginHunt(player);
    }

    public static void add(ServerPlayer player, int amount) {
        set(player, get(player.getUUID()) + amount);
    }

    public static void reset(ServerPlayer player) {
        ANGER.put(player.getUUID(), 0);
        SHATTERED.put(player.getUUID(), false);
        sync(player);
    }

    public static void reset(UUID id) {
        ANGER.put(id, 0);
        SHATTERED.put(id, false);
    }

    public static void sync(ServerPlayer player) {
        ModNetwork.sendToPlayer(
                new AngerSyncPacket(get(player.getUUID()), isShattered(player.getUUID())), player);
    }
}
