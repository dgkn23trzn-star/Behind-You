package com.arkandayim.network;

import com.arkandayim.util.PanelActions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Every button of the panel is validated on the server; the client only asks.
 */
public class PanelActionPacket {

    public static final byte GIVE_ITEM = 0;
    public static final byte START_HUNT = 1;
    public static final byte BUILD_HOUSE = 2;
    public static final byte DARK_BIOME = 3;
    public static final byte KNOCK_BOX = 4;
    public static final byte TOGGLE_TIME = 5;
    public static final byte TOGGLE_CHASE = 6;

    private final byte action;
    private final byte index;

    public PanelActionPacket(byte action) {
        this(action, (byte) 0);
    }

    public PanelActionPacket(byte action, byte index) {
        this.action = action;
        this.index = index;
    }

    public PanelActionPacket(FriendlyByteBuf buf) {
        this.action = buf.readByte();
        this.index = buf.readByte();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeByte(this.action);
        buf.writeByte(this.index);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                PanelActions.handle(player, this.action, this.index);
            }
        });
        context.setPacketHandled(true);
    }
}
