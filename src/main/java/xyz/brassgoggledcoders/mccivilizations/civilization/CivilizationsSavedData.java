package xyz.brassgoggledcoders.mccivilizations.civilization;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.ICivilizations;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class CivilizationsSavedData extends SavedData implements ICivilizations {
    private final Map<UUID, Civilization> civilizationsById;
    private final Map<UUID, UUID> civilizationsByCitizen;

    public CivilizationsSavedData() {
        this.civilizationsById = new HashMap<>();
        this.civilizationsByCitizen = new HashMap<>();
    }

    public CivilizationsSavedData(CompoundTag tag) {
        this();
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
    public void createCivilization(Civilization civilization) {
        this.civilizationsById.put(civilization.getId(), civilization);
    }

    @Override
    public void joinCivilization(Civilization newCivilization, Entity player) {
        if (this.civilizationsById.containsKey(newCivilization.getId())) {
            this.civilizationsByCitizen.put(player.getUUID(), newCivilization.getId());
        }
    }

    @Override
    @NotNull
    public CompoundTag save(@NotNull CompoundTag pCompoundTag) {
        return pCompoundTag;
    }
}
