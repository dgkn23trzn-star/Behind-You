package com.arkandayim.network;

import com.arkandayim.ArkandayimMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetwork {

    private static final String VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(ArkandayimMod.MODID, "main"))
            .clientAcceptedVersions(VERSION::equals)
            .serverAcceptedVersions(VERSION::equals)
            .networkProtocolVersion(() -> VERSION)
            .simpleChannel();

    public static void register() {
        int id = 0;
        CHANNEL.messageBuilder(AngerSyncPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(AngerSyncPacket::encode)
                .decoder(AngerSyncPacket::new)
                .consumerMainThread(AngerSyncPacket::handle)
                .add();

        CHANNEL.messageBuilder(FxPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(FxPacket::encode)
                .decoder(FxPacket::new)
                .consumerMainThread(FxPacket::handle)
                .add();

        CHANNEL.messageBuilder(LookPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(LookPacket::encode)
                .decoder(LookPacket::new)
                .consumerMainThread(LookPacket::handle)
                .add();

        CHANNEL.messageBuilder(VanishRequestPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(VanishRequestPacket::encode)
                .decoder(VanishRequestPacket::new)
                .consumerMainThread(VanishRequestPacket::handle)
                .add();

        CHANNEL.messageBuilder(FlashlightPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(FlashlightPacket::encode)
                .decoder(FlashlightPacket::new)
                .consumerMainThread(FlashlightPacket::handle)
                .add();

        CHANNEL.messageBuilder(PanelActionPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(PanelActionPacket::encode)
                .decoder(PanelActionPacket::new)
                .consumerMainThread(PanelActionPacket::handle)
                .add();
    }

    public static void sendToPlayer(Object message, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static void sendToServer(Object message) {
        CHANNEL.sendToServer(message);
    }

    public static void sendFx(ServerLevel level, int type, double x, double y, double z) {
        for (ServerPlayer player : level.players()) {
            if (player.position().distanceTo(new Vec3(x, y, z)) < 160.0D) {
                sendToPlayer(new FxPacket(type, x, y, z), player);
            }
        }
    }
}
