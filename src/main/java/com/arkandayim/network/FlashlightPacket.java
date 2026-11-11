package com.arkandayim.network;

import com.arkandayim.registry.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Sent when the player presses the flashlight key. The server owns the state so
 * the effect survives respawns and works the same on a dedicated server.
 */
public class FlashlightPacket {

    public static final String TAG = "arkandayim:flashlight";

    public FlashlightPacket() {
    }

    public FlashlightPacket(FriendlyByteBuf buf) {
    }

    public void encode(FriendlyByteBuf buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            CompoundTag data = player.getPersistentData();
            boolean on = !data.getBoolean(TAG);
            data.putBoolean(TAG, on);
            if (on) {
                player.addEffect(nightVision());
            } else {
                player.removeEffect(MobEffects.NIGHT_VISION);
            }
            player.playNotifySound(ModSounds.FLASHLIGHT.get(), SoundSource.PLAYERS, 1.0F, on ? 1.12F : 0.92F);
            player.displayClientMessage(Component.translatable(on ? "arkandayim.flashlight.on" : "arkandayim.flashlight.off"), true);
        });
        context.setPacketHandled(true);
    }

    public static MobEffectInstance nightVision() {
        return new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 60 * 20, 0, true, false, false);
    }
}
