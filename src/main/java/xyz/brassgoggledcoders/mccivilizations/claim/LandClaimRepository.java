package xyz.brassgoggledcoders.mccivilizations.claim;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.ICivilizationRepository;
import xyz.brassgoggledcoders.mccivilizations.api.claim.ILandClaimRepository;
import xyz.brassgoggledcoders.mccivilizations.repository.Repository;

import java.util.*;

public class LandClaimRepository extends Repository implements ILandClaimRepository {
    private final Map<ChunkPos, UUID> claimsByPos;
    private final Multimap<UUID, ChunkPos> claimsByOwner;
    private final ICivilizationRepository civilizations;

    public LandClaimRepository(ICivilizationRepository civilizations) {
        super("land_claims");
        this.civilizations = civilizations;
        this.claimsByPos = new HashMap<>();
        this.claimsByOwner = HashMultimap.create();
    }

    @Override
    public boolean isClaimed(ChunkPos chunkPos) {
        return this.claimsByPos.containsKey(chunkPos);
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
        if (!this.claimsByPos.containsKey(chunkPos)) {
            this.claimsByPos.put(chunkPos, civilization.getId());
            this.claimsByOwner.put(civilization.getId(), chunkPos);
            this.addDirtyId(civilization.getId());
        }

    }

    private Civilization getCivilization(UUID uuid) {
        return this.civilizations.getCivilizationById(uuid);
    }

    @Override
    @NotNull
    public CompoundTag getSerializedValue(UUID id) {
        CompoundTag serializedValue = new CompoundTag();
        ListTag listTag = new ListTag();
        for (ChunkPos chunkPos : this.claimsByOwner.get(id)) {
            CompoundTag chunkNBT = new CompoundTag();
            chunkNBT.putInt("X", chunkPos.x);
            chunkNBT.putInt("Z", chunkPos.z);
            listTag.add(chunkNBT);
        }
        CompoundTag claimTag = new CompoundTag();
        claimTag.putUUID("Civilization", id);
        claimTag.put("Claims", listTag);
        return serializedValue;
    }

    @Override
    public void deserializeAndInsertValue(@NotNull CompoundTag tag) {
        UUID civilizationId = tag.getUUID("Civilization");
        ListTag claimsList = tag.getList("Claims", Tag.TAG_COMPOUND);
        for (int j = 0; j < claimsList.size(); j++) {
            CompoundTag chunkPosTag = claimsList.getCompound(j);
            ChunkPos chunkPos = new ChunkPos(
                    chunkPosTag.getInt("X"),
                    chunkPosTag.getInt("Z")
            );
            this.claimsByOwner.put(civilizationId, chunkPos);
            this.claimsByPos.put(chunkPos, civilizationId);
        }
    }
}
