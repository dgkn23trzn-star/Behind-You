package com.arkandayim.network;

import com.arkandayim.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Turns the player's head towards a point in the world for a few seconds, used
 * before the creature forms so the player is already looking at its spot.
 */
public class LookPacket {

    private final double x;
    private final double y;
    private final double z;
    private final int ticks;

    public LookPacket(double x, double y, double z, int ticks) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.ticks = ticks;
    }

    public LookPacket(FriendlyByteBuf buf) {
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.ticks = buf.readVarInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
        buf.writeVarInt(this.ticks);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientPacketHandler.onLook(this.x, this.y, this.z, this.ticks)));
        ctx.get().setPacketHandled(true);
    }
}
