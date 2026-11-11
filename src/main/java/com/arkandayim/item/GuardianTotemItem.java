package com.arkandayim.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * A safety net. Right click for a short shield, and while it sits in the inventory
 * it swallows one lethal hit (see CommonEvents).
 */
public class GuardianTotemItem extends Item {

    public GuardianTotemItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 1, true, true, true));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 400, 1, true, true, true));
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.1F);
            player.displayClientMessage(Component.translatable("arkandayim.totem.guardian_used"), true);
        }
        player.getCooldowns().addCooldown(this, 900);
        player.swing(hand, true);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    /** True when the player carries one anywhere in the inventory. */
    public static boolean isCarried(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof GuardianTotemItem) {
                return true;
            }
        }
        return player.getOffhandItem().getItem() instanceof GuardianTotemItem;
    }

    /** Burns one totem from the inventory and returns true when one was found. */
    public static boolean consume(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof GuardianTotemItem) {
                stack.shrink(1);
                return true;
            }
        }
        ItemStack off = player.getOffhandItem();
        if (off.getItem() instanceof GuardianTotemItem) {
            off.shrink(1);
            return true;
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.arkandayim.guardian_totem.tip")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
