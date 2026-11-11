package com.arkandayim.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/**
 * A handsome quartz mansion standing within a hundred blocks of the world spawn.
 * Nobody lives there anymore, but the black book remembers where it is.
 */
public final class LuxuryHouse {

    private LuxuryHouse() {
    }

    public static BlockPos ensure(ServerLevel level) {
        ArkandayimData data = ArkandayimData.get(level);
        BlockPos existing = data.getHousePos();
        if (existing != null) {
            return existing;
        }
        BlockPos spawn = level.getSharedSpawnPos();
        long seed = level.getSeed();
        double startAngle = Math.floorMod(seed, 6283L) / 1000.0D;
        for (int attempt = 0; attempt < 12; attempt++) {
            double angle = startAngle + attempt * 0.63D;
            double distance = 58.0D + attempt * 3.0D;
            if (distance > 96.0D) {
                distance = 60.0D + (attempt % 5) * 6.0D;
            }
            int x = spawn.getX() + (int) Math.round(Math.cos(angle) * distance);
            int z = spawn.getZ() + (int) Math.round(Math.sin(angle) * distance);
            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
            if (y <= level.getMinBuildHeight() + 4) {
                continue;
            }
            if (!level.getFluidState(new BlockPos(x, y - 1, z)).isEmpty()) {
                continue;
            }
            BlockPos house = new BlockPos(x, y, z);
            build(level, house);
            data.setHousePos(house);
            return house;
        }
        BlockPos fallback = new BlockPos(spawn.getX() + 64,
                level.getHeight(Heightmap.Types.WORLD_SURFACE, spawn.getX() + 64, spawn.getZ()), spawn.getZ());
        build(level, fallback);
        data.setHousePos(fallback);
        return fallback;
    }

    /** Builds a fresh mansion at the given spot (used by the panel). */
    public static BlockPos buildAt(ServerLevel level, BlockPos pos) {
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ());
        BlockPos house = new BlockPos(pos.getX(), y, pos.getZ());
        build(level, house);
        ArkandayimData.get(level).setHousePos(house);
        return house;
    }

    public static boolean contains(BlockPos house, Vec3 pos) {
        if (house == null) {
            return false;
        }
        return pos.x >= house.getX() - 4.6D && pos.x <= house.getX() + 4.6D
                && pos.z >= house.getZ() - 3.6D && pos.z <= house.getZ() + 3.6D
                && pos.y >= house.getY() - 0.5D && pos.y <= house.getY() + 4.2D;
    }

    private static void build(ServerLevel level, BlockPos origin) {
        int x0 = origin.getX();
        int y0 = origin.getY();
        int z0 = origin.getZ();

        for (int dx = -6; dx <= 6; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                for (int dy = -1; dy <= 7; dy++) {
                    level.setBlock(new BlockPos(x0 + dx, y0 + dy, z0 + dz), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                level.setBlock(new BlockPos(x0 + dx, y0 - 1, z0 + dz),
                        Blocks.SMOOTH_QUARTZ.defaultBlockState(), 2);
                level.setBlock(new BlockPos(x0 + dx, y0 - 2, z0 + dz),
                        Blocks.QUARTZ_BLOCK.defaultBlockState(), 2);
            }
        }

        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                boolean edge = Math.abs(dx) == 5 || Math.abs(dz) == 4;
                if (!edge) {
                    continue;
                }
                for (int up = 0; up < 4; up++) {
                    boolean door = dz == -4 && dx == 0 && up < 2;
                    if (door) {
                        continue;
                    }
                    boolean window = up == 2 && ((Math.abs(dx) == 5 && Math.abs(dz) <= 1)
                            || (Math.abs(dz) == 4 && Math.abs(dx) == 3));
                    boolean corner = Math.abs(dx) == 5 && Math.abs(dz) == 4;
                    if (window && !corner) {
                        level.setBlock(new BlockPos(x0 + dx, y0 + up, z0 + dz),
                                Blocks.GLASS_PANE.defaultBlockState(), 2);
                    } else {
                        level.setBlock(new BlockPos(x0 + dx, y0 + up, z0 + dz),
                                Blocks.QUARTZ_BLOCK.defaultBlockState(), 2);
                    }
                }
            }
        }

        for (int dx = -6; dx <= 6; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                level.setBlock(new BlockPos(x0 + dx, y0 + 4, z0 + dz),
                        Blocks.DARK_OAK_PLANKS.defaultBlockState(), 2);
            }
        }
        for (int dx = -6; dx <= 6; dx++) {
            level.setBlock(new BlockPos(x0 + dx, y0 + 3, z0 + 6), Blocks.AIR.defaultBlockState(), 2);
        }
        for (int dx = -4; dx <= 4; dx += 2) {
            for (int dz = -3; dz <= 3; dz += 2) {
                level.setBlock(new BlockPos(x0 + dx, y0 + 5, z0 + dz),
                        Blocks.QUARTZ_BLOCK.defaultBlockState(), 2);
            }
        }
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                level.setBlock(new BlockPos(x0 + dx, y0 + 6, z0 + dz),
                        Blocks.DARK_OAK_PLANKS.defaultBlockState(), 2);
            }
        }

        // doorway frame and a small porch
        level.setBlock(new BlockPos(x0, y0 + 3, z0 - 4), Blocks.DARK_OAK_PLANKS.defaultBlockState(), 2);
        for (int dx = -1; dx <= 1; dx++) {
            level.setBlock(new BlockPos(x0 + dx, y0 - 1, z0 - 5), Blocks.SMOOTH_QUARTZ.defaultBlockState(), 2);
            level.setBlock(new BlockPos(x0 + dx, y0, z0 - 5), Blocks.AIR.defaultBlockState(), 2);
        }

        // interior
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                level.setBlock(new BlockPos(x0 + dx, y0, z0 + dz), Blocks.RED_CARPET.defaultBlockState(), 2);
            }
        }
        level.setBlock(new BlockPos(x0 - 4, y0, z0 - 3), Blocks.BOOKSHELF.defaultBlockState(), 2);
        level.setBlock(new BlockPos(x0 - 4, y0 + 1, z0 - 3), Blocks.BOOKSHELF.defaultBlockState(), 2);
        level.setBlock(new BlockPos(x0 + 4, y0, z0 + 3), Blocks.BOOKSHELF.defaultBlockState(), 2);
        level.setBlock(new BlockPos(x0 + 3, y0, z0 - 3), Blocks.CRAFTING_TABLE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(x0 - 3, y0, z0 + 3), Blocks.CHEST.defaultBlockState()
                .setValue(net.minecraft.world.level.block.ChestBlock.FACING, Direction.NORTH), 2);
        if (level.getBlockEntity(new BlockPos(x0 - 3, y0, z0 + 3))
                instanceof net.minecraft.world.level.block.entity.ChestBlockEntity chest) {
            chest.setItem(0, new net.minecraft.world.item.ItemStack(Blocks.TORCH, 8));
            chest.setItem(1, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GOLDEN_APPLE, 1));
            chest.setChanged();
        }
        level.setBlock(new BlockPos(x0, y0 + 3, z0), Blocks.SEA_LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(x0 - 3, y0 + 3, z0 - 2), Blocks.SEA_LANTERN.defaultBlockState(), 2);
        level.setBlock(new BlockPos(x0 + 3, y0 + 3, z0 + 2), Blocks.SEA_LANTERN.defaultBlockState(), 2);
    }
}
