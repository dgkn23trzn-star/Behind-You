package com.arkandayim.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Hold this while the creature comes for you and it is struck down by lightning.
 * The behaviour itself lives on the creature, which checks both hands.
 */
public class NullTotemItem extends Item {

    public NullTotemItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.arkandayim.null_totem.tip").withStyle(ChatFormatting.DARK_GRAY));
    }
}
