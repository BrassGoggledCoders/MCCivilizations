package xyz.brassgoggledcoders.mccivilizations.api.claim;

import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;

public interface ILandClaimRepository {
    boolean isClaimed(ChunkPos chunkPos);

    @Nullable
    Civilization getClaimOwner(ChunkPos chunkPos);

    void addClaim(Civilization civilization, ChunkPos chunkPos);
}
