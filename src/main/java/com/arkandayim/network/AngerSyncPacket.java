package com.arkandayim.network;

import com.arkandayim.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class AngerSyncPacket {

    private final int anger;
    private final boolean shattered;

    public AngerSyncPacket(int anger, boolean shattered) {
        this.anger = anger;
        this.shattered = shattered;
    }

    public AngerSyncPacket(FriendlyByteBuf buf) {
        this.anger = buf.readVarInt();
        this.shattered = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(this.anger);
        buf.writeBoolean(this.shattered);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientPacketHandler.onAnger(this.anger, this.shattered)));
        ctx.get().setPacketHandled(true);
    }
}
