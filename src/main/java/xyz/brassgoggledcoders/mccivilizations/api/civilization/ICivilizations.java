package xyz.brassgoggledcoders.mccivilizations.api.civilization;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface ICivilizations {
    @Nullable
    Civilization getCivilizationByCitizen(Entity entity);

    @Nullable
    Civilization getCivilizationById(UUID id);

    void createCivilization(Civilization civilization);

    void joinCivilization(Civilization newCivilization, Entity player);
}
