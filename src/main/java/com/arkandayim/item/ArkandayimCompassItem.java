package com.arkandayim.item;

import com.arkandayim.registry.ModItems;
import com.arkandayim.util.ArkandayimData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Points at the black book house. Right click reports the distance and the
 * direction, so it works exactly like a compass without needing a lodestone.
 */
public class ArkandayimCompassItem extends Item {

    public ArkandayimCompassItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }
        if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return InteractionResultHolder.pass(stack);
        }
        BlockPos house = ArkandayimData.get(serverLevel).getHousePos();
        if (house == null) {
            player.displayClientMessage(Component.translatable("arkandayim.compass.none")
                    .withStyle(ChatFormatting.DARK_GRAY), false);
            return InteractionResultHolder.pass(stack);
        }
        double dx = house.getX() - player.getX();
        double dz = house.getZ() - player.getZ();
        int distance = (int) Math.round(Math.sqrt(dx * dx + dz * dz));
        String key = directionKey(dx, dz);
        player.displayClientMessage(Component.translatable("arkandayim.compass.read",
                distance, Component.translatable(key), house.getX(), house.getY(), house.getZ()), false);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS, 0.8F, 1.3F);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    private static String directionKey(double dx, double dz) {
        double angle = Math.toDegrees(Math.atan2(dx, -dz));
        if (angle < 0) {
            angle += 360.0D;
        }
        if (angle < 22.5D || angle >= 337.5D) {
            return "arkandayim.dir.north";
        }
        if (angle < 67.5D) {
            return "arkandayim.dir.northeast";
        }
        if (angle < 112.5D) {
            return "arkandayim.dir.east";
        }
        if (angle < 157.5D) {
            return "arkandayim.dir.southeast";
        }
        if (angle < 202.5D) {
            return "arkandayim.dir.south";
        }
        if (angle < 247.5D) {
            return "arkandayim.dir.southwest";
        }
        if (angle < 292.5D) {
            return "arkandayim.dir.west";
        }
        return "arkandayim.dir.northwest";
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.arkandayim.arkandayim_compass.tip")
                .withStyle(ChatFormatting.DARK_GRAY));
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            BlockPos house = ArkandayimData.get(serverLevel).getHousePos();
            if (house != null) {
                tooltip.add(Component.translatable("arkandayim.compass.coords",
                        house.getX(), house.getY(), house.getZ()).withStyle(ChatFormatting.GRAY));
            }
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    public static ItemStack create() {
        return new ItemStack(ModItems.ARKANDAYIM_COMPASS.get());
    }
}
