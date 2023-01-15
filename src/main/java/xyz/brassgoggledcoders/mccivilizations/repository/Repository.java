package xyz.brassgoggledcoders.mccivilizations.repository;

import net.minecraft.nbt.CompoundTag;

import java.util.List;
import java.util.Map;

public abstract class Repository<K, V> {
    private final String name;
    private boolean isDirty = false;

    protected Repository(String name) {
        this.name = name;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void setDirty(boolean dirty) {
        isDirty = dirty;
    }

    public abstract Map<String, CompoundTag> getSerializedValuesToSave();

    public abstract void deserializeAndInsertValue(CompoundTag tag);

    public String getName() {
        return this.name;
    }

}
