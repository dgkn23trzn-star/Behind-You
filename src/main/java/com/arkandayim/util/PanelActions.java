package com.arkandayim.util;

import com.arkandayim.network.PanelActionPacket;
import com.arkandayim.registry.ModBlocks;
import com.arkandayim.registry.ModItems;
import com.arkandayim.registry.ModSounds;
import com.arkandayim.item.BlackBookItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.List;

/**
 * Server side of the panel buttons. Everything is checked here, never trusted
 * from the client.
 */
public final class PanelActions {

    private PanelActions() {
    }

    public static void handle(ServerPlayer player, byte action, byte index) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        ArkandayimData data = ArkandayimData.get(level);

        switch (action) {
            case PanelActionPacket.GIVE_ITEM -> {
                if (!player.isCreative()) {
                    message(player, "arkandayim.panel.creative_only");
                    return;
                }
                List<ItemStack> stacks = PanelEntries.stacks();
                if (index < 0 || index >= stacks.size()) {
                    return;
                }
                ItemStack stack = stacks.get(index).copy();
                if (!player.getInventory().add(stack)) {
                    player.drop(stack, false);
                }
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        net.minecraft.sounds.SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.6F, 1.4F);
            }
            case PanelActionPacket.START_HUNT -> {
                if (!data.isChaseEnabled()) {
                    message(player, "arkandayim.panel.chase_off");
                    return;
                }
                AngerManager.startHunt(player);
                message(player, "arkandayim.panel.hunt_started");
            }
            case PanelActionPacket.BUILD_HOUSE -> {
                BlockPos pos = LuxuryHouse.buildAt(level, player.blockPosition().offset(7, 0, 0));
                giveCoords(player, pos);
                message(player, "arkandayim.panel.house_built");
            }
            case PanelActionPacket.DARK_BIOME -> {
                if (!teleportToDarkZone(player, level)) {
                    message(player, "arkandayim.panel.no_dark_zone");
                }
            }
            case PanelActionPacket.KNOCK_BOX -> {
                knockAndLeaveBox(player, level, data);
            }
            case PanelActionPacket.TOGGLE_TIME -> {
                long time = level.getDayTime() % 24000L;
                boolean night = time >= 13000L && time < 23000L;
                long target = night ? 1000L : 14500L;
                long delta = ((target - time) + 24000L) % 24000L;
                level.setDayTime(level.getDayTime() + delta);
                message(player, night ? "arkandayim.panel.day" : "arkandayim.panel.night");
            }
            case PanelActionPacket.TOGGLE_CHASE -> {
                data.setChaseEnabled(!data.isChaseEnabled());
                message(player, data.isChaseEnabled()
                        ? "arkandayim.panel.chase_on" : "arkandayim.panel.chase_off");
            }
            default -> {
            }
        }
    }

    private static void giveCoords(ServerPlayer player, BlockPos pos) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(ModItems.BLACK_BOOK.get())) {
                BlackBookItem.writeHouseCoords(stack, pos.getX(), pos.getY(), pos.getZ());
                return;
            }
        }
        ItemStack book = new ItemStack(ModItems.BLACK_BOOK.get());
        BlackBookItem.writeHouseCoords(book, pos.getX(), pos.getY(), pos.getZ());
        if (!player.getInventory().add(book)) {
            player.drop(book, false);
        }
    }

    private static boolean teleportToDarkZone(ServerPlayer player, ServerLevel level) {
        BlockPos centre = DarkZone.nearestDarkCentre(level, player.getBlockX(), player.getBlockZ(), 9);
        if (centre == null) {
            return false;
        }
        // Load the area first, so the black ground is already there when the player lands.
        int chunkX = centre.getX() >> 4;
        int chunkZ = centre.getZ() >> 4;
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                level.getChunk(chunkX + dx, chunkZ + dz);
            }
        }
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, centre.getX(), centre.getZ());
        player.teleportTo(centre.getX() + 0.5D, y + 1.0D, centre.getZ() + 0.5D);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.7F);
        message(player, "arkandayim.panel.dark_arrived");
        return true;
    }

    private static void knockAndLeaveBox(ServerPlayer player, ServerLevel level, ArkandayimData data) {
        BlockPos house = data.getHousePos();
        BlockPos door = house == null ? player.blockPosition() : house.offset(0, 0, -5);
        level.playSound(null, door.getX() + 0.5D, door.getY() + 1.0D, door.getZ() + 0.5D,
                ModSounds.KNOCK.get(), SoundSource.NEUTRAL, 3.0F, 1.0F);

        BlockPos box = door.offset(0, 0, -1);
        while (box.getY() > level.getMinBuildHeight() && level.getBlockState(box).isAir()) {
            box = box.below();
        }
        box = box.above();
        level.setBlock(box, Blocks.CHEST.defaultBlockState()
                .setValue(ChestBlock.FACING, Direction.SOUTH), 3);
        if (level.getBlockEntity(box) instanceof ChestBlockEntity chest) {
            chest.setItem(0, new ItemStack(ModItems.BROWN_INGOT.get(), 1));
            chest.setItem(1, new ItemStack(ModBlocks.BLACK_DIRT.get(), 4));
            chest.setItem(2, new ItemStack(Items.BONE, 2));
            if (player.getRandom().nextInt(100) < 30) {
                chest.setItem(3, new ItemStack(ModItems.NULL_TOTEM.get()));
            }
            chest.setChanged();
        }
        message(player, "arkandayim.panel.knock_done");
    }

    private static void message(ServerPlayer player, String key) {
        player.displayClientMessage(Component.translatable(key), true);
    }
}
