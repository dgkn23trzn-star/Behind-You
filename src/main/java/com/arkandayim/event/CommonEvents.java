package com.arkandayim.event;

import com.arkandayim.ArkandayimMod;
import com.arkandayim.entity.ArasEntity;
import com.arkandayim.entity.ModEntities;
import com.arkandayim.item.BlackBookItem;
import com.arkandayim.item.GuardianTotemItem;
import com.arkandayim.network.FlashlightPacket;
import com.arkandayim.registry.ModItems;
import com.arkandayim.registry.ModSounds;
import com.arkandayim.util.Advancements;
import com.arkandayim.util.AngerManager;
import com.arkandayim.util.ArkandayimData;
import com.arkandayim.util.DarkZone;
import com.arkandayim.util.LuxuryHouse;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

@Mod.EventBusSubscriber(modid = ArkandayimMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonEvents {

    private static final List<PendingSound> PENDING = new ArrayList<>();
    private static final RandomSource RANDOM = RandomSource.create();
    private static int syncTimer = 0;

    private record PendingSound(ServerLevel level, Vec3 pos, int tick, SoundEvent sound, float volume, float pitch) {
    }

    /**
     * Typing "null" in chat is a dare: the bar fills a little. "aras" does nothing at all.
     */
    @SubscribeEvent
    public static void onChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        String raw = event.getMessage().getString().trim().toLowerCase(Locale.ROOT);
        if (raw.equals("null")) {
            AngerManager.add(player, 10);
            player.displayClientMessage(Component.translatable("arkandayim.chat.null"), false);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.WHISPER.get(), SoundSource.HOSTILE, 1.2F, 0.55F);
        }
    }

    /** The guardian totem eats one killing blow while it sits in the inventory. */
    @SubscribeEvent
    public static void onLivingDamage(net.minecraftforge.event.entity.living.LivingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (event.getAmount() < player.getHealth() + player.getAbsorptionAmount()) {
            return;
        }
        if (player.isCreative() || !GuardianTotemItem.isCarried(player)) {
            return;
        }
        if (!GuardianTotemItem.consume(player)) {
            return;
        }
        event.setCanceled(true);
        player.setHealth(Math.min(player.getMaxHealth(), 8.0F));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 2, true, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 2, true, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 1, true, true, true));
        if (player.level() instanceof ServerLevel level) {
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
            if (bolt != null) {
                bolt.moveTo(player.getX(), player.getY(), player.getZ());
                bolt.setVisualOnly(true);
                level.addFreshEntity(bolt);
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 2.0F, 0.9F);
        }
        player.displayClientMessage(Component.translatable("arkandayim.totem.guardian_saved"), true);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!(event.player instanceof ServerPlayer player)) {
            return;
        }
        keepFlashlight(player);
        trackStillness(player);
        trackOldResidents(player);
        if (player.isSpectator() || (player.tickCount & 3) != 0) {
            return;
        }
        fillAngerNearCreature(player);
        if (AngerManager.get(player.getUUID()) < 25 && looksAtArasFromAfar(player)) {
            AngerManager.set(player, AngerManager.get(player.getUUID()) + 1);
        }
        ambientHorror(player);
    }

    /**
     * The flashlight is a server side night vision effect, so it keeps burning after
     * respawns and works exactly the same on a dedicated server.
     */
    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!(player.level() instanceof ServerLevel level) || level.dimension() != Level.OVERWORLD) {
            return;
        }
        BlockPos house = LuxuryHouse.ensure(level);
        CompoundTag data = player.getPersistentData();
        if (!data.getBoolean("arkandayim:book_given")) {
            data.putBoolean("arkandayim:book_given", true);
            ItemStack book = new ItemStack(ModItems.BLACK_BOOK.get());
            BlackBookItem.writeHouseCoords(book, house.getX(), house.getY(), house.getZ());
            if (!player.getInventory().add(book)) {
                player.drop(book, false);
            }
            player.displayClientMessage(Component.translatable("arkandayim.book.given"), false);
        }
    }

    /** Ten minutes of standing still earns the feeling of being watched. */
    private static void trackStillness(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains("arkandayim:lastX")) {
            rememberPosition(data, player);
            return;
        }
        double dx = player.getX() - data.getDouble("arkandayim:lastX");
        double dy = player.getY() - data.getDouble("arkandayim:lastY");
        double dz = player.getZ() - data.getDouble("arkandayim:lastZ");
        if (dx * dx + dy * dy + dz * dz < 0.0025D) {
            int still = data.getInt("arkandayim:still") + 1;
            data.putInt("arkandayim:still", still);
            if (still >= 12000) {
                Advancements.grant(player, Advancements.WATCHED);
            }
        } else {
            data.putInt("arkandayim:still", 0);
            rememberPosition(data, player);
        }
    }

    private static void rememberPosition(CompoundTag data, ServerPlayer player) {
        data.putDouble("arkandayim:lastX", player.getX());
        data.putDouble("arkandayim:lastY", player.getY());
        data.putDouble("arkandayim:lastZ", player.getZ());
    }

    private static void trackOldResidents(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        BlockPos house = ArkandayimData.get(level).getHousePos();
        if (house != null && LuxuryHouse.contains(house, player.position())) {
            Advancements.grant(player, Advancements.OLD_RESIDENTS);
        }
    }

    /** Standing close to the creature at night slowly fills the bar. */
    private static void fillAngerNearCreature(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level) || !level.isNight()) {
            return;
        }
        if ((player.tickCount % 40) != 0 || AngerManager.get(player.getUUID()) >= AngerManager.MAX) {
            return;
        }
        for (ArasEntity aras : level.getEntitiesOfClass(ArasEntity.class, player.getBoundingBox().inflate(80.0D))) {
            if (aras.isAlive()) {
                AngerManager.add(player, 1);
                return;
            }
        }
    }

    @SubscribeEvent
    public static void onCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!event.getCrafting().is(ModItems.BROWN_SWORD.get())) {
            return;
        }
        Level level = player.level();
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt != null) {
            bolt.moveTo(player.getX(), player.getY(), player.getZ());
            bolt.setVisualOnly(true);
            level.addFreshEntity(bolt);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 2.5F, 0.85F);
        Advancements.grant(player, Advancements.LEGENDARY_SWORD);
    }

    private static void keepFlashlight(ServerPlayer player) {
        if (!player.getPersistentData().getBoolean(FlashlightPacket.TAG)) {
            return;
        }
        MobEffectInstance current = player.getEffect(MobEffects.NIGHT_VISION);
        if (current == null || current.getDuration() < 400) {
            player.addEffect(FlashlightPacket.nightVision());
        }
    }

    private static boolean looksAtArasFromAfar(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return false;
        }
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        for (ArasEntity aras : level.getEntitiesOfClass(ArasEntity.class, player.getBoundingBox().inflate(115.0D))) {
            double distance = aras.distanceTo(player);
            if (distance < 32.0D || distance > 108.0D) {
                continue;
            }
            Vec3 toEntity = new Vec3(aras.getX() - eye.x, (aras.getY() + 1.7D) - eye.y, aras.getZ() - eye.z).normalize();
            if (look.dot(toEntity) > 0.975D && player.hasLineOfSight(aras)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Black zones, knocks on the door of the player's home and the whisper from behind.
     */
    private static void ambientHorror(ServerPlayer player) {
        if (player.isCreative() || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        if ((player.tickCount & 15) != 0) {
            return;
        }

        if (DarkZone.isDark(level, player.blockPosition())) {
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 300, 0, true, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 340, 0, true, false, false));
            if (RANDOM.nextInt(3) == 0) {
                playRandomAround(level, player, ModSounds.STEP.get(), 3.0D, 8.0D, 0.9F, 0.55F);
            }
            if (RANDOM.nextInt(9) == 0) {
                playRandomAround(level, player, ModSounds.WHISPER.get(), 4.0D, 10.0D, 1.2F, 0.7F);
            }
            if (RANDOM.nextInt(22) == 0) {
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 90 + RANDOM.nextInt(80), 0, true, false, false));
                AngerManager.add(player, 3);
            }
        }

        if (!level.isNight()) {
            return;
        }

        BlockPos home = player.getRespawnPosition();
        if (home == null) {
            return;
        }
        double distance = player.position().distanceTo(Vec3.atCenterOf(home));
        if (distance < 64.0D && RANDOM.nextInt(16) == 0) {
            queueKnocks(level, home);
        } else if (distance > 90.0D && RANDOM.nextInt(14) == 0) {
            speakBehind(player, level);
        }
    }

    private static void playRandomAround(ServerLevel level, ServerPlayer player, SoundEvent sound,
                                         double min, double max, float volume, float pitch) {
        double angle = RANDOM.nextDouble() * Math.PI * 2.0D;
        double distance = min + RANDOM.nextDouble() * (max - min);
        double x = player.getX() + Math.cos(angle) * distance;
        double z = player.getZ() + Math.sin(angle) * distance;
        double y = player.getY() + (RANDOM.nextDouble() - 0.2D) * 1.5D;
        level.playSound(null, x, y, z, sound, SoundSource.AMBIENT, volume, pitch + RANDOM.nextFloat() * 0.25F);
    }

    private static void queueKnocks(ServerLevel level, BlockPos home) {
        int base = level.getServer().getTickCount();
        Vec3 pos = Vec3.atCenterOf(home);
        for (int i = 0; i < 3; i++) {
            PENDING.add(new PendingSound(level, pos, base + 1 + i * 9,
                    ModSounds.KNOCK.get(), 1.6F, 0.84F + i * 0.03F));
        }
    }

    private static void speakBehind(ServerPlayer player, ServerLevel level) {
        Vec3 look = player.getViewVector(1.0F);
        Vec3 behind = player.position().subtract(look.scale(2.2D)).add(0.0D, 1.5D, 0.0D);
        level.playSound(null, behind.x, behind.y, behind.z,
                ModSounds.WHISPER.get(), SoundSource.HOSTILE, 1.8F, 0.6F);
        player.displayClientMessage(Component.translatable("arkandayim.behind")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        syncTimer++;
        boolean doSync = syncTimer % 20 == 0;

        if (!PENDING.isEmpty()) {
            int now = server.getTickCount();
            for (Iterator<PendingSound> iterator = PENDING.iterator(); iterator.hasNext(); ) {
                PendingSound pending = iterator.next();
                if (now >= pending.tick()) {
                    pending.level().playSound(null, pending.pos().x, pending.pos().y, pending.pos().z,
                            pending.sound(), SoundSource.BLOCKS, pending.volume(), pending.pitch());
                    iterator.remove();
                }
            }
        }

        for (ServerLevel level : server.getAllLevels()) {
            for (ServerPlayer player : level.players()) {
                if (doSync) {
                    AngerManager.sync(player);
                }
                if (!AngerManager.isShattered(player.getUUID()) || !level.isNight()
                        || !ArkandayimData.get(level).isChaseEnabled()) {
                    continue;
                }
                if (player.tickCount % 40 == 0) {
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            ModSounds.HEARTBEAT.get(), SoundSource.PLAYERS, 1.1F, 1.0F);
                }
                if (hasActiveChase(level, player)) {
                    continue;
                }
                startNightChase(level, player);
            }
        }
    }

    private static boolean hasActiveChase(ServerLevel level, ServerPlayer player) {
        for (ArasEntity aras : level.getEntitiesOfClass(ArasEntity.class, player.getBoundingBox().inflate(200.0D))) {
            if (aras.isLocking()) {
                return true;
            }
        }
        return false;
    }

    private static void startNightChase(ServerLevel level, ServerPlayer player) {
        ArasEntity aras = null;
        for (ArasEntity existing : level.getEntitiesOfClass(ArasEntity.class, player.getBoundingBox().inflate(160.0D))) {
            if (existing.getState() == ArasEntity.STATE_CALM) {
                aras = existing;
                break;
            }
        }
        Vec3 look = player.getViewVector(1.0F);
        double x = player.getX() + look.x * 18.0D;
        double z = player.getZ() + look.z * 18.0D;
        BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                new BlockPos(Mth.floor(x), 0, Mth.floor(z)));

        if (aras == null) {
            aras = ModEntities.ARAS.get().create(level);
            if (aras == null) {
                return;
            }
            aras.moveTo(x, surface.getY(), z, player.getYRot() + 180.0F, 0.0F);
            level.addFreshEntity(aras);
        } else {
            aras.teleportTo(x, surface.getY(), z);
        }
        aras.beginNightChase(player);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDERMAN_SCREAM, SoundSource.HOSTILE, 2.5F, 0.35F);
    }
}
