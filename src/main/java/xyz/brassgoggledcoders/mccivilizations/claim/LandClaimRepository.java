package xyz.brassgoggledcoders.mccivilizations.claim;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.ICivilizationRepository;
import xyz.brassgoggledcoders.mccivilizations.api.claim.ILandClaimRepository;
import xyz.brassgoggledcoders.mccivilizations.api.claim.LandClaimChangedEvent;
import xyz.brassgoggledcoders.mccivilizations.api.repositories.ChangeType;
import xyz.brassgoggledcoders.mccivilizations.network.LandClaimUpdatePacket;
import xyz.brassgoggledcoders.mccivilizations.network.NetworkHandler;
import xyz.brassgoggledcoders.mccivilizations.repository.Repository;

import java.util.*;
import java.util.Map.Entry;

public class LandClaimRepository extends Repository implements ILandClaimRepository {
    private final Table<ResourceKey<Level>, ChunkPos, UUID> claimsByPos;
    private final Table<UUID, ResourceKey<Level>, Collection<ChunkPos>> claimsByOwner;
    private final ICivilizationRepository civilizations;
    private final boolean sync;

    public LandClaimRepository(ICivilizationRepository civilizations, boolean sync) {
        super("land_claims");
        this.civilizations = civilizations;
        this.sync = sync;
        this.claimsByPos = HashBasedTable.create();
        this.claimsByOwner = HashBasedTable.create();
    }

    @Override
    public boolean isClaimed(ResourceKey<Level> level, ChunkPos chunkPos) {
        return this.claimsByPos.contains(level, chunkPos);
    }

    @Override
    @Nullable
    public Civilization getClaimOwner(ResourceKey<Level> level, ChunkPos chunkPos) {
        return Optional.ofNullable(claimsByPos.get(level, chunkPos))
                .map(this::getCivilization)
                .orElse(null);
    }

    @Override
    public void addClaim(Civilization civilization, ResourceKey<Level> level, ChunkPos chunkPos) {
        if (!this.claimsByPos.contains(level, chunkPos)) {
            this.claimsByPos.put(level, chunkPos, civilization.getId());
            addChunkToOwner(civilization, level, chunkPos);
            this.addDirtyId(civilization.getId());
            MinecraftForge.EVENT_BUS.post(new LandClaimChangedEvent(civilization, level, Collections.singletonList(chunkPos), ChangeType.ADD));
            if (sync) {
                NetworkHandler.getInstance()
                        .sendPacketToAll(new LandClaimUpdatePacket(
                                civilization.getId(),
                                Map.of(level, Collections.singletonList(chunkPos)),
                                ChangeType.ADD,
                                15
                        ));
            }
        }
    }

    @Override
    public void addClaims(Civilization civilization, ResourceKey<Level> level, Collection<ChunkPos> chunkPosList) {
        List<ChunkPos> updatedPos = new ArrayList<>();
        for (ChunkPos chunkPos : chunkPosList) {
            if (!this.claimsByPos.contains(level, chunkPos)) {
                this.claimsByPos.put(level, chunkPos, civilization.getId());
                addChunkToOwner(civilization, level, chunkPos);
                updatedPos.add(chunkPos);
            }
        }
        if (!updatedPos.isEmpty()) {
            this.addDirtyId(civilization.getId());
            MinecraftForge.EVENT_BUS.post(new LandClaimChangedEvent(civilization, level, updatedPos, ChangeType.ADD));
            if (sync) {
                NetworkHandler.getInstance()
                        .sendPacketToAll(new LandClaimUpdatePacket(
                                civilization.getId(),
                                Map.of(level, updatedPos),
                                ChangeType.ADD,
                                15
                        ));
            }
        }
    }

    private void addChunkToOwner(UUID civilizationId, ResourceKey<Level> level, ChunkPos chunkPos) {
        Collection<ChunkPos> claims = this.claimsByOwner.get(civilizationId, level);
        if (claims == null) {
            claims = new ArrayList<>();
            this.claimsByOwner.put(civilizationId, level, claims);
        }
        claims.add(chunkPos);
    }


    private void addChunkToOwner(Civilization civilization, ResourceKey<Level> level, ChunkPos chunkPos) {
        this.addChunkToOwner(civilization.getId(), level, chunkPos);
    }

    @Override
    public void removeClaim(Civilization civilization, ResourceKey<Level> level, ChunkPos chunkPos) {
        if (this.claimsByPos.contains(level, chunkPos)) {
            this.claimsByPos.remove(level, chunkPos);
            Optional.ofNullable(this.claimsByOwner.get(civilization.getId(), level))
                    .ifPresent(chunkPosClaim -> chunkPosClaim.remove(chunkPos));
            this.addDirtyId(civilization.getId());
            MinecraftForge.EVENT_BUS.post(new LandClaimChangedEvent(civilization, level, Collections.singleton(chunkPos), ChangeType.REMOVE));
            if (sync) {
                NetworkHandler.getInstance()
                        .sendPacketToAll(new LandClaimUpdatePacket(
                                civilization.getId(),
                                Map.of(level, Collections.singletonList(chunkPos)),
                                ChangeType.REMOVE,
                                14
                        ));
            }
        }
    }

