package xyz.brassgoggledcoders.mccivilizations.api.claim;

import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;

import java.util.List;
import java.util.UUID;

public interface ILandClaimRepository {
    boolean isClaimed(ChunkPos chunkPos);

    @Nullable
    Civilization getClaimOwner(ChunkPos chunkPos);

    void addClaim(Civilization civilization, ChunkPos chunkPos);

    void addClaims(Civilization civilization, List<ChunkPos> chunkPosList);

    void setClaims(UUID civilizationId, List<ChunkPos> chunkPosList);

    void removeClaim(Civilization civilization, ChunkPos chunkPos);

    void removeClaims(Civilization civilization, List<ChunkPos> chunkPosList);
}
