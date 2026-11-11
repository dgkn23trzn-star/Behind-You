package com.arkandayim.item;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Right click fells the whole trunk of the tree you are looking at. */
public class BrownAxeItem extends AxeItem {

    public BrownAxeItem() {
        super(new BrownTier(), 6.0F, -3.0F, new Item.Properties().fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide) {
            BlockPos target = ToolAbilities.lookPos(level, player, 5.5D);
            int broken = ToolAbilities.fellTree(level, player, target);
            if (broken > 0) {
                stack.hurtAndBreak(Math.max(1, broken / 3), player, p -> p.broadcastBreakEvent(hand));
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.WOOD_BREAK, SoundSource.PLAYERS, 1.6F, 0.7F);
            }
            player.displayClientMessage(Component.translatable("arkandayim.tool.axe", broken), true);
        }
        player.getCooldowns().addCooldown(this, 80);
        player.swing(hand, true);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