    @Override
    public void removeClaims(Civilization civilization, ResourceKey<Level> level, Collection<ChunkPos> chunkPosList) {
        List<ChunkPos> updatedChunks = new ArrayList<>();
        for (ChunkPos chunkPos : chunkPosList) {
            if (this.claimsByPos.contains(level, chunkPos)) {
                this.claimsByPos.remove(level, chunkPos);
                Optional.ofNullable(this.claimsByOwner.get(civilization.getId(), level))
                        .ifPresent(chunkPosClaim -> chunkPosClaim.remove(chunkPos));
                updatedChunks.add(chunkPos);
            }
        }
        if (!updatedChunks.isEmpty()) {
            this.addDirtyId(civilization.getId());
            MinecraftForge.EVENT_BUS.post(new LandClaimChangedEvent(civilization, level, updatedChunks, ChangeType.REMOVE));
            if (sync) {
                NetworkHandler.getInstance()
                        .sendPacketToAll(new LandClaimUpdatePacket(
                                civilization.getId(),
                                Map.of(level, updatedChunks),
                                ChangeType.REMOVE,
                                14
                        ));
            }
        }
    }

    @Override
    public void transferClaims(Civilization fromCivilization, Civilization toCivilization) {
        Map<ResourceKey<Level>, Collection<ChunkPos>> fromClaims = this.claimsByOwner.row(fromCivilization.getId());
        for (Entry<ResourceKey<Level>, Collection<ChunkPos>> entry : fromClaims.entrySet()) {
            this.removeClaims(fromCivilization, entry.getKey(), entry.getValue());
            this.addClaims(toCivilization, entry.getKey(), entry.getValue());
        }
    }

    private Civilization getCivilization(UUID uuid) {
        return this.civilizations.getCivilizationById(uuid);
    }

    @Override
    public Collection<UUID> getIds() {
        return this.claimsByOwner.rowKeySet();
    }

    @Override
    @NotNull
    public CompoundTag getSerializedValue(UUID id) {
        CompoundTag serializedValue = new CompoundTag();
        ListTag claimsTag = new ListTag();
        for (Entry<ResourceKey<Level>, Collection<ChunkPos>> ownerClaims : this.claimsByOwner.row(id).entrySet()) {
            CompoundTag levelTag = new CompoundTag();
            ListTag chunkListTag = new ListTag();
            for (ChunkPos chunkPos : ownerClaims.getValue()) {
                CompoundTag chunkNBT = new CompoundTag();
                chunkNBT.putInt("X", chunkPos.x);
                chunkNBT.putInt("Z", chunkPos.z);
                chunkListTag.add(chunkNBT);
            }
            levelTag.putString("Level", ownerClaims.getKey().location().toString());
            levelTag.put("Chunks", chunkListTag);
            claimsTag.add(levelTag);
        }
        serializedValue.putUUID("Civilization", id);
        serializedValue.put("Claims", claimsTag);
        return serializedValue;
    }

    @Override
    public void deserializeAndInsertValue(@NotNull CompoundTag tag) {
        UUID civilizationId = tag.getUUID("Civilization");
        ListTag claimsList = tag.getList("Claims", Tag.TAG_COMPOUND);
        for (int i = 0; i < claimsList.size(); i++) {
            CompoundTag claimsTag = claimsList.getCompound(i);
            ResourceKey<Level> level = ResourceKey.create(
                    Registry.DIMENSION_REGISTRY,
                    new ResourceLocation(claimsTag.getString("Level"))
            );
            ListTag chunkListTag = claimsTag.getList("Chunks", Tag.TAG_COMPOUND);
            Collection<ChunkPos> chunkPosList = new ArrayList<>();
            for (int j = 0; j < chunkListTag.size(); j++) {
                CompoundTag chunkTag = chunkListTag.getCompound(j);
                ChunkPos chunkPos = new ChunkPos(
                        chunkTag.getInt("X"),
                        chunkTag.getInt("Z")
                );
                chunkPosList.add(chunkPos);
                this.claimsByPos.put(level, chunkPos, civilizationId);
            }
            this.claimsByOwner.put(civilizationId, level, chunkPosList);
        }
    }

    @Override
    public void onPlayerJoin(ServerPlayer serverPlayer) {
        Civilization playerCivilization = this.civilizations.getCivilizationByCitizen(serverPlayer);

        if (playerCivilization != null) {
            Map<ResourceKey<Level>, Collection<ChunkPos>> chunkPosMap = this.claimsByOwner.row(playerCivilization.getId());
            NetworkHandler.getInstance()
                    .sendPacket(
                            serverPlayer,
                            new LandClaimUpdatePacket(
                                    playerCivilization.getId(),
                                    chunkPosMap,
                                    ChangeType.ADD,
                                    5
                            )
                    );
        }


        for (Entry<UUID, Map<ResourceKey<Level>, Collection<ChunkPos>>> entry : this.claimsByOwner.rowMap().entrySet()) {
            NetworkHandler.getInstance()
                    .sendPacket(
                            serverPlayer,
                            new LandClaimUpdatePacket(
                                    entry.getKey(),
                                    entry.getValue(),
                                    ChangeType.ADD,
                                    13
                            )
                    );
        }

    }
}
