package xyz.brassgoggledcoders.mccivilizations.claim;

import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.ICivilizations;
import xyz.brassgoggledcoders.mccivilizations.api.claim.IClaimedLand;

public class ClientClaimedLand implements IClaimedLand {
    private final ICivilizations civilizations;

    public ClientClaimedLand(ICivilizations civilizations) {
        this.civilizations = civilizations;
    }

    @Override
    public boolean isClaimed(ChunkPos chunkPos) {
        return false;
    }

    @Override
    @Nullable
    public Civilization getClaimOwner(ChunkPos chunkPos) {
        return null;
    }

    @Override
    public void addClaim(Civilization civilization, ChunkPos chunkPos) {

    }
}
