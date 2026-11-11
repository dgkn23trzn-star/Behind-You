package com.arkandayim.item;

import com.arkandayim.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Shared helpers for the extra right click behaviour of the brown tools.
 */
public final class ToolAbilities {

    private ToolAbilities() {
    }

    public static BlockHitResult look(Level level, Player player, double reach) {
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(reach));
        return level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
    }

    /** The block under the crosshair, or the player's own feet when nothing is hit. */
    public static BlockPos lookPos(Level level, Player player, double reach) {
        BlockHitResult hit = look(level, player, reach);
        return hit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK
                ? hit.getBlockPos() : player.blockPosition();
    }

    public static boolean isOre(BlockState state) {
        return state.is(ModBlocks.BROWN_ORE.get()) || state.is(BlockTags.COPPER_ORES)
                || state.is(BlockTags.IRON_ORES) || state.is(BlockTags.GOLD_ORES)
                || state.is(BlockTags.DIAMOND_ORES) || state.is(BlockTags.REDSTONE_ORES)
                || state.is(BlockTags.LAPIS_ORES) || state.is(BlockTags.EMERALD_ORES)
                || state.is(BlockTags.COAL_ORES);
    }

    /** Counts and glints every ore around the block the player is looking at. */
    public static int pulseOres(Level level, BlockPos origin) {
        int found = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -10; dx <= 10; dx++) {
            for (int dy = -4; dy <= 4; dy++) {
                for (int dz = -10; dz <= 10; dz++) {
                    cursor.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    BlockState state = level.getBlockState(cursor);
                    if (!isOre(state)) {
                        continue;
                    }
                    found++;
                    if (found <= 40 && level instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.END_ROD,
                                cursor.getX() + 0.5D, cursor.getY() + 0.5D, cursor.getZ() + 0.5D,
                                2, 0.25D, 0.25D, 0.25D, 0.01D);
                    }
                }
            }
        }
        return found;
    }

    /**
     * Breaks the exposed ores around the aim point, up to a small limit. The drops never
     * hit the ground: they land in the player's inventory, and only spill out when the
     * bag is already full.
     */
    public static int mineOres(Level level, Player player, BlockPos centre, int radius, int limit) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return 0;
        }
        int broken = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius && broken < limit; dx++) {
            for (int dy = -radius; dy <= radius && broken < limit; dy++) {
                for (int dz = -radius; dz <= radius && broken < limit; dz++) {
                    cursor.set(centre.getX() + dx, centre.getY() + dy, centre.getZ() + dz);
                    BlockState state = level.getBlockState(cursor);
                    if (!isOre(state)) {
                        continue;
                    }
                    BlockPos target = cursor.immutable();
                    List<ItemStack> drops = Block.getDrops(state, serverLevel, target,
                            serverLevel.getBlockEntity(target), player, player.getMainHandItem());
                    if (serverLevel.destroyBlock(target, false, player)) {
                        broken++;
                        for (ItemStack drop : drops) {
                            if (!drop.isEmpty() && !player.getInventory().add(drop)) {
                                player.drop(drop, false);
                            }
                        }
                        serverLevel.sendParticles(ParticleTypes.END_ROD,
                                target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D,
                                3, 0.3D, 0.3D, 0.3D, 0.01D);
                    }
                }
            }
        }
        return broken;
    }

    public static int digScoop(Level level, Player player, BlockPos centre, int layers) {
        int broken = 0;
        for (int dy = 0; dy < Math.max(1, layers); dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos pos = centre.offset(dx, -dy, dz);
                    BlockState state = level.getBlockState(pos);
                    if (!isSoftGround(state) || state.getDestroySpeed(level, pos) < 0.0F) {
                        continue;
                    }
                    if (level.destroyBlock(pos, true, player)) {
                        broken++;
                    }
                }
            }
        }
        return broken;
    }

    private static boolean isSoftGround(BlockState state) {
        return state.is(BlockTags.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.SAND)
                || state.is(Blocks.GRAVEL) || state.is(Blocks.CLAY) || state.is(Blocks.SOUL_SAND)
                || state.is(Blocks.SNOW_BLOCK) || state.is(ModBlocks.BLACK_DIRT.get());
    }

    public static int fellTree(Level level, Player player, BlockPos base) {
        int broken = 0;
        for (int up = 0; up < 64 && broken < 64; up++) {
            BlockPos pos = base.above(up);
            BlockState state = level.getBlockState(pos);
            if (!state.is(BlockTags.LOGS)) {
                break;
            }
            if (level.destroyBlock(pos, true, player)) {
                broken++;
            }
        }
        if (broken > 0) {
            for (int y = 0; y <= 42; y++) {
                for (int dx = -3; dx <= 3; dx++) {
                    for (int dz = -3; dz <= 3; dz++) {
                        BlockPos leaf = base.offset(dx, y, dz);
                        if (level.getBlockState(leaf).is(BlockTags.LEAVES)) {
                            level.destroyBlock(leaf, true, player);
                        }
                    }
                }
            }
        }
        return broken;
    }

    public static int corruptGround(Level level, BlockPos centre, int radius) {
        int changed = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                BlockPos pos = centre.offset(dx, 0, dz);
                BlockState state = level.getBlockState(pos);
                if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.PODZOL)
                        || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.MYCELIUM) || state.is(Blocks.MOSS_BLOCK)) {
                    level.setBlock(pos, ModBlocks.BLACK_DIRT.get().defaultBlockState(), 3);
                    changed++;
                }
            }
        }
        return changed;
    }
}
