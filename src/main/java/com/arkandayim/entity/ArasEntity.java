package com.arkandayim.entity;

import com.arkandayim.network.ModNetwork;
import com.arkandayim.registry.ModItems;
import com.arkandayim.item.GuardianTotemItem;
import com.arkandayim.network.LookPacket;
import com.arkandayim.util.Advancements;
import com.arkandayim.util.AngerManager;
import com.arkandayim.util.ArasFx;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ArasEntity extends Monster {

    public static final int STATE_CALM = 0;
    public static final int STATE_VANISHING = 1;
    public static final int STATE_APPROACHING = 2;
    public static final int STATE_STARE = 3;
    public static final int STATE_NIGHT_CHASE = 4;
    public static final int STATE_DYING = 5;
    /** It is gathering at the spot the player is forced to look at. */
    public static final int STATE_FORMING = 6;

    private static final EntityDataAccessor<Integer> DATA_STATE =
            SynchedEntityData.defineId(ArasEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_DIE_TICKS =
            SynchedEntityData.defineId(ArasEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_FLYING =
            SynchedEntityData.defineId(ArasEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_FORM_TICKS =
            SynchedEntityData.defineId(ArasEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_SCREAM =
            SynchedEntityData.defineId(ArasEntity.class, EntityDataSerializers.INT);

    private final Map<UUID, Integer> lookTicks = new HashMap<>();
    private UUID triggerPlayer;
    private UUID nightTarget;
    private boolean approachAfterVanish = true;
    private int vanishTicks;
    private int stareTicks;
    private int actionCooldown;
    private int huntTicks;
    private int formTicks;

    public ArasEntity(EntityType<? extends ArasEntity> type, Level level) {
        super(type, level);
        this.xpReward = 0;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 220.0D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.34D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 160.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.6D, false));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 96.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_STATE, STATE_CALM);
        this.entityData.define(DATA_DIE_TICKS, 0);
        this.entityData.define(DATA_FLYING, false);
        this.entityData.define(DATA_FORM_TICKS, 0);
        this.entityData.define(DATA_SCREAM, 0);
    }

    /** True while the jaw is open and the head is thrown back. */
    public boolean isScreaming() {
        return this.entityData.get(DATA_SCREAM) > 0;
    }

    /** Keeps the scream pose alive for a short while on the client. */
    public void scream(int ticks) {
        this.entityData.set(DATA_SCREAM, Math.max(this.entityData.get(DATA_SCREAM), ticks));
    }

    private void tickScream() {
        int ticks = this.entityData.get(DATA_SCREAM);
        if (ticks > 0) {
            this.entityData.set(DATA_SCREAM, ticks - 1);
        }
    }

    /** Ticks left of the gathering animation, 0 when it is not forming. */
    public int getFormTicks() {
        return this.entityData.get(DATA_FORM_TICKS);
    }

    private void setFormTicks(int ticks) {
        this.entityData.set(DATA_FORM_TICKS, ticks);
    }

    public int getState() {
        return this.entityData.get(DATA_STATE);
    }

    public void setState(int state) {
        this.entityData.set(DATA_STATE, state);
    }

    public int getDieTicks() {
        return this.entityData.get(DATA_DIE_TICKS);
    }

    private void setDieTicks(int ticks) {
        this.entityData.set(DATA_DIE_TICKS, ticks);
    }

    public boolean isFlying() {
        return this.entityData.get(DATA_FLYING);
    }

    public void setFlying(boolean flying) {
        if (this.entityData.get(DATA_FLYING) == flying) {
            return;
        }
        this.entityData.set(DATA_FLYING, flying);
        this.setNoGravity(flying);
        if (!flying) {
            Vec3 movement = this.getDeltaMovement();
            this.setDeltaMovement(movement.x, 0.0D, movement.z);
        }
    }

    public boolean isLocking() {
        int state = getState();
        return state == STATE_APPROACHING || state == STATE_STARE || state == STATE_NIGHT_CHASE;
    }

    public float getDeathAnimProgress(float partialTicks) {
        if (getState() != STATE_DYING) {
            return 0.0F;
        }
        return Mth.clamp((getDieTicks() + partialTicks) / 42.0F, 0.0F, 1.0F);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return;
        }
        if (this.level() instanceof ServerLevel serverLevel) {
            serverTick(serverLevel);
        }
    }

    private void serverTick(ServerLevel level) {
        tickScream();
        int state = getState();

        if (state == STATE_DYING) {
            int ticks = getDieTicks() + 1;
            setDieTicks(ticks);
            if (ticks < 42) {
                if (ticks % 2 == 0) {
                    level.sendParticles(ParticleTypes.FLAME, getX(), getY() + 1.0D, getZ(), 10, 0.5D, 0.9D, 0.5D, 0.04D);
                    level.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 1.2D, getZ(), 4, 0.4D, 0.7D, 0.4D, 0.02D);
                }
            } else {
                level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY() + 1.0D, getZ(), 2, 0.3D, 0.3D, 0.3D, 0.0D);
                this.discard();
            }
            return;
        }

        if (this.actionCooldown > 0) {
            this.actionCooldown--;
        }

        if (state != STATE_APPROACHING && state != STATE_NIGHT_CHASE) {
            setFlying(false);
        }

        if (state == STATE_APPROACHING || state == STATE_NIGHT_CHASE || state == STATE_FORMING) {
            if (checkTotem(level)) {
                return;
            }
        }

        switch (state) {
            case STATE_VANISHING -> {
                this.vanishTicks--;
                if (this.vanishTicks % 2 == 0) {
                    level.sendParticles(ParticleTypes.POOF, getX(), getY() + 1.0D, getZ(), 8, 0.4D, 0.9D, 0.4D, 0.02D);
                }
                if (this.vanishTicks <= 0) {
                    relocateAfterVanish(level);
                }
            }
            case STATE_APPROACHING -> {
                Player player = getTrackedPlayer(this.triggerPlayer);
                if (player == null || !player.isAlive()) {
                    setState(STATE_CALM);
                    this.setSprinting(false);
                    this.getNavigation().stop();
                    break;
                }
                if (this.huntTicks > 0) {
                    if (this.distanceTo(player) > 260.0D) {
                        relocateNear(level, player, 30.0D);
                    }
                    if (this.huntTicks % 45 == 0) {
                        scream(18);
                        level.playSound(null, getX(), getY(), getZ(), SoundEvents.ENDERMAN_SCREAM,
                                SoundSource.HOSTILE, 2.2F, 0.42F);
                    }
                    if (this.huntTicks % 34 == 0) {
                        double angle = this.getRandom().nextDouble() * Math.PI * 2.0D;
                        double spread = 5.0D + this.getRandom().nextDouble() * 7.0D;
                        strike(level, player.getX() + Math.cos(angle) * spread, player.getY(),
                                player.getZ() + Math.sin(angle) * spread);
                        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 1.1F, 1.2F);
                    }
                    updateChaseMovement(level, player, 2.8D);
                    this.getLookControl().setLookAt(player, 120.0F, 120.0F);
                    if (this.distanceTo(player) < 2.8D) {
                        catchPlayer(level, player);
                        break;
                    }
                    if (--this.huntTicks <= 0) {
                        endHunt(level, player);
                    }
                    break;
                }
                if (this.distanceTo(player) > 240.0D) {
                    relocateNear(level, player, 40.0D);
                }
                updateChaseMovement(level, player, 2.0D);
                this.getLookControl().setLookAt(player, 90.0F, 90.0F);
                if (this.distanceTo(player) < 6.0D) {
                    setState(STATE_CALM);
                    this.setSprinting(false);
                    this.getNavigation().stop();
                    this.actionCooldown = 140;
                    level.playSound(null, getX(), getY(), getZ(), SoundEvents.ENDERMAN_STARE, SoundSource.HOSTILE, 1.6F, 0.5F);
                }
            }
            case STATE_FORMING -> formingTick(level);
            case STATE_STARE -> {
                Player player = getTrackedPlayer(this.nightTarget);
                if (player != null) {
                    this.getNavigation().stop();
                    this.getLookControl().setLookAt(player, 120.0F, 120.0F);
                }
                this.stareTicks--;
                if (this.stareTicks <= 0) {
                    setState(STATE_NIGHT_CHASE);
                    if (player != null) {
                        this.setTarget(player);
                    }
                    level.playSound(null, getX(), getY(), getZ(), SoundEvents.ENDERMAN_SCREAM, SoundSource.HOSTILE, 2.0F, 0.4F);
                }
            }
            case STATE_NIGHT_CHASE -> {
                Player player = getTrackedPlayer(this.nightTarget);
                if (player == null || !player.isAlive() || level.isDay()) {
                    endSequence(level, true);
                    break;
                }
                if (this.distanceTo(player) > 200.0D) {
                    relocateNear(level, player, 24.0D);
                }
                this.setTarget(player);
                updateChaseMovement(level, player, 2.2D);
                this.getLookControl().setLookAt(player, 120.0F, 120.0F);
            }
            default -> {
                this.setSprinting(false);
                if (this.actionCooldown <= 0) {
                    if (this.tickCount % 100 == 0) {
                        keepInBand(level);
                    }
                    detectLook(level);
                }
            }
        }
    }

    /**
     * Runs on the ground with the running animation, or flies when nothing solid is below.
     */
    private void updateChaseMovement(ServerLevel level, Player player, double speed) {
        boolean shouldFly = player.isCreative() || player.isSpectator() || !hasGroundBeneath(level);
        if (shouldFly != isFlying()) {
            setFlying(shouldFly);
            this.getNavigation().stop();
        }
        this.setSprinting(!shouldFly);
        this.getLookControl().setLookAt(player, 120.0F, 120.0F);

        if (!shouldFly) {
            if (this.getNavigation().isDone() || (this.tickCount & 7) == 0) {
                this.getNavigation().moveTo(player, speed);
            }
            return;
        }

        double dx = player.getX() - getX();
        double dz = player.getZ() - getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        double forwardSpeed = horizontal > 1.2D ? Math.min(0.66D, 0.22D + horizontal * 0.06D) : 0.0D;
        double vx = horizontal > 1.0E-4D ? dx / horizontal * forwardSpeed : 0.0D;
        double vz = horizontal > 1.0E-4D ? dz / horizontal * forwardSpeed : 0.0D;
        double targetY = player.getY() + player.getBbHeight() * 0.55D;
        double lift = Mth.clamp((targetY - getY()) * 0.16D, -0.25D, 0.45D);
        this.setDeltaMovement(vx, lift, vz);
        this.hasImpulse = true;
        this.getNavigation().stop();
    }

    /** True when there is solid ground within three blocks under the creature. */
    private boolean hasGroundBeneath(ServerLevel level) {
        BlockPos below = BlockPos.containing(getX(), getY() - 0.15D, getZ());
        for (int i = 0; i < 3; i++) {
            BlockPos check = below.below(i);
            if (!level.getBlockState(check).getCollisionShape(level, check).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private Player getTrackedPlayer(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return this.level().getPlayerByUUID(uuid);
    }

    private void detectLook(ServerLevel level) {
        if ((this.tickCount & 1) != 0) {
            return;
        }
        for (Player player : level.players()) {
            if (player.isCreative() || player.isSpectator()) {
                continue;
            }
            double distance = player.distanceTo(this);
            if (distance < 3.0D || distance > 100.0D) {
                continue;
            }
            Vec3 toEntity = new Vec3(this.getX() - player.getX(),
                    (this.getY() + 1.7D) - player.getEyeY(),
                    this.getZ() - player.getZ()).normalize();
            if (player.getViewVector(1.0F).dot(toEntity) > 0.985D && player.hasLineOfSight(this)) {
                int ticks = this.lookTicks.getOrDefault(player.getUUID(), 0) + 1;
                this.lookTicks.put(player.getUUID(), ticks);
                if (ticks >= 20) {
                    this.lookTicks.remove(player.getUUID());
                    startVanish(player, true);
                    return;
                }
            } else {
                int ticks = this.lookTicks.getOrDefault(player.getUUID(), 0);
                if (ticks > 0) {
                    this.lookTicks.put(player.getUUID(), ticks - 1);
                }
            }
        }
    }

    public void startVanish(Player player, boolean approachAfter) {
        int state = getState();
        if (state == STATE_DYING || state == STATE_NIGHT_CHASE) {
            return;
        }
        this.triggerPlayer = player.getUUID();
        this.approachAfterVanish = approachAfter;
        setState(STATE_VANISHING);
        this.vanishTicks = 12;
        this.setSprinting(false);
        this.setDeltaMovement(0.0D, 0.0D, 0.0D);
        this.getNavigation().stop();
        this.level().playSound(null, getX(), getY(), getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.4F, 0.45F);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.POOF, getX(), getY() + 1.0D, getZ(), 26, 0.4D, 1.0D, 0.4D, 0.05D);
            serverLevel.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 1.0D, getZ(), 14, 0.5D, 1.0D, 0.5D, 0.02D);
        }
    }

    private void relocateAfterVanish(ServerLevel level) {
        Player player = getTrackedPlayer(this.triggerPlayer);
        if (player == null) {
            setState(STATE_CALM);
            return;
        }
        double distance = this.getRandom().nextBoolean() ? 50.0D : 100.0D;
        double side = (this.getRandom().nextBoolean() ? 1.0D : -1.0D) * (70.0D + this.getRandom().nextDouble() * 40.0D);
        double yaw = Math.toRadians(player.getYRot() + side);
        double x = player.getX() - Math.sin(yaw) * distance;
        double z = player.getZ() + Math.cos(yaw) * distance;
        BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                new BlockPos(Mth.floor(x), 0, Mth.floor(z)));
        this.teleportTo(x, surface.getY(), z);
        this.setDeltaMovement(0.0D, 0.0D, 0.0D);
        level.sendParticles(ParticleTypes.POOF, x, surface.getY() + 1.0D, z, 20, 0.4D, 1.0D, 0.4D, 0.04D);
        if (this.approachAfterVanish && AngerManager.get(player.getUUID()) >= AngerManager.MAX) {
            setState(STATE_APPROACHING);
            this.setTarget(player);
            level.playSound(null, x, surface.getY(), z, SoundEvents.ENDERMAN_SCREAM, SoundSource.HOSTILE, 2.0F, 0.35F);
        } else {
            setState(STATE_CALM);
            this.actionCooldown = 60;
        }
    }

    private void relocateNear(ServerLevel level, Player player, double distance) {
        Vec3 look = player.getViewVector(1.0F);
        double x = player.getX() - look.x * distance + (this.getRandom().nextDouble() - 0.5D) * 10.0D;
        double z = player.getZ() - look.z * distance + (this.getRandom().nextDouble() - 0.5D) * 10.0D;
        BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                new BlockPos(Mth.floor(x), 0, Mth.floor(z)));
        this.teleportTo(x, surface.getY(), z);
        this.setDeltaMovement(0.0D, 0.0D, 0.0D);
        level.sendParticles(ParticleTypes.POOF, x, surface.getY() + 1.0D, z, 16, 0.4D, 1.0D, 0.4D, 0.04D);
    }

    private void keepInBand(ServerLevel level) {
        Player nearest = level.getNearestPlayer(this, 200.0D);
        if (nearest == null) {
            return;
        }
        if (this.distanceTo(nearest) < 115.0D) {
            return;
        }
        if (isWatchedByAnyone(level)) {
            return;
        }
        double distance = this.getRandom().nextBoolean() ? 50.0D : 100.0D;
        double side = (this.getRandom().nextBoolean() ? 1.0D : -1.0D) * (70.0D + this.getRandom().nextDouble() * 40.0D);
        double yaw = Math.toRadians(nearest.getYRot() + side);
        double x = nearest.getX() - Math.sin(yaw) * distance;
        double z = nearest.getZ() + Math.cos(yaw) * distance;
        BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                new BlockPos(Mth.floor(x), 0, Mth.floor(z)));
        this.teleportTo(x, surface.getY(), z);
        this.setDeltaMovement(0.0D, 0.0D, 0.0D);
    }

    private boolean isWatchedByAnyone(ServerLevel level) {
        for (Player player : level.players()) {
            if (player.distanceTo(this) > 100.0D) {
                continue;
            }
            Vec3 toEntity = new Vec3(this.getX() - player.getX(),
                    (this.getY() + 1.7D) - player.getEyeY(),
                    this.getZ() - player.getZ()).normalize();
            if (player.getViewVector(1.0F).dot(toEntity) > 0.96D) {
                return true;
            }
        }
        return false;
    }

    public void forceApproach(Player player) {
        if (getState() == STATE_DYING) {
            return;
        }
        if (AngerManager.get(player.getUUID()) < AngerManager.MAX) {
            return;
        }
        this.triggerPlayer = player.getUUID();
        setState(STATE_APPROACHING);
        this.setTarget(player);
        this.actionCooldown = 0;
        this.level().playSound(null, getX(), getY(), getZ(), SoundEvents.ENDERMAN_SCREAM, SoundSource.HOSTILE, 1.4F, 0.4F);
    }

    /**
     * The full anger bar hunt: it shows up right away, it has ten seconds to reach the
     * player, and when it fails the player is released and the creature is gone.
     */
    public void beginHunt(ServerPlayer player) {
        if (getState() == STATE_DYING) {
            return;
        }
        ServerLevel level = (ServerLevel) player.level();
        this.triggerPlayer = player.getUUID();
        this.nightTarget = player.getUUID();
        this.huntTicks = 200;
        this.formTicks = 52;
        setFormTicks(this.formTicks);
        setState(STATE_FORMING);
        this.setTarget(null);
        this.actionCooldown = 0;
        this.setSprinting(false);
        this.setFlying(false);
        this.getNavigation().stop();
        relocateNear(level, player, 24.0D);

        // Turn the player's head to the spot before anything appears there.
        ModNetwork.sendToPlayer(new LookPacket(getX(), getY() + 1.4D, getZ(), 60), player);
        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 260, 0, true, false, false));
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.ENDERMAN_STARE,
                SoundSource.HOSTILE, 2.6F, 0.32F);
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER,
                SoundSource.WEATHER, 1.4F, 0.55F);
    }

    /** The creature gathers in lightning for a couple of seconds, then turns and runs. */
    private void formingTick(ServerLevel level) {
        this.formTicks--;
        setFormTicks(Math.max(0, this.formTicks));
        this.setDeltaMovement(0.0D, 0.0D, 0.0D);
        this.getNavigation().stop();
        this.getLookControl().setLookAt(this.getX(), this.getY() + 1.0D, this.getZ() + 1.0D, 30.0F, 30.0F);
        if (this.formTicks % 3 == 0) {
            level.sendParticles(ParticleTypes.SOUL, getX() + (getRandom().nextDouble() - 0.5D) * 1.6D,
                    getY() + getRandom().nextDouble() * 2.6D,
                    getZ() + (getRandom().nextDouble() - 0.5D) * 1.6D,
                    3, 0.2D, 0.3D, 0.2D, 0.02D);
            level.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 1.2D, getZ(),
                    2, 0.5D, 1.0D, 0.5D, 0.01D);
        }
        if (this.formTicks % 13 == 0) {
            strike(level, getX() + (getRandom().nextDouble() - 0.5D) * 3.0D, getY(),
                    getZ() + (getRandom().nextDouble() - 0.5D) * 3.0D);
            level.playSound(null, getX(), getY(), getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER,
                    SoundSource.WEATHER, 1.0F, 1.15F);
        }
        if (this.formTicks > 0) {
            return;
        }
        Player target = getTrackedPlayer(this.triggerPlayer);
        if (!(target instanceof ServerPlayer serverPlayer) || !serverPlayer.isAlive()) {
            setState(STATE_CALM);
            this.discard();
            return;
        }
        strike(level, getX(), getY(), getZ());
        scream(40);
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.ENDERMAN_SCREAM,
                SoundSource.HOSTILE, 3.0F, 0.3F);
        this.setTarget(serverPlayer);
        this.getLookControl().setLookAt(serverPlayer, 180.0F, 180.0F);
        this.huntTicks = 200;
        serverPlayer.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0, true, false, false));
        serverPlayer.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 200, 0, true, false, false));
        setFormTicks(0);
        setState(STATE_APPROACHING);
    }

    private static void strike(ServerLevel level, double x, double y, double z) {
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt == null) {
            return;
        }
        bolt.moveTo(x, y, z);
        bolt.setVisualOnly(true);
        level.addFreshEntity(bolt);
    }

    private void catchPlayer(ServerLevel level, Player player) {
        scream(30);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDERMAN_SCREAM, SoundSource.HOSTILE, 3.0F, 0.28F);
        // The guardian totem swallows the killing blow instead of the whole chase.
        if (GuardianTotemItem.consume(player)) {
            strike(level, getX(), getY(), getZ());
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 2.0F, 0.6F);
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 2, true, true, true));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 2, true, true, true));
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable("arkandayim.totem.guardian_saved"), true);
            endHunt(level, player);
            return;
        }
        player.hurt(level.damageSources().mobAttack(this), 1000.0F);
        endHunt(level, player);
    }

    private void endHunt(ServerLevel level, Player player) {
        if (player != null) {
            player.removeEffect(MobEffects.BLINDNESS);
            player.removeEffect(MobEffects.DARKNESS);
            if (player instanceof ServerPlayer serverPlayer) {
                AngerManager.reset(serverPlayer);
            }
        }
        this.huntTicks = 0;
        this.nightTarget = null;
        this.triggerPlayer = null;
        this.setTarget(null);
        setState(STATE_CALM);
        this.setSprinting(false);
        this.getNavigation().stop();
        level.sendParticles(ParticleTypes.POOF, getX(), getY() + 1.0D, getZ(), 30, 0.5D, 1.0D, 0.5D, 0.05D);
        this.discard();
    }

    /** While the creature charges, a totem in either hand burns it out of the world. */
    private boolean checkTotem(ServerLevel level) {
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class,
                this.getBoundingBox().inflate(14.0D))) {
            ItemStack main = player.getMainHandItem();
            ItemStack off = player.getOffhandItem();
            ItemStack totem;
            if (main.is(ModItems.NULL_TOTEM.get())) {
                totem = main;
            } else if (off.is(ModItems.NULL_TOTEM.get())) {
                totem = off;
            } else {
                continue;
            }
            totem.shrink(1);
            destroyByTotem(level, player);
            return true;
        }
        return false;
    }

    private void destroyByTotem(ServerLevel level, ServerPlayer player) {
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt != null) {
            bolt.moveTo(getX(), getY(), getZ());
            bolt.setVisualOnly(true);
            level.addFreshEntity(bolt);
        }
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER,
                SoundSource.WEATHER, 3.0F, 0.7F);
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.TOTEM_USE,
                SoundSource.PLAYERS, 2.0F, 0.5F);
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY() + 1.0D, getZ(), 4, 0.6D, 0.8D, 0.6D, 0.0D);
        player.removeEffect(MobEffects.BLINDNESS);
        player.removeEffect(MobEffects.DARKNESS);
        AngerManager.reset(player);
        this.setTarget(null);
        setState(STATE_CALM);
        this.setSprinting(false);
        this.getNavigation().stop();
        this.discard();
    }

    public void beginNightChase(ServerPlayer player) {
        this.nightTarget = player.getUUID();
        this.setTarget(player);
        setState(STATE_STARE);
        scream(30);
        this.stareTicks = 70;
        this.setSprinting(false);
        this.getNavigation().stop();
        this.level().playSound(null, getX(), getY(), getZ(), SoundEvents.ENDERMAN_STARE, SoundSource.HOSTILE, 2.0F, 0.4F);
    }

    public void beginDeathSequence(LivingEntity killer) {
        if (getState() == STATE_DYING) {
            return;
        }
        setState(STATE_DYING);
        setDieTicks(0);
        this.setNoAi(true);
        this.setTarget(null);
        setFlying(false);
        this.setSprinting(false);
        this.setDeltaMovement(0.0D, 0.0D, 0.0D);
        this.getNavigation().stop();

        Level level = this.level();
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt != null) {
            bolt.moveTo(this.getX(), this.getY(), this.getZ());
            bolt.setVisualOnly(true);
            level.addFreshEntity(bolt);
        }
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 3.0F, 0.8F);
        ArasFx.explodeNoDamage(level, this.getX(), this.getY() + 1.0D, this.getZ(), 3.0F);

        if (killer instanceof ServerPlayer serverPlayer) {
            AngerManager.reset(serverPlayer);
            serverPlayer.removeEffect(MobEffects.BLINDNESS);
            serverPlayer.removeEffect(MobEffects.DARKNESS);
            Advancements.grant(serverPlayer, Advancements.NULL_DIED);
        }
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY() + 1.0D, getZ(), 3, 0.5D, 0.5D, 0.5D, 0.0D);
            ModNetwork.sendFx(serverLevel, 0, getX(), getY() + 1.0D, getZ());
        }
        this.hurtTime = 0;
        this.deathTime = 0;
    }

    private void endSequence(ServerLevel level, boolean resetAnger) {
        Player player = getTrackedPlayer(this.nightTarget);
        if (resetAnger && player instanceof ServerPlayer serverPlayer) {
            AngerManager.reset(serverPlayer);
        }
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.POOF, getX(), getY() + 1.0D, getZ(), 30, 0.5D, 1.0D, 0.5D, 0.05D);
        }
        this.nightTarget = null;
        this.triggerPlayer = null;
        this.setTarget(null);
        this.discard();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (getState() == STATE_DYING) {
            return false;
        }
        if (source.getEntity() instanceof Player player
                && player.getMainHandItem().is(ModItems.BROWN_SWORD.get())) {
            beginDeathSequence(player);
            return false;
        }
        int state = getState();
        if (source.getEntity() instanceof Player player && (state == STATE_APPROACHING || state == STATE_NIGHT_CHASE)) {
            startVanish(player, false);
            this.hurtTime = 0;
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void checkDespawn() {
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENDERMAN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENDERMAN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENDERMAN_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return 0.55F;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("ArasState", getState());
        if (this.triggerPlayer != null) {
            tag.putUUID("ArasTrigger", this.triggerPlayer);
        }
        if (this.nightTarget != null) {
            tag.putUUID("ArasNight", this.nightTarget);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setState(STATE_CALM);
        setDieTicks(0);
        if (tag.hasUUID("ArasTrigger")) {
            this.triggerPlayer = tag.getUUID("ArasTrigger");
        }
    }
}
