package xyz.brassgoggledcoders.mccivilizations.claim;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
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
        this.civilizations = civilizations;
        this.claimsByPos = new HashMap<>();
        this.claimsByOwner = HashMultimap.create();
    }

    public LandClaimRepository(ICivilizationRepository civilizations) {
        this(civilizations);
        ListTag claimListTag = compoundTag.getList("Claims", Tag.TAG_COMPOUND);
        for (int i = 0; i < claimListTag.size(); i++) {
            CompoundTag civilizationClaims = claimListTag.getCompound(i);
            UUID civilizationId = civilizationClaims.getUUID("Civilization");
            ListTag claimsList = civilizationClaims.getList("Claims", Tag.TAG_COMPOUND);
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
            this.setDirty(true);
        }

    }

    private Civilization getCivilization(UUID uuid) {
        return this.civilizations.getCivilizationById(uuid);
    }

    @Override
    @NotNull
    public CompoundTag save(@NotNull CompoundTag pCompoundTag) {
        ListTag claimListTag = new ListTag();
        for (Map.Entry<UUID, Collection<ChunkPos>> entry : this.claimsByOwner.asMap().entrySet()) {
            ListTag listTag = new ListTag();
            for (ChunkPos chunkPos : entry.getValue()) {
                CompoundTag chunkNBT = new CompoundTag();
                chunkNBT.putInt("X", chunkPos.x);
                chunkNBT.putInt("Z", chunkPos.z);
                listTag.add(chunkNBT);
            }
            CompoundTag claimTag = new CompoundTag();
            claimTag.putUUID("Civilization", entry.getKey());
            claimTag.put("Claims", listTag);
        }
        pCompoundTag.put("Claims", claimListTag);
        return pCompoundTag;
    }

    @Override
    public Map<String, CompoundTag> getSerializedValuesToSave() {
        return null;
    }

    @Override
    public void deserializeAndInsertValue(CompoundTag tag) {

    }
}
