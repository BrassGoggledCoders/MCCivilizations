package xyz.brassgoggledcoders.mccivilizations.api.claim;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;

import java.util.Collection;

public interface ILandClaimRepository {
    boolean isClaimed(ResourceKey<Level> level, ChunkPos chunkPos);

    @Nullable
    Civilization getClaimOwner(ResourceKey<Level> level, ChunkPos chunkPos);

    void addClaim(Civilization civilization, ResourceKey<Level> level, ChunkPos chunkPos);

    void addClaims(Civilization civilization, ResourceKey<Level> level, Collection<ChunkPos> chunkPosList);

    void removeClaim(Civilization civilization, ResourceKey<Level> level, ChunkPos chunkPos);

    void removeClaims(Civilization civilization, ResourceKey<Level> level, Collection<ChunkPos> chunkPosList);

    void transferClaims(Civilization fromCivilization, Civilization toCivilization);
}
