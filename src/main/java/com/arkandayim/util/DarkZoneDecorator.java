package com.arkandayim.util;

import com.arkandayim.ArkandayimMod;
import com.arkandayim.registry.ModBlocks;
import com.arkandayim.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.List;

/**
 * The dark biome already exists in the world. When a chunk that belongs to a blob is
 * loaded for the first time its biome data is set to {@code arkandayim:black_forest} and
 * the terrain is converted: black soil, three stone tones, burnt leaves on the old trees,
 * bare trunks, an occasional lava pool and now and then a ruin with a chest.
 */
@Mod.EventBusSubscriber(modid = ArkandayimMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DarkZoneDecorator {

    private static final ResourceLocation BIOME_ID = new ResourceLocation(ArkandayimMod.MODID, "black_forest");
    private static final int COLUMNS_PER_TICK = 900;

    private static final List<Job> JOBS = new ArrayList<>();
    private static final List<Job> NEW_JOBS = new ArrayList<>();

    // ------------------------------------------------------------------- chunks

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (level.dimension() != Level.OVERWORLD) {
            return;
        }
        if (!(event.getChunk() instanceof LevelChunk chunk)) {
            return;
        }
        ChunkPos pos = chunk.getPos();
        int centreX = pos.getMinBlockX() + 8;
        int centreZ = pos.getMinBlockZ() + 8;
        if (!DarkZone.isDark(level, centreX, centreZ)) {
            return;
        }
        long key = chunkKey(pos.x, pos.z);
        if (ArkandayimData.get(level).isDecorated(key) || hasJob(key)) {
            return;
        }
        applyBiome(level, chunk);
        NEW_JOBS.add(new Job(level, key, pos));
    }

    /** The chunk pretends that the multi noise source put the dark biome here. */
    private static void applyBiome(ServerLevel level, LevelChunk chunk) {
        Holder<Biome> holder = level.registryAccess()
                .registryOrThrow(Registries.BIOME)
                .getHolderOrThrow(ResourceKey.create(Registries.BIOME, BIOME_ID));
        chunk.fillBiomesFromNoise((quartX, quartY, quartZ, sampler) -> holder,
                level.getChunkSource().randomState().sampler());
        chunk.setUnsaved(true);
    }

    private static boolean hasJob(long key) {
        for (Job job : JOBS) {
            if (job.key == key) {
                return true;
            }
        }
        for (Job job : NEW_JOBS) {
            if (job.key == key) {
                return true;
            }
        }
        return false;
    }

    private static long chunkKey(int x, int z) {
        return ((long) x << 32) ^ (z & 0xFFFFFFFFL);
    }

    // --------------------------------------------------------------------- jobs

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!NEW_JOBS.isEmpty()) {
            JOBS.addAll(NEW_JOBS);
            NEW_JOBS.clear();
        }
        if (JOBS.isEmpty()) {
            return;
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        int budget = COLUMNS_PER_TICK;
        for (int i = 0; i < JOBS.size() && budget > 0; i++) {
            Job job = JOBS.get(i);
            budget = job.advance(budget);
            if (job.done()) {
                JOBS.remove(i);
                i--;
            }
        }
    }

    private static final class Job {

        private final ServerLevel level;
        private final long key;
        private final ChunkPos pos;
        private final RandomSource random;
        private int index;
        private boolean featuresDone;
        private boolean done;

        private Job(ServerLevel level, long key, ChunkPos pos) {
            this.level = level;
            this.key = key;
            this.pos = pos;
            this.random = RandomSource.create(key);
        }

        private boolean done() {
            return this.done;
        }

        private int advance(int budget) {
            int local = budget;
            int x0 = this.pos.getMinBlockX();
            int z0 = this.pos.getMinBlockZ();
            while (this.index < 256 && local > 0) {
                int x = x0 + (this.index & 15);
                int z = z0 + (this.index >> 4);
                this.index++;
                local--;
                if (!DarkZone.isDark(this.level, x, z)) {
                    continue;
                }
                decorateColumn(this.level, x, z, this.random);
            }
            if (this.index < 256) {
                return local;
            }
            if (!this.featuresDone) {
                this.featuresDone = true;
                placeFeatures(this.level, this.pos, this.random);
            }
            ArkandayimData.get(this.level).markDecorated(this.key);
            this.done = true;
            return local;
        }
    }

    // ------------------------------------------------------------------- ground

    private static void decorateColumn(ServerLevel level, int x, int z, RandomSource random) {
        BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z));
        int y = top.getY();

        // Everything a normal world grew here turns black or burns away.
        for (int dy = 0; dy <= 26; dy++) {
            BlockPos pos = new BlockPos(x, y + dy, z);
            BlockState state = level.getBlockState(pos);
            if (state.is(BlockTags.LEAVES)) {
                level.setBlock(pos, ModBlocks.BLACK_LEAVES.get().defaultBlockState(), 2);
            } else if (state.is(BlockTags.LOGS)) {
                level.setBlock(pos, ModBlocks.BLACK_LOG.get().defaultBlockState(), 2);
            } else if (state.is(BlockTags.FLOWERS) || state.is(BlockTags.REPLACEABLE_BY_TREES)
                    || state.is(Blocks.GRASS) || state.is(Blocks.TALL_GRASS) || state.is(Blocks.FERN)
                    || state.is(Blocks.SWEET_BERRY_BUSH)) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
            }
        }

        BlockPos ground = new BlockPos(x, y - 1, z);
        if (!isNaturalSoil(level.getBlockState(ground))) {
            return;
        }
        long hash = positionHash(x, z);
        level.setBlock(ground, (hash % 9L == 0L ? ModBlocks.ASH_BLOCK.get() : ModBlocks.BLACK_DIRT.get())
                .defaultBlockState(), 2);
        for (int depth = 1; depth <= 3; depth++) {
            BlockPos pos = ground.below(depth);
            if (isNaturalSoil(level.getBlockState(pos))) {
                level.setBlock(pos, ModBlocks.BLACK_DIRT.get().defaultBlockState(), 2);
            }
        }
        for (int depth = 4; depth <= 9; depth++) {
            BlockPos pos = ground.below(depth);
            BlockState state = level.getBlockState(pos);
            if (!state.is(BlockTags.BASE_STONE_OVERWORLD) && !isNaturalSoil(state)) {
                continue;
            }
            if ((hash >> (depth * 5)) % 47L == 0L) {
                level.setBlock(pos, ModBlocks.BROWN_ORE.get().defaultBlockState(), 2);
            } else {
                long tone = (hash >> (depth * 3)) % 10L;
                level.setBlock(pos, (tone < 6L ? ModBlocks.BLACK_STONE.get() : ModBlocks.DARK_STONE.get())
                        .defaultBlockState(), 2);
            }
        }
    }

    private static boolean isNaturalSoil(BlockState state) {
        return state.is(BlockTags.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.PODZOL)
                || state.is(Blocks.MYCELIUM) || state.is(Blocks.SAND) || state.is(Blocks.GRAVEL)
                || state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.CLAY) || state.is(BlockTags.BASE_STONE_OVERWORLD)
                || state.is(Blocks.TERRACOTTA);
    }

    // ----------------------------------------------------------------- features

    private static void placeFeatures(ServerLevel level, ChunkPos pos, RandomSource random) {
        int x0 = pos.getMinBlockX();
        int z0 = pos.getMinBlockZ();
        int centreX = x0 + 8;
        int centreZ = z0 + 8;

        int trees = 1 + random.nextInt(2);
        for (int i = 0; i < trees; i++) {
            int x = x0 + random.nextInt(16);
            int z = z0 + random.nextInt(16);
            if (DarkZone.isDark(level, x, z)) {
                placeDarkTree(level, x, z, random);
            }
        }

        BlockPos zoneCentre = DarkZone.nearestDarkCentre(level, centreX, centreZ, 1);
        if (zoneCentre != null && Math.abs(zoneCentre.getX() - centreX) < 26
                && Math.abs(zoneCentre.getZ() - centreZ) < 26) {
            placeLavaPool(level, centreX + random.nextInt(9) - 4, centreZ + random.nextInt(9) - 4, random);
        }

        if (zoneCentre != null && random.nextInt(9) == 0) {
            int x = x0 + random.nextInt(16);
            int z = z0 + random.nextInt(16);
            if (DarkZone.isDark(level, x, z)) {
                placeRuin(level, x, z, random);
            }
        }
    }

    private static int surfaceY(ServerLevel level, int x, int z) {
        return level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z)).getY();
    }

    /** A black trunk, sometimes bare and sometimes carrying burnt leaves. */
    private static void placeDarkTree(ServerLevel level, int x, int z, RandomSource random) {
        int y = surfaceY(level, x, z);
        if (!level.getBlockState(new BlockPos(x, y - 1, z)).is(ModBlocks.BLACK_DIRT.get())) {
            return;
        }
        int height = 4 + random.nextInt(4);
        for (int i = 0; i < height; i++) {
            level.setBlock(new BlockPos(x, y + i, z), ModBlocks.BLACK_LOG.get().defaultBlockState(), 2);
        }
        if (random.nextInt(10) < 6) {
            int radius = 2;
            for (int dy = height - 3; dy <= height; dy++) {
                int ring = dy >= height ? 1 : radius;
                for (int dx = -ring; dx <= ring; dx++) {
                    for (int dz = -ring; dz <= ring; dz++) {
                        if (dx * dx + dz * dz > ring * ring) {
                            continue;
                        }
                        BlockPos leaf = new BlockPos(x + dx, y + dy, z + dz);
                        if (level.getBlockState(leaf).isAir()) {
                            level.setBlock(leaf, ModBlocks.BLACK_LEAVES.get().defaultBlockState(), 2);
                        }
                    }
                }
            }
        }
        int branches = 2 + random.nextInt(2);
        for (int b = 0; b < branches; b++) {
            Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            int startY = y + 2 + random.nextInt(Math.max(1, height - 2));
            int length = 2 + random.nextInt(2);
            for (int step = 1; step <= length; step++) {
                BlockPos branch = new BlockPos(x + direction.getStepX() * step,
                        startY + (step > 1 ? 1 : 0), z + direction.getStepZ() * step);
                if (level.getBlockState(branch).isAir()) {
                    level.setBlock(branch, ModBlocks.BLACK_LOG.get().defaultBlockState(), 2);
                }
            }
        }
    }

    private static void placeLavaPool(ServerLevel level, int x, int z, RandomSource random) {
        int y = surfaceY(level, x, z);
        int cells = 0;
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance > 3.2D) {
                    continue;
                }
                BlockPos rim = new BlockPos(x + dx, y - 1, z + dz);
                if (distance > 2.2D) {
                    level.setBlock(rim, ModBlocks.BLACK_STONE.get().defaultBlockState(), 2);
                    continue;
                }
                level.setBlock(rim, Blocks.LAVA.defaultBlockState(), 2);
                level.setBlock(rim.below(), Blocks.LAVA.defaultBlockState(), 2);
                level.setBlock(rim.below(2), ModBlocks.BLACK_STONE.get().defaultBlockState(), 2);
                for (int up = 0; up < 6; up++) {
                    BlockPos above = rim.above(up);
                    if (!level.getBlockState(above).isAir()) {
                        level.setBlock(above, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
                cells++;
            }
        }
        if (cells > 0) {
            level.playSound(null, x, y, z, net.minecraft.sounds.SoundEvents.LAVA_AMBIENT,
                    net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 0.7F);
        }
    }

    private static void placeRuin(ServerLevel level, int x, int z, RandomSource random) {
        int baseY = surfaceY(level, x, z);
        int half = 3;
        for (int dx = -half; dx <= half; dx++) {
            for (int dz = -half; dz <= half; dz++) {
                BlockPos floor = new BlockPos(x + dx, baseY - 1, z + dz);
                level.setBlock(floor, ModBlocks.BLACK_PLANKS.get().defaultBlockState(), 2);
                for (int up = 0; up < 5; up++) {
                    level.setBlock(new BlockPos(x + dx, baseY + up, z + dz), Blocks.AIR.defaultBlockState(), 2);
                }
                boolean wall = Math.abs(dx) == half || Math.abs(dz) == half;
                if (!wall) {
                    continue;
                }
                for (int up = 0; up < 3; up++) {
                    boolean door = dz == -half && Math.abs(dx) <= 1 && up < 2;
                    if (door || random.nextInt(10) == 0) {
                        continue;
                    }
                    level.setBlock(new BlockPos(x + dx, baseY + up, z + dz),
                            ModBlocks.BLACK_PLANKS.get().defaultBlockState(), 2);
                }
                if (random.nextInt(3) == 0) {
                    level.setBlock(new BlockPos(x + dx, baseY + 3, z + dz),
                            ModBlocks.BLACK_PLANKS.get().defaultBlockState(), 2);
                }
            }
        }
        placeLootChest(level, new BlockPos(x + 2, baseY, z + 2), random, true);
        if (random.nextInt(3) == 0) {
            placeLootChest(level, new BlockPos(x - 2, baseY, z + 1), random, false);
        }
    }

    private static void placeLootChest(ServerLevel level, BlockPos pos, RandomSource random, boolean guaranteedOre) {
        level.setBlock(pos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH), 2);
        if (!(level.getBlockEntity(pos) instanceof ChestBlockEntity chest)) {
            return;
        }
        chest.setItem(0, new ItemStack(ModItems.BROWN_INGOT.get(), 1));
        int slot = 1;
        if (guaranteedOre || random.nextBoolean()) {
            chest.setItem(slot++, new ItemStack(ModBlocks.BROWN_ORE.get().asItem(), 1 + random.nextInt(2)));
        }
        if (random.nextInt(100) < 45) {
            chest.setItem(slot++, new ItemStack(ModItems.NULL_TOTEM.get()));
        }
        if (random.nextInt(3) == 0) {
            chest.setItem(slot++, new ItemStack(Items.BONE, 1 + random.nextInt(3)));
        }
        chest.setChanged();
    }

    private static long positionHash(int x, int z) {
        long value = (long) x * 0x9E3779B97F4A7C15L ^ (long) z * 0xC2B2AE3D27D4EB4FL;
        value = (value ^ (value >>> 33)) * 0xFF51AFD7ED558CCDL;
        value = (value ^ (value >>> 33)) * 0xC4CEB9FE1A85EC53L;
        return value ^ (value >>> 33);
    }
}
