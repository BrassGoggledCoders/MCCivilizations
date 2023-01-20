package xyz.brassgoggledcoders.mccivilizations.api.claim;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.repositories.ChangeType;

import java.util.Collection;

public class LandClaimChangedEvent extends Event {
    private final Civilization civilization;
    private final ResourceKey<Level> level;
    private final Collection<ChunkPos> chunkPositions;
    private final ChangeType changeType;

    public LandClaimChangedEvent(Civilization civilization, ResourceKey<Level> level, Collection<ChunkPos> chunkPositions, ChangeType changeType) {
        this.civilization = civilization;
        this.level = level;
        this.chunkPositions = chunkPositions;
        this.changeType = changeType;
    }

    public ResourceKey<Level> getLevel() {
        return level;
    }

    public Collection<ChunkPos> getChunkPositions() {
        return chunkPositions;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public Civilization getCivilization() {
        return civilization;
    }
}
