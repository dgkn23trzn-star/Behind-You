package com.arkandayim.item;

import com.arkandayim.entity.ArasEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Right click: slips the player out of sight of the creature. Invisible for a few
 * seconds and thrown away from it, which is the only way out of a losing chase.
 */
public class ShadowTotemItem extends Item {

    public ShadowTotemItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.pass(stack);
        }
        ArasEntity nearest = null;
        double best = 70.0D;
        for (ArasEntity aras : level.getEntitiesOfClass(ArasEntity.class, player.getBoundingBox().inflate(70.0D))) {
            double distance = aras.distanceTo(player);
            if (distance < best) {
                best = distance;
                nearest = aras;
            }
        }
        if (nearest == null) {
            player.displayClientMessage(Component.translatable("arkandayim.totem.shadow_none")
                    .withStyle(ChatFormatting.DARK_GRAY), true);
            return InteractionResultHolder.pass(stack);
        }

        Vec3 away = player.position().subtract(nearest.position());
        if (away.lengthSqr() < 0.01D) {
            away = player.getLookAngle().reverse();
        }
        Vec3 direction = away.normalize();
        double targetX = player.getX() + direction.x * 26.0D;
        double targetZ = player.getZ() + direction.z * 26.0D;
        int y = serverLevel.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (int) targetX, (int) targetZ);
        serverLevel.sendParticles(ParticleTypes.POOF, player.getX(), player.getY() + 1.0D, player.getZ(),
                40, 0.5D, 1.0D, 0.5D, 0.06D);
        player.teleportTo(targetX, y, targetZ);
        serverLevel.sendParticles(ParticleTypes.SMOKE, targetX, y + 1.0D, targetZ,
                30, 0.4D, 0.8D, 0.4D, 0.03D);
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 200, 0, true, false, false));
        player.removeEffect(MobEffects.DARKNESS);
        player.removeEffect(MobEffects.BLINDNESS);
        level.playSound(null, targetX, y, targetZ, SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS, 1.6F, 0.5F);
        player.displayClientMessage(Component.translatable("arkandayim.totem.shadow_used"), true);
        stack.shrink(1);
        player.getCooldowns().addCooldown(this, 200);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.arkandayim.shadow_totem.tip")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
