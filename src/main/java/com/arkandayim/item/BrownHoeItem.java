package com.arkandayim.item;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Right click turns the 3x3 under your crosshair into black soil. */
public class BrownHoeItem extends HoeItem {

    public BrownHoeItem() {
        super(new BrownTier(), -3, -1.0F, new Item.Properties().fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide) {
            BlockPos target = ToolAbilities.lookPos(level, player, 5.0D);
            int changed = ToolAbilities.corruptGround(level, target, 2);
            if (changed > 0) {
                stack.hurtAndBreak(Math.max(1, changed / 8), player, p -> p.broadcastBreakEvent(hand));
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ROOTED_DIRT_PLACE, SoundSource.PLAYERS, 1.0F, 0.6F);
            }
            player.displayClientMessage(Component.translatable("arkandayim.tool.hoe", changed), true);
        }
        player.getCooldowns().addCooldown(this, 60);
        player.swing(hand, true);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
