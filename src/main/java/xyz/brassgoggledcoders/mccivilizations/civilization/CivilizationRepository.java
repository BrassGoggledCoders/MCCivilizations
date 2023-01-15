package xyz.brassgoggledcoders.mccivilizations.civilization;

import com.google.common.collect.ImmutableMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.ICivilizationRepository;
import xyz.brassgoggledcoders.mccivilizations.repository.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class CivilizationRepository extends Repository<UUID, Civilization> implements ICivilizationRepository {
    private final Map<UUID, Civilization> civilizationsById;
    private final Map<UUID, UUID> civilizationsByCitizen;

    public CivilizationRepository() {
        super("civilizations");
        this.civilizationsById = new HashMap<>();
        this.civilizationsByCitizen = new HashMap<>();
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
        this.setDirty(true);
    }

    @Override
    public void joinCivilization(Civilization newCivilization, Entity player) {
        if (this.civilizationsById.containsKey(newCivilization.getId())) {
            this.civilizationsByCitizen.put(player.getUUID(), newCivilization.getId());
            this.setDirty(true);
        }
    }
}
