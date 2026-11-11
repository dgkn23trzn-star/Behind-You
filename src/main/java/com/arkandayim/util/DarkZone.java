package com.arkandayim.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * The dark biome is part of the world from the seed: its blobs are about 160 to 250
 * blocks across, sit in a deterministic grid and are masked by the files in
 * {@code data/arkandayim/worldgen/biome}. Nothing is created because a player walked in,
 * the chunks simply generate as part of the landscape.
 */
public final class DarkZone {

    /** Spacing of the grid that holds one possible blob per cell. */
    public static final int CELL = 320;
    private static final int MIN_RADIUS = 78;
    private static final int RADIUS_SPREAD = 48;

    private DarkZone() {
    }

    public static boolean isDark(Level level, BlockPos pos) {
        return isDark(level, pos.getX(), pos.getZ());
    }

    public static boolean isDark(Level level, int x, int z) {
        long seed = level.dimension().location().hashCode() * 0x9E3779B97F4A7C15L + 0x51ED270BL;
        int cellX = Math.floorDiv(x, CELL);
        int cellZ = Math.floorDiv(z, CELL);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                long hash = cellHash(cellX + dx, cellZ + dz, seed);
                if ((hash & 3L) != 0L) {
                    continue;
                }
                int centreX = (cellX + dx) * CELL + CELL / 2 + (int) ((hash >>> 8) & 63L) - 32;
                int centreZ = (cellZ + dz) * CELL + CELL / 2 + (int) ((hash >>> 16) & 63L) - 32;
                double radius = MIN_RADIUS + (double) ((hash >>> 24) & 63L) / 63.0D * RADIUS_SPREAD;
                double ox = x - centreX;
                double oz = z - centreZ;
                double distance = Math.sqrt(ox * ox + oz * oz);
                double angle = Math.atan2(oz, ox);
                double wobble = 1.0D
                        + 0.15D * Math.sin(angle * 3.0D + (double) ((hash >>> 40) & 7L))
                        + 0.08D * Math.sin(angle * 5.0D + (double) ((hash >>> 47) & 7L));
                if (distance < radius * wobble) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * The centre of the closest blob, searched outwards in whole grid cells.
     * Returns null when nothing is close enough.
     */
    public static BlockPos nearestDarkCentre(Level level, int x, int z, int maxCells) {
        long seed = level.dimension().location().hashCode() * 0x9E3779B97F4A7C15L + 0x51ED270BL;
        int cellX = Math.floorDiv(x, CELL);
        int cellZ = Math.floorDiv(z, CELL);
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int ring = 0; ring <= maxCells; ring++) {
            for (int dx = -ring; dx <= ring; dx++) {
                for (int dz = -ring; dz <= ring; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != ring) {
                        continue;
                    }
                    long hash = cellHash(cellX + dx, cellZ + dz, seed);
                    if ((hash & 3L) != 0L) {
                        continue;
                    }
                    int centreX = (cellX + dx) * CELL + CELL / 2 + (int) ((hash >>> 8) & 63L) - 32;
                    int centreZ = (cellZ + dz) * CELL + CELL / 2 + (int) ((hash >>> 16) & 63L) - 32;
                    if (!isDark(level, centreX, centreZ)) {
                        continue;
                    }
                    double distance = Math.hypot(centreX - x, centreZ - z);
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        best = new BlockPos(centreX, 0, centreZ);
                    }
                }
            }
        }
        return best;
    }

    private static long cellHash(int cellX, int cellZ, long seed) {
        long value = (long) cellX * 0x9E3779B97F4A7C15L ^ (long) cellZ * 0xC2B2AE3D27D4EB4FL ^ seed;
        value = (value ^ (value >>> 33)) * 0xFF51AFD7ED558CCDL;
        value = (value ^ (value >>> 33)) * 0xC4CEB9FE1A85EC53L;
        return value ^ (value >>> 33);
    }
}
