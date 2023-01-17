package xyz.brassgoggledcoders.mccivilizations.civilization;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.ICivilizationRepository;
import xyz.brassgoggledcoders.mccivilizations.network.ChangeType;
import xyz.brassgoggledcoders.mccivilizations.network.CivilizationUpdatePacket;
import xyz.brassgoggledcoders.mccivilizations.network.NetworkHandler;
import xyz.brassgoggledcoders.mccivilizations.repository.Repository;

import java.util.*;

public class CivilizationRepository extends Repository implements ICivilizationRepository {
    private final Map<UUID, Civilization> civilizationsById;
    private final Multimap<UUID, UUID> civilizationCitizens;
    private final Map<UUID, UUID> civilizationsByCitizen;

    private final MinecraftServer server;

    public CivilizationRepository(MinecraftServer server) {
        super("civilizations");
        this.server = server;
        this.civilizationsById = new HashMap<>();
        this.civilizationsByCitizen = new HashMap<>();
        this.civilizationCitizens = HashMultimap.create();
    }

    @Override
    @Nullable
    public Civilization getCivilizationByCitizen(Entity entity) {
        return Optional.ofNullable(this.civilizationsByCitizen.get(entity.getUUID()))
                .map(this.civilizationsById::get)
                .orElse(null);
    }

    @Override
    @Nullable
    public Civilization getCivilizationById(UUID id) {
        return this.civilizationsById.get(id);
    }

    @Override
    public Collection<UUID> getCitizens(Civilization civilization) {
        return this.civilizationCitizens.get(civilization.getId());
    }

    @Override
    public void upsertCivilization(Civilization civilization) {
        this.civilizationsById.put(civilization.getId(), civilization);
        this.addDirtyId(civilization.getId());
        updateCitizens(civilization, new CivilizationUpdatePacket(civilization, ChangeType.ADD));
    }

    @Override
    public void joinCivilization(Civilization civilization, Entity player) {
        if (this.civilizationsById.containsKey(civilization.getId())) {
            this.civilizationsByCitizen.put(player.getUUID(), civilization.getId());
            this.civilizationCitizens.put(civilization.getId(), player.getUUID());
            this.addDirtyId(civilization.getId());
        }
    }

    @Override
    public void removeCivilization(Civilization civilization) {
        this.civilizationsById.remove(civilization.getId());
        Collection<UUID> uuids = this.civilizationCitizens.removeAll(civilization.getId());
        for (UUID uuid : uuids) {
            this.civilizationsByCitizen.remove(uuid);
        }
        this.addDirtyId(civilization.getId());
        updateCitizens(civilization, new CivilizationUpdatePacket(civilization, ChangeType.DELETE));
    }

    @Override
    @Nullable
    public CompoundTag getSerializedValue(UUID id) {
        Civilization civilization = this.civilizationsById.get(id);
        if (civilization != null) {
            CompoundTag tag = civilization.toTag();
            Collection<UUID> citizens = this.civilizationCitizens.get(id);
            if (!citizens.isEmpty()) {
                ListTag citizensTag = new ListTag();
                for (UUID citizen : citizens) {
                    citizensTag.add(NbtUtils.createUUID(citizen));
                }
                tag.put("Citizens", citizensTag);
            }
            return tag;
        }

        return null;
    }

    @Override
    public void deserializeAndInsertValue(@NotNull CompoundTag tag) {
        Civilization civilization = Civilization.fromTag(tag);
        this.civilizationsById.put(civilization.getId(), civilization);
        ListTag citizens = tag.getList("Citizens", Tag.TAG_INT_ARRAY);
        for (Tag citizen : citizens) {
            this.civilizationsByCitizen.put(civilization.getId(), NbtUtils.loadUUID(citizen));
        }
    }

    public void updateCitizens(Civilization civilization, Object packet) {
        if (this.server != null) {
            for (UUID citizen : this.getCitizens(civilization)) {
                ServerPlayer serverPlayer = this.server.getPlayerList()
                        .getPlayer(citizen);

                if (serverPlayer != null) {
                    NetworkHandler.getInstance()
                            .sendPacket(serverPlayer, packet);
                }
            }
        }
    }

    @Override
    public void onPlayerJoin(ServerPlayer serverPlayer) {
        Civilization civilization = this.getCivilizationByCitizen(serverPlayer);
        if (civilization != null) {
            NetworkHandler.getInstance()
                    .sendPacket(serverPlayer, new CivilizationUpdatePacket(civilization, ChangeType.ADD));
        }
    }
}
