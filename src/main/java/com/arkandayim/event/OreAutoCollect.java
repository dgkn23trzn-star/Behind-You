package com.arkandayim.event;

import com.arkandayim.ArkandayimMod;
import com.arkandayim.item.ToolAbilities;
import com.arkandayim.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Ore broken with the brown pickaxe goes straight into the inventory. The block still
 * breaks normally, so tool damage, sounds and the ore experience stay vanilla; only the
 * floating drop is caught the moment it is about to spawn and put into the bag instead.
 */
@Mod.EventBusSubscriber(modid = ArkandayimMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class OreAutoCollect {

    private static final List<PendingOre> PENDING = new ArrayList<>();
    /** Guards against catching the item we ourselves drop when the inventory is full. */
    private static boolean spilling = false;

    private record PendingOre(ServerLevel level, BlockPos pos, UUID player, long tick) {
    }

    private OreAutoCollect() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onOreBroken(BlockEvent.BreakEvent event) {
        if (event.isCanceled() || !(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (!ToolAbilities.isOre(event.getState())) {
            return;
        }
        if (!player.getMainHandItem().is(ModItems.BROWN_PICKAXE.get())) {
            return;
        }
        PENDING.add(new PendingOre(level, event.getPos().immutable(), player.getUUID(), level.getGameTime()));
    }

    @SubscribeEvent
    public static void onItemSpawn(EntityJoinLevelEvent event) {
        if (spilling || PENDING.isEmpty() || !(event.getEntity() instanceof ItemEntity item)) {
            return;
        }
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        long tick = level.getGameTime();
        BlockPos pos = item.blockPosition();
        for (Iterator<PendingOre> iterator = PENDING.iterator(); iterator.hasNext(); ) {
            PendingOre pending = iterator.next();
            if (pending.tick() < tick) {
                iterator.remove();
                continue;
            }
            if (pending.tick() != tick || pending.level() != level || !pending.pos().equals(pos)) {
                continue;
            }
            ServerPlayer player = pending.level().getServer() == null
                    ? null : pending.level().getServer().getPlayerList().getPlayer(pending.player());
            if (player == null) {
                return;
            }
            ItemStack drop = item.getItem().copy();
            event.setCanceled(true);
            spilling = true;
            try {
                if (!drop.isEmpty() && !player.getInventory().add(drop)) {
                    player.drop(drop, false);
                }
            } finally {
                spilling = false;
            }
            return;
        }
    }
}
