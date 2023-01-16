package xyz.brassgoggledcoders.mccivilizations.civilization;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.ICivilizationRepository;
import xyz.brassgoggledcoders.mccivilizations.repository.Repository;

import java.util.*;

public class CivilizationRepository extends Repository implements ICivilizationRepository {
    private final Map<UUID, Civilization> civilizationsById;
    private final Multimap<UUID, UUID> civilizationCitizens;
    private final Map<UUID, UUID> civilizationsByCitizen;

    public CivilizationRepository() {
        super("civilizations");
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
    public void upsertCivilization(Civilization civilization) {
        this.civilizationsById.put(civilization.getId(), civilization);
        this.addDirtyId(civilization.getId());
    }

    @Override
    public void joinCivilization(Civilization civilization, Entity player) {
        if (this.civilizationsById.containsKey(civilization.getId())) {
            this.civilizationsByCitizen.put(player.getUUID(), civilization.getId());
            this.addDirtyId(civilization.getId());
        }
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
                for (UUID citizen: citizens) {
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
}
