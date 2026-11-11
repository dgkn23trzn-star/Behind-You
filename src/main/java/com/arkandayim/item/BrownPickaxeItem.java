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
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.Level;

/** Right click sends out an ore pulse that counts and glints nearby ores. */
public class BrownPickaxeItem extends PickaxeItem {

    public BrownPickaxeItem() {
        super(new BrownTier(), 3, -2.8F, new Item.Properties().fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide) {
            BlockPos target = ToolAbilities.lookPos(level, player, 6.0D);
            int found = ToolAbilities.pulseOres(level, target);
            int mined = ToolAbilities.mineOres(level, player, target, 4, 12);
            if (mined > 0) {
                stack.hurtAndBreak(mined, player, p -> p.broadcastBreakEvent(hand));
            }
            player.displayClientMessage(Component.translatable("arkandayim.tool.pickaxe", found, mined), true);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2F, 0.7F);
        }
        player.getCooldowns().addCooldown(this, 60);
        player.swing(hand, true);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
