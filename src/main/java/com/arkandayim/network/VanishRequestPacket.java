package com.arkandayim.network;

import com.arkandayim.entity.ArasEntity;
import com.arkandayim.util.AngerManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class VanishRequestPacket {

    private final int entityId;
    private final boolean approach;

    public VanishRequestPacket(int entityId, boolean approach) {
        this.entityId = entityId;
        this.approach = approach;
    }

    public VanishRequestPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readVarInt();
        this.approach = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(this.entityId);
        buf.writeBoolean(this.approach);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            Entity entity = player.level().getEntity(this.entityId);
            if (!(entity instanceof ArasEntity aras)) {
                return;
            }
            if (this.approach) {
                // Only a completely full anger bar lets it come towards the player.
                aras.forceApproach(player);
                return;
            }
            // Staring it down does nothing in creative mode.
            if (player.isCreative() || player.isSpectator()) {
                return;
            }
            if (player.distanceTo(aras) <= 25.0D) {
                aras.startVanish(player, false);
            }
        });
        context.setPacketHandled(true);
    }
}
