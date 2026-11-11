package com.arkandayim.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import java.util.List;

/**
 * A black leather book. The last page holds the coordinates of the old mansion
 * that was built near the world spawn.
 */
public class BlackBookItem extends Item {

    public static final String HOUSE_X = "HouseX";
    public static final String HOUSE_Y = "HouseY";
    public static final String HOUSE_Z = "HouseZ";

    public BlackBookItem(Properties properties) {
        super(properties);
    }

    public static void writeHouseCoords(ItemStack stack, int x, int y, int z) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(HOUSE_X, x);
        tag.putInt(HOUSE_Y, y);
        tag.putInt(HOUSE_Z, z);
    }

    public static boolean hasHouseCoords(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(HOUSE_X) && tag.contains(HOUSE_Y) && tag.contains(HOUSE_Z);
    }

    public static int[] readHouseCoords(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !hasHouseCoords(stack)) {
            return null;
        }
        return new int[]{tag.getInt(HOUSE_X), tag.getInt(HOUSE_Y), tag.getInt(HOUSE_Z)};
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.arkandayim.client.BlackBookScreen.open(stack));
        }
        player.playSound(SoundEvents.BOOK_PAGE_TURN, 1.0F, 0.85F);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.arkandayim.black_book.tip").withStyle(ChatFormatting.DARK_GRAY));
    }
}
