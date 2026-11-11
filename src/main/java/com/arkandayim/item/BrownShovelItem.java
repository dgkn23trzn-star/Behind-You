package com.arkandayim.item;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.Level;

/** Right click digs a 3x3 scoop of soil. */
public class BrownShovelItem extends ShovelItem {

    public BrownShovelItem() {
        super(new BrownTier(), 3.5F, -3.0F, new Item.Properties().fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide) {
            BlockPos target = ToolAbilities.lookPos(level, player, 5.0D);
            int broken = ToolAbilities.digScoop(level, player, target, 2);
            if (broken > 0) {
                stack.hurtAndBreak(Math.max(1, broken / 4), player, p -> p.broadcastBreakEvent(hand));
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.GRAVEL_BREAK, SoundSource.PLAYERS, 1.2F, 0.8F);
            }
            player.displayClientMessage(Component.translatable("arkandayim.tool.shovel", broken), true);
        }
        player.getCooldowns().addCooldown(this, 50);
        player.swing(hand, true);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
