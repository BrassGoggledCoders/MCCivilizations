package xyz.brassgoggledcoders.mccivilizations.claim;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.ICivilizations;
import xyz.brassgoggledcoders.mccivilizations.api.claim.IClaimedLand;
import xyz.brassgoggledcoders.mccivilizations.api.service.CivilizationServices;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class ClaimedLandSavedData extends SavedData implements IClaimedLand {
    private final Map<ChunkPos, UUID> claimsByPos;
    private final Multimap<UUID, ChunkPos> claimsByOwner;
    private final ICivilizations civilizations;

    public ClaimedLandSavedData(ICivilizations civilizations) {
        this.civilizations = civilizations;
        this.claimsByPos = new HashMap<>();
        this.claimsByOwner = HashMultimap.create();
    }

    public ClaimedLandSavedData(ICivilizations civilizations, CompoundTag compoundTag) {
        this(civilizations);
    }

    @Override
    public boolean isClaimed(ChunkPos chunkPos) {
        return false;
    }

    @Override
    @Nullable
    public Civilization getClaimOwner(ChunkPos chunkPos) {
        return Optional.ofNullable(claimsByPos.get(chunkPos))
                .map(this::getCivilization)
                .orElse(null);
    }

    @Override
    public void addClaim(Civilization civilization, ChunkPos chunkPos) {
        this.claimsByPos.put(chunkPos, civilization.getId());
    }

    private Civilization getCivilization(UUID uuid) {
        return this.civilizations.getCivilizationById(uuid);
    }

    @Override
    @NotNull
    public CompoundTag save(@NotNull CompoundTag pCompoundTag) {
        return pCompoundTag;
    }
}
