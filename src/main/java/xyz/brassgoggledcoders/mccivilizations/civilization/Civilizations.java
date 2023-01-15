package xyz.brassgoggledcoders.mccivilizations.civilization;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.ICivilizationRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class Civilizations implements ICivilizationRepository {
    private final Map<UUID, Civilization> civilizationById;
    private final Map<UUID, UUID> civilizationByCitizen;

    public Civilizations() {
        this(new HashMap<>(), new HashMap<>());
    }

    public Civilizations(Map<UUID, Civilization> civilizationById, Map<UUID, UUID> civilizationByCitizen) {
        this.civilizationById = civilizationById;
        this.civilizationByCitizen = civilizationByCitizen;
    }

    @Override
    @Nullable
    public Civilization getCivilizationByCitizen(Entity entity) {
        return Optional.ofNullable(civilizationByCitizen.get(entity.getUUID()))
                .map(this::getCivilizationById)
                .orElse(null);
    }

    @Override
    @Nullable
    public Civilization getCivilizationById(UUID id) {
        return civilizationById.get(id);
    }

    @Override
    public void upsertCivilization(Civilization civilization) {
        this.civilizationById.put(civilization.getId(), civilization);
    }

    @Override
    public void joinCivilization(Civilization civilization, Entity player) {
        if (this.getCivilizationById(civilization.getId()) != null) {
            this.civilizationByCitizen.put(civilization.getId(), player.getUUID());
        }
    }
}
