package xyz.brassgoggledcoders.mccivilizations.civilization;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.ICivilizationRepository;
import xyz.brassgoggledcoders.mccivilizations.api.repositories.ChangeType;
import xyz.brassgoggledcoders.mccivilizations.network.CivilizationCitizenUpdatePacket;
import xyz.brassgoggledcoders.mccivilizations.network.CivilizationUpdatePacket;
import xyz.brassgoggledcoders.mccivilizations.network.NetworkHandler;
import xyz.brassgoggledcoders.mccivilizations.repository.Repository;
import xyz.brassgoggledcoders.mccivilizations.util.DamerauLevenshtein;

import java.util.*;
import java.util.stream.Collectors;

public class CivilizationRepository extends Repository implements ICivilizationRepository {
    private final Map<UUID, Civilization> civilizationsById;
    private final Multimap<UUID, UUID> civilizationCitizens;
    private final Map<UUID, UUID> civilizationsByCitizen;

    private final boolean sync;

    public CivilizationRepository(boolean sync) {
        super("civilizations");
        this.sync = sync;
        this.civilizationsById = new HashMap<>();
        this.civilizationsByCitizen = new HashMap<>();
        this.civilizationCitizens = HashMultimap.create();
    }

    @Override
    @Nullable
    public Civilization getCivilizationByCitizen(@NotNull Entity entity) {
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
        if (sync) {
            NetworkHandler.getInstance()
                    .sendPacketToAll(new CivilizationUpdatePacket(Collections.singleton(civilization), ChangeType.ADD, 11));
        }

    }

    @Override
    public boolean joinCivilization(Civilization civilization, UUID citizen) {
        if (this.civilizationsById.containsKey(civilization.getId())) {
            this.civilizationsByCitizen.put(citizen, civilization.getId());
            this.civilizationCitizens.put(civilization.getId(), citizen);
            this.addDirtyId(civilization.getId());
            if (this.sync) {
                NetworkHandler.getInstance()
                        .sendPacketToAll(new CivilizationCitizenUpdatePacket(
                                civilization.getId(),
                                Collections.singleton(citizen),
                                ChangeType.ADD,
                                13
                        ));
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean leaveCivilization(Civilization civilization, UUID citizen) {
        if (this.civilizationsById.containsKey(civilization.getId())) {
            if (this.civilizationCitizens.remove(civilization.getId(), citizen)) {
                this.civilizationsByCitizen.remove(citizen);
                this.addDirtyId(civilization.getId());
                if (this.sync) {
                    NetworkHandler.getInstance()
                            .sendPacketToAll(new CivilizationCitizenUpdatePacket(
                                    civilization.getId(),
                                    Collections.singleton(citizen),
                                    ChangeType.REMOVE,
                                    13
                            ));
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void removeCivilization(Civilization civilization) {
        this.civilizationsById.remove(civilization.getId());
        Collection<UUID> uuids = this.civilizationCitizens.removeAll(civilization.getId());
        for (UUID uuid : uuids) {
            this.civilizationsByCitizen.remove(uuid);
        }
        this.addDirtyId(civilization.getId());
        if (sync) {
            NetworkHandler.getInstance()
                    .sendPacketToAll(new CivilizationUpdatePacket(Collections.singleton(civilization), ChangeType.REMOVE, 11));
        }
    }

    @Override
    public Collection<Civilization> getAllCivilizations() {
        return this.civilizationsById.values();
    }

    @Override
    @NotNull
    public Collection<Civilization> getCivilizationByName(String name) {
        Map<String, Collection<Civilization>> civilizationMap = this.getAllCivilizations()
                .stream()
                .collect(Collectors.toMap(
                        civilization -> civilization.getName().getString(),
                        civilization -> {
                            ArrayList<Civilization> civilizations = new ArrayList<>();
                            civilizations.add(civilization);
                            return civilizations;
                        },
                        (u, v) -> {
                            u.addAll(v);
                            return u;
                        }
                ));

        return DamerauLevenshtein.getClosest(name, civilizationMap.keySet(), 4)
                .stream()
                .map(civilizationMap::get)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
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
            UUID citizenId = NbtUtils.loadUUID(citizen);
            this.civilizationsByCitizen.put(citizenId, civilization.getId());
            this.civilizationCitizens.put(civilization.getId(), citizenId);
        }
    }

    @Override
    public void onPlayerJoin(ServerPlayer serverPlayer) {
        Civilization civilization = this.getCivilizationByCitizen(serverPlayer);
        if (civilization != null) {
            NetworkHandler.getInstance()
                    .sendPacket(serverPlayer, new CivilizationUpdatePacket(Collections.singleton(civilization), ChangeType.ADD, 1));
            NetworkHandler.getInstance()
                    .sendPacket(serverPlayer, new CivilizationCitizenUpdatePacket(
                            civilization.getId(),
                            this.getCitizens(civilization),
                            ChangeType.ADD,
                            3
                    ));
        }
        NetworkHandler.getInstance()
                .sendPacket(serverPlayer, new CivilizationUpdatePacket(this.getAllCivilizations(), ChangeType.ADD, 11));
    }
}
