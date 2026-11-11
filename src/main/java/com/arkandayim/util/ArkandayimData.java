package com.arkandayim.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;

/**
 * Small world level bookkeeping: which dark cells were already turned into black
 * terrain, and where the old mansion near spawn stands.
 */
public class ArkandayimData extends SavedData {

    private static final String DATA_NAME = "arkandayim_world";

    private final Set<Long> decoratedCells = new HashSet<>();
    private BlockPos housePos;
    private boolean chaseEnabled = true;

    public static ArkandayimData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(ArkandayimData::load, ArkandayimData::new, DATA_NAME);
    }

    public ArkandayimData() {
    }

    public static ArkandayimData load(CompoundTag tag) {
        ArkandayimData data = new ArkandayimData();
        if (tag.contains("Cells", 12)) {
            for (long value : tag.getLongArray("Cells")) {
                data.decoratedCells.add(value);
            }
        }
        if (tag.contains("HouseX")) {
            data.housePos = new BlockPos(tag.getInt("HouseX"), tag.getInt("HouseY"), tag.getInt("HouseZ"));
        }
        if (tag.contains("ChaseEnabled")) {
            data.chaseEnabled = tag.getBoolean("ChaseEnabled");
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        long[] cells = new long[this.decoratedCells.size()];
        int index = 0;
        for (long value : this.decoratedCells) {
            cells[index++] = value;
        }
        tag.put("Cells", new LongArrayTag(cells));
        if (this.housePos != null) {
            tag.putInt("HouseX", this.housePos.getX());
            tag.putInt("HouseY", this.housePos.getY());
            tag.putInt("HouseZ", this.housePos.getZ());
        }
        tag.putBoolean("ChaseEnabled", this.chaseEnabled);
        return tag;
    }

    public boolean isDecorated(long key) {
        return this.decoratedCells.contains(key);
    }

    public void markDecorated(long key) {
        this.decoratedCells.add(key);
        this.setDirty();
    }

    public BlockPos getHousePos() {
        return this.housePos;
    }

    public void setHousePos(BlockPos pos) {
        this.housePos = pos;
        this.setDirty();
    }

    public boolean isChaseEnabled() {
        return this.chaseEnabled;
    }

    public void setChaseEnabled(boolean enabled) {
        this.chaseEnabled = enabled;
        this.setDirty();
    }
}
