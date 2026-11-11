package com.arkandayim.item;

import com.arkandayim.entity.ArasEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BrownSwordItem extends SwordItem {

    public BrownSwordItem() {
        super(new BrownTier(), 5, -2.2F, new Item.Properties().fireResistant());
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (target instanceof ArasEntity aras) {
            aras.beginDeathSequence(attacker);
            return true;
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.pass(stack);
        }

        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 look = player.getViewVector(1.0F);
        BlockHitResult hit = level.clip(new ClipContext(eye, eye.add(look.scale(48.0D)),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 target = hit.getType() == HitResult.Type.MISS
                ? eye.add(look.scale(42.0D))
                : hit.getLocation();

        if (!level.isClientSide) {
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
            if (bolt != null) {
                bolt.moveTo(target.x, target.y, target.z);
                bolt.setVisualOnly(true);
                level.addFreshEntity(bolt);
            }
            level.playSound(null, target.x, target.y, target.z,
                    SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 3.0F, 0.9F);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 2.0F, 1.1F);

            AABB area = new AABB(target.x - 3.5D, target.y - 3.0D, target.z - 3.5D,
                    target.x + 3.5D, target.y + 4.0D, target.z + 3.5D);
            for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, area)) {
                if (living == player || living instanceof Player) {
                    continue;
                }
                if (living instanceof ArasEntity aras) {
                    aras.beginDeathSequence(player);
                } else {
                    living.hurt(level.damageSources().magic(), 9.0F);
                }
            }
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.x, target.y + 0.6D, target.z,
                        40, 0.5D, 0.8D, 0.5D, 0.28D);
            }
        }

        player.getCooldowns().addCooldown(this, 160);
        player.swing(hand, true);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
