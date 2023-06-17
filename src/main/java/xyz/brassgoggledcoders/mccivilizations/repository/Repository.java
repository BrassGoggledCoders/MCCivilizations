package xyz.brassgoggledcoders.mccivilizations.repository;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class Repository {
    private final String name;
    private final List<UUID> dirtyIds;

    protected Repository(String name) {
        this.name = name;
        this.dirtyIds = new ArrayList<>();
    }

    public abstract Collection<UUID> getIds();

    public boolean isDirty() {
        return !dirtyIds.isEmpty();
    }

    public void addDirtyId(UUID dirty) {
        this.dirtyIds.add(dirty);
    }

    public List<UUID> getDirtyIds() {
        return this.dirtyIds;
    }

    public void clearDirtyIds() {
        this.dirtyIds.clear();
    }

    @Nullable
    public abstract CompoundTag getSerializedValue(UUID id);

    public abstract void deserializeAndInsertValue(@NotNull CompoundTag tag);

    public String getName() {
        return this.name;
    }

    public void onPlayerJoin(ServerPlayer serverPlayer) {

    }
}
